package com.gg;

/**
 * Created by ggarg on 7/30/16.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.Map;

/**
 * CacheMonitor will clean up seat holds from Map for which are past hold time
 */
public class CacheMonitor implements Runnable {

    private static final Logger log = LogManager.getLogger(TicketServiceImpl.class.getName());

    Map<String, LocalTime> expireMap;
    Map<String, SeatHold> seatHoldMap;
    Map<Integer, Integer> inventory;

    CacheMonitor(Map<String, LocalTime> expireMap, Map<String, SeatHold> seatHoldMap, Map<Integer, Integer> inventory) {
        this.expireMap = expireMap;
        this.seatHoldMap = seatHoldMap;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<String, LocalTime>> iter = expireMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, LocalTime> entry = iter.next();
            if (LocalTime.now().isAfter(entry.getValue())) {
                iter.remove();
                SeatHold sh = seatHoldMap.get(entry.getKey());
                int currSeats = inventory.get(sh.seatLevel);
                inventory.put(sh.seatLevel, currSeats + sh.numSeats);
            }
        }
        log.info("Current Held Seats: " + expireMap);
    }
}
