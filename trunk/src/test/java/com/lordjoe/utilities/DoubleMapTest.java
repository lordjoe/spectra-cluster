package com.lordjoe.utilities;

import org.junit.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * com.lordjoe.utilities.DoubleMapTest
 *
 * @author Steve Lewis
 * @date 07/05/13
 */
public class DoubleMapTest {
    public static DoubleMapTest[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = DoubleMapTest.class;

    public static final Random RND = new Random();
    public static final int DEFAULT_MAP_SIZE = 1000;

    public Map<Double,Double> buildMap( ) {
         return buildMap(DEFAULT_MAP_SIZE);
     }

    public Map<Double,Double> buildMap(int size) {
        Map<Double,Double> ret = new HashMap<Double,Double>();
        for (int i = 0; i < size; i++) {
             ret.put(RND.nextDouble() * 2000,RND.nextDouble() * 2000);

        }
        return ret;
    }

    /**
     * this seems to work - not sure why
     */
    @Test
    public void testMapFormat()
    {
        Map<Double,Double> mp = buildMap( );
        for(Double key : mp.keySet()) {
            String s = Double.toString(key);
            Double test = new Double(s);
            Assert.assertTrue(mp.containsKey(test));
        }
    }

    /**
     * this is expected to fail
     */
   @Test
    public void testMathFormat()
    {
        Map<Double,Double> mp = buildMap( );
        for(Double key : mp.keySet()) {
            double d = key /10;
            d *= 10;
             Assert.assertTrue(mp.containsKey(d));
        }
    }
}
