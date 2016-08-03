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

    Map<String, SeatHold> seatHoldMap;
    Map<Integer, Integer> inventory;

    CacheMonitor(Map<String, SeatHold> seatHoldMap, Map<Integer, Integer> inventory) {
        this.seatHoldMap = seatHoldMap;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        log.info("Current Held Seats: " + seatHoldMap);
        Iterator<Map.Entry<String, SeatHold>> iter = seatHoldMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, SeatHold> entry = iter.next();
            SeatHold sh = entry.getValue();
            if (sh.confirmId != null) {
                log.debug("Reservation is confirmed: {}" , sh.confirmId);
                continue;
            } else if (LocalTime.now().isAfter(sh.expTime)) {
                iter.remove();
                int currSeats = inventory.get(sh.seatLevel);
                inventory.put(sh.seatLevel, currSeats + sh.numSeats);
            }
        }
        log.info("Current Inventory: " + inventory);
    }
}
