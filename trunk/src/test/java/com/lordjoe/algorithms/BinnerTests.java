package com.lordjoe.algorithms;

import org.junit.*;


public class BinnerTests {
    public static BinnerTests[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = BinnerTests.class;

    public static final double MAX_VALUE = 1000;
    public static final double BIN_SIZE = 10;

    public static IBinner buildLinearBinner(double max, double binsize) {
        return new LinearBinner(max, binsize);
    }

    @Test
    public void testBinning() throws Exception {
        IBinner binner = new LinearBinner(MAX_VALUE, BIN_SIZE);
        // normal inrange values
        for (int i = 0; i < MAX_VALUE; i++) {
            Assert.assertEquals((int) (i / BIN_SIZE), binner.asBin(i));
        }
        // default out of range is to bin
        final int expected = binner.getMaxBin() - 1;
        Assert.assertEquals(expected, binner.asBin(MAX_VALUE + BIN_SIZE));
        // default out of range is to bin
        Assert.assertEquals(0, binner.asBin(-BIN_SIZE));
    }

    @Test
    public void testLinearBinner() throws Exception{

        IBinner binner = new LinearBinner(MAX_VALUE, BIN_SIZE);

        for (int bin = 0; bin < binner.getNumberBins(); bin++) {
            Assert.assertEquals((int) (bin / BIN_SIZE), binner.asBin(bin));
            double value = binner.fromBin(bin);
            int actualBin = binner.asBin(value);
            Assert.assertEquals(bin, actualBin);

        }

    }

}
