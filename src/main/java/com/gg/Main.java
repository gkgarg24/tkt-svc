package com.gg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.InputMismatchException;
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

        // Maps LevelID to Ticket price
        Map<Integer, String> priceMap = new HashMap<>();
        priceMap.put(1, "100.00");
        priceMap.put(2, "75.00");
        priceMap.put(3, "50.00");
        priceMap.put(4, "40.00");

        // It will map the Customer Email to SeatHold objects
        Map<String, SeatHold> seatHoldMap = new HashMap<>();

        // Hold time in seconds after which hold seats expire returned to inventory
        long holdTime = 30;

        Main tc = new Main();

        TicketServiceImpl ts = new TicketServiceImpl(hm, seatHoldMap, holdTime);

        // Run scheduler to delete expired seat holds
        schedExec.scheduleAtFixedRate(new CacheMonitor(seatHoldMap, hm), 1 , 1, TimeUnit.MINUTES);

        log.info("Starting Ticket Service..");

        while (true) {
            tc.runSvc(levelMap, priceMap, ts);
        }
    }

    private void runSvc(Map<Integer, String> levelMap, Map<Integer, String> priceMap, TicketServiceImpl ts) {
        Scanner sc = new Scanner(System.in);

        System.out.printf("Please select operations: \ns (Search), \nf (Find and Hold), \nr (Reserve)\n");
        String input = sc.nextLine();

        try {
            if (input.equals("s")) {
                Scanner sc2 = new Scanner(System.in);
                System.out.println("Please select the level (1-4): ");
                for (Map.Entry e : levelMap.entrySet()) {
                    System.out.println(e.getKey() + ": " + e.getValue() + "\tPrice: $" + priceMap.get(e.getKey()));
                }
                int level = sc2.nextInt();
                System.out.printf("\nNo of seat available on Level %d (%s): %d\n", level, levelMap.get(level),
                        ts.numSeatsAvailable(Optional.of(level)));
            } else if (input.equals("f")) {
                Scanner sc2 = new Scanner(System.in);
                System.out.println("\nPlease enter no of seats: ");
                int seat = sc2.nextInt();
                System.out.println("\nPlease enter min level (1-4): ");
                int minLvl = sc2.nextInt();
                System.out.println("\nPlease enter max level (1-4): ");
                int maxLvl = sc2.nextInt();
                System.out.println("\nPlease enter email: ");
                String email = sc2.next();
                if (email != null && email.trim().length() <= 0) {
                    System.out.println("Email is not valid!");
                    return;
                }
                SeatHold sh =  ts.findAndHoldSeats(seat, Optional.of(minLvl), Optional.of(maxLvl), email);
                System.out.println("Please Keep following hold info for booking later");
                System.out.printf("\nSeats on Hold: %d \nSeat Level: %d \nSeatHold ID: %d \nExpiry Time: %tT " +
                        "\nEmail: %s\n\n", sh.numSeats, sh.seatLevel, sh.seatHoldId, sh.expTime, sh.custEmail);
            } else if (input.equals("r")) {
                Scanner sc2 = new Scanner(System.in);
                System.out.println("Please enter SeatHold Id: ");
                int seatHoldID = sc2.nextInt();
                System.out.println("Please enter email: ");
                String email = sc2.next();
                if (email != null && email.trim().length() <= 0) {
                    System.out.println("Email is not valid!");
                    return;
                }
                String resv = ts.reserveSeats(seatHoldID, email);
                if (resv != null) {
                    System.out.println("Congratulations your seats are reserved!");
                    System.out.println("Reservation ID: " + resv);
                } else {
                    System.out.println("Could Not Reserve seat!! ");
                }
            } else {
                System.out.printf("!!Wrong operation name!!: %s\n", input);
            }
        } catch (InputMismatchException e) {
            System.out.println("Wrong Value!! Please enter correct number \n");

        }
        sc.reset();
    }
}
