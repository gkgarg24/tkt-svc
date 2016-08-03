package com.gg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class TicketServiceImpl implements  TicketService {

    private static final Logger log = LogManager.getLogger(TicketServiceImpl.class.getName());

    Map<Integer, Integer> inventory;
    Map<String, SeatHold> seatHoldMap;
    long holdTime;

    TicketServiceImpl(Map<Integer, Integer> inventory, Map<String, SeatHold> seatHoldMap,
                       long holdTime) {
        this.inventory = inventory;
        this.seatHoldMap = seatHoldMap;
        this.holdTime = holdTime;

    }

    @Override
    public int numSeatsAvailable(Optional<Integer> venueLevel) {
        int seatAvail = 0;
        if (venueLevel.isPresent() ){
            for (Integer i : inventory.keySet()) {
                if (venueLevel.get().equals(i) ) {
                    seatAvail = inventory.get(i);
                    break;
                }
            }
        }
        log.info("Seat available: {}", seatAvail);
        return seatAvail;
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
        SeatHold sh = null;
        Set<Integer> setLvl = rangeCheck(minLevel, maxLevel);

        for (Integer i : setLvl) {
                sh = holdSeats(numSeats, i, customerEmail);
                if (sh != null) {
                    break;
                } else {
                    continue;
                }

        }
        return sh;
    }

    private SeatHold holdSeats(int numSeats, Integer level, String customerEmail) {
        int currSeats = inventory.get(level);
        if (currSeats < numSeats) {
            log.info("Seats Hold Failed!! Available: {} Requested: {} Level: {}", currSeats, numSeats, level);
            return null;
        } else {
            LocalTime lt = LocalTime.now().plusSeconds(holdTime);
            inventory.put(level, currSeats-numSeats);
            SeatHold sh = new SeatHold();
            sh.numSeats = numSeats;
            sh.custEmail = customerEmail;
            sh.seatLevel = level;
            sh.expTime = lt;
            sh.hold();
            seatHoldMap.put(customerEmail, sh);
            return sh;
        }
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
         SeatHold sh = seatHoldMap.get(customerEmail);
        if (sh == null) {
            log.error("Email does not exist: {}", customerEmail);
            System.out.println("Email not found! Please check entered mail Id is correct: " + customerEmail );
            return null;
        } else if (LocalTime.now().isAfter(sh.expTime)) {

            // Release seats from seatHoldMap
            int currSeats = inventory.get(sh.seatLevel);
            inventory.put(sh.seatLevel, currSeats+sh.numSeats);
            seatHoldMap.remove(customerEmail);
            System.out.println("Sorry! SeatHold is expired. Please search for seats and try again");
            return null;
        } else  {
             if (sh.seatHoldId != seatHoldId) {
                 log.error("SeatHold Id does not exist: ", seatHoldId);
                 System.out.println("SeatHold Id not found! Please check entered Id is correct: "+ seatHoldId);
                 return null;
             }
            String confirmId = "C"+seatHoldId;
            sh.confirmId = confirmId;
            sh.expTime = null; // Tickets are confirmed so no expiry time
            log.info("Seat Confirmed: {} Email: {}", confirmId, customerEmail);
            return  confirmId;
        }

    }

    // Add all the levels to reqLvl set for which customer can book ticket
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
            reqLvl.add(l1+i);
        }
        log.debug("Request Levels: " + reqLvl);
        return reqLvl;
    }
}
