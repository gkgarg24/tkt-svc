package com.gg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class TicketServiceImpl implements  TicketService {

    private static final Logger log = LogManager.getLogger(TicketServiceImpl.class.getName());

    Map<Integer, Integer> inventory;
    Map<String, SeatHold> holdMap;
    Map<Integer, LocalTime> expireMap;
    long holdTime; // In minutes

    TicketServiceImpl(Map<Integer, Integer> inventory, Map<String, SeatHold> holdMap,
                      Map<Integer, LocalTime> expireMap, long holdTime) {
        this.inventory = inventory;
        this.holdMap = holdMap;
        this.expireMap = expireMap;
        this.holdTime = holdTime;

    }

    @Override
    public int numSeatsAvailable(Optional<Integer> venueLevel) {
        int seatAvail = 0;
        if (venueLevel.isPresent() ){
            for (Integer i : inventory.keySet()) {
                if (venueLevel.get() == i ) {
                    seatAvail = inventory.get(i);
                    break;
                }
            }
        }
        log.info("Seat available: %d", seatAvail);
        return seatAvail;
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
       int seatAvail = 0;
        SeatHold sh = null;
        Set<Integer> setLvl = rangeCheck(minLevel, maxLevel);

        for (Integer i : setLvl) {
                sh = holdSeats(numSeats, i, customerEmail);
                break;
        }
        return sh;
    }

    private SeatHold holdSeats(int numSeats, Integer level, String customerEmail) {
        int currSeats = inventory.get(level);
        if (currSeats < numSeats) {
            return null;
        } else {
            LocalTime lt = LocalTime.now().plusMinutes(holdTime);
            inventory.put(level, currSeats-numSeats);
            SeatHold sh = new SeatHold();
            sh.numSeats = numSeats;
            sh.custEmail = customerEmail;
            sh.seatLevel = level;
            sh.expTime = lt;
            sh.hold();
            holdMap.put(customerEmail, sh);
            expireMap.put(sh.seatHoldId, lt);
            return sh;
        }
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        LocalTime lt = expireMap.get(seatHoldId);
        if (LocalTime.now().isAfter(lt)) {
            // Delete entry from expireMap
            expireMap.remove(seatHoldId);
            // Release seats from holdMap
            SeatHold sh = holdMap.get(customerEmail);
            int currSeats = inventory.get(sh.seatLevel);
            inventory.put(sh.seatLevel, currSeats+sh.numSeats);
        } else {
            String confirm = "C"+seatHoldId;
            return  confirm;
        }

        return null;
    }

    private Set<Integer> rangeCheck(Optional<Integer> minLevel, Optional<Integer> maxLevel) {
        Set<Integer> allLvl = inventory.keySet();
        Set<Integer> reqLvl = new HashSet<>();
        int l1 = 0, l2 = 0;

        if (minLevel.isPresent() && allLvl.contains(minLevel.get())) {
            l1 = minLevel.get();
            reqLvl.add(l1);
        }
        if (maxLevel.isPresent() && allLvl.contains(maxLevel.get())) {
            l2 = maxLevel.get();
            reqLvl.add(l2);
        }
        int ld = l2 -l1;
        for (int i=1; i < ld; i++) {
            reqLvl.add(i);
        }
        return reqLvl;
    }
}
