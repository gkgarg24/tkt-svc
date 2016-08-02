package com.gg;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by ggarg on 7/31/16.
 */
public class TicketServiceImplTest {

    Map<Integer, Integer> hm2 = new ConcurrentHashMap<>();


    @Before
    public void setUp() throws Exception {
        hm2.put(1, 50*25);
        hm2.put(2, 100*20);
        hm2.put(3, 100*15);
        hm2.put(4, 100*15);

    }

    @Test
    public void numSeatsAvailableTest() throws Exception {

    assertEquals(hm2.get(1).intValue(), 1250);


    }


}