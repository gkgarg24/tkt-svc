package com.gg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {


        // Cleanup scheduler will run to remove expired seat holds.
        ScheduledExecutorService schedExec = Executors.newScheduledThreadPool(1);

        // Map the level to total seats available at each level
        Map<Integer, Integer> hm = new ConcurrentHashMap<>();
        hm.put(1, 50*25);
        hm.put(2, 100*20);
        hm.put(3, 100*15);
        hm.put(4, 100*15);

        // Maps LevelID to Level name
        Map<Integer, String> levelMap = new HashMap<>();
        levelMap.put(1, "Orchestra");
        levelMap.put(2, "Main");
        levelMap.put(3, "Balcony 1");
        levelMap.put(4, "Balcony 2");

        // It will map the Customer Email to SeatHold objects
        Map<String, SeatHold> seatHoldMap = new HashMap<>();

        // It will seat hold id to expiry time
        Map<String, LocalTime> expireMap = new HashMap<>();

        // Hold time in minutes after which hold seats expire returned to inventory
        long holdTime = 5;

        Main tc = new Main();

        TicketServiceImpl ts = new TicketServiceImpl(hm, seatHoldMap, expireMap, holdTime);

        // Run scheduler to delete expired seat holds
        schedExec.scheduleAtFixedRate(new CacheMonitor(expireMap, seatHoldMap, hm), 1 , 1, TimeUnit.MINUTES);

        log.info("Starting Ticket Service..");

        while (true) {
            tc.runSvc(levelMap, ts);
        }
    }

    private void runSvc(Map<Integer, String> levelMap, TicketServiceImpl ts) {
        Scanner sc = new Scanner(System.in);

        System.out.printf("Please select operations: \ns (Search), \nf (Find and Hold), \nr (Reserve)\n");
        String input = sc.nextLine();

        if (input.equals("s")) {
            Scanner sc2 = new Scanner(System.in);
            System.out.println("Please enter the level: ");
            for(Map.Entry e : levelMap.entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue());
            }
            int level = sc2.nextInt();
            System.out.printf("No of seat available on Level %d (%s): %d\n", level, levelMap.get(level),
                    ts.numSeatsAvailable(Optional.of(level)));
        } else if (input.equals("f")) {
            Scanner sc2 = new Scanner(System.in);
            System.out.println("Please enter no of seats: ");
            int seat = sc2.nextInt();
            System.out.println("Please enter min level (1-4): ");
            int minLvl = sc2.nextInt();
            System.out.println("Please enter max level (1-4): ");
            int maxLvl = sc2.nextInt();
            System.out.println("Please enter email: ");
            String email = sc2.nextLine();
            System.out.println("Find and Hold: " + ts.findAndHoldSeats(seat, Optional.of(minLvl),
                    Optional.of(maxLvl), email));
        } else if (input.equals("r")) {
            Scanner sc2 = new Scanner(System.in);
            System.out.println("Please enter seat hold ID: ");
            int seatHoldID = sc2.nextInt();
            System.out.println("Please enter email: ");
            String email = sc2.nextLine();
            System.out.println("Reservation ID: " + ts.reserveSeats(seatHoldID, email));
        } else  {
            System.out.printf("!!Wrong operation name!!: %s\n", input);
        }
        sc.reset();
    }
}
