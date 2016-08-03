package com.gg;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by ggarg on 7/31/16.
 */
public class TicketServiceImplTest {

    Map<Integer, Integer> hm = new ConcurrentHashMap<>();
    Map<Integer, String> levelMap = new HashMap<>();
    Map<String, SeatHold> seatMap = new HashMap<>();
    Map<String, LocalTime> expireMap = new HashMap<>();
    TicketService ts;
    long holdTime = 1; // In seconds

    ScheduledExecutorService schedExec = Executors.newScheduledThreadPool(1);

    @Before
    public void setUp() throws Exception {
        hm.put(1, 50*25);
        hm.put(2, 100*20);
        hm.put(3, 100*15);
        hm.put(4, 100*15);

        levelMap.put(1, "Orchestra");
        levelMap.put(2, "Main");
        levelMap.put(3, "Balcony 1");
        levelMap.put(4, "Balcony 2");

        ts = new TicketServiceImpl(hm, seatMap, expireMap, holdTime);
        schedExec.scheduleAtFixedRate(new CacheMonitor(expireMap, seatMap, hm), 5, 5, TimeUnit.SECONDS);

    }

    @Test
    public void totalSeatLevel1() throws Exception {
        assertEquals(hm.get(1).intValue(), 1250);

    }

    @Test
    public void totalSeatLevel2() throws Exception {
        assertEquals(hm.get(2).intValue(), 2000);

    }

    @Test
    public void totalSeatLevel3() throws Exception {
        assertEquals(hm.get(3).intValue(), 1500);

    }

    @Test
    public void totalSeatLevel4() throws Exception {
        assertEquals(hm.get(4).intValue(), 1500);

    }

    @Test
    public void numSeatAvailAfterBooking() throws Exception {
        ts.findAndHoldSeats(23, Optional.of(2), Optional.of(4), "gg@ab.com");
        assertEquals(hm.get(2).intValue(), 1977);
    }

    @Test
    public void numSeatAvailAfterBooking1() throws Exception {
        ts.findAndHoldSeats(300, Optional.of(1), Optional.of(4), "gg@ab.com");
        assertEquals(hm.get(1).intValue(), 950);
    }

    @Test
    public void checkHoldandReserve() throws Exception {
        ts.findAndHoldSeats(30, Optional.of(3), Optional.of(4), "gg@ab.com");
        System.out.println(seatMap);
        Thread.sleep(20*1000);
        ts.reserveSeats(seatMap.get("gg@ab.com").seatHoldId, "gg@ab.com");
        assertEquals(hm.get(3).intValue(), 1470);
    }

    @Test
    public void checkHoldExpiry() throws Exception {
        ts.findAndHoldSeats(30, Optional.of(3), Optional.of(4), "gg@ab.com");
        System.out.println(expireMap);
        Thread.sleep(80*1000);
        assertEquals(hm.get(3).intValue(), 1500);
    }

    // Hold seats at next level if min level has numSeats not available.
    @Test
    public void bookNextLevel() throws Exception {
        ts.findAndHoldSeats(300, Optional.of(1), Optional.of(4), "gg@ab.com");
        System.out.println(hm);
        ts.findAndHoldSeats(300, Optional.of(1), Optional.of(4), "gg1@ab.com");
        System.out.println(hm);
        ts.findAndHoldSeats(300, Optional.of(1), Optional.of(4), "gg2@ab.com");
        System.out.println(hm);
        ts.findAndHoldSeats(300, Optional.of(1), Optional.of(4), "gg3@ab.com");
        System.out.println(hm);
        ts.findAndHoldSeats(400, Optional.of(1), Optional.of(4), "gg4@ab.com");
        System.out.println(hm);
        assertEquals(hm.get(2).intValue(), 1600);
    }
}