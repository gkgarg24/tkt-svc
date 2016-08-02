package com.gg;

/**
 * Created by ggarg on 7/30/16.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CacheMonitor will clean up seat holds from Map for which are past hold time
 */
public class CacheMonitor implements Runnable {

    private static final Logger log = LogManager.getLogger(TicketServiceImpl.class.getName());

    Map<Integer, LocalTime> expireMap;

    CacheMonitor(Map<Integer, LocalTime> expireMap) {
        this.expireMap = expireMap;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<Integer, LocalTime>> iter = expireMap.entrySet().iterator();
        while (iter.hasNext()) {
            if (LocalTime.now().isAfter(iter.next().getValue())) {
                iter.remove();
            }
        }
        System.out.println("Nothing expired");
    }
}
