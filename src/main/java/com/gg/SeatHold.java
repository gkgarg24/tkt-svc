package com.gg;

import java.time.LocalTime;
import java.util.Random;

/**
 * Created by ggarg on 7/19/16.
 */
public class SeatHold {

    int seatLevel;
    int numSeats;
    int seatHoldId;
    String custEmail;
    LocalTime expTime;
    static Random r = new Random();


    public void hold() {
        seatHoldId = r.nextInt(Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return "SeatHold{" +
                "custEmail='" + custEmail + '\'' +
                ", seatLevel=" + seatLevel +
                ", numSeats=" + numSeats +
                ", seatHoldId=" + seatHoldId +
                ", expTime=" + expTime +
                '}';
    }

}