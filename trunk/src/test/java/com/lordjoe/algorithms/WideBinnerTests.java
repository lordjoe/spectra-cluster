package com.lordjoe.algorithms;

import org.junit.*;


public class WideBinnerTests {

    public static final double MAX_VALUE = 1000;
    public static final double BIN_SIZE = 10;
    public static final int NUMBER_OVERLAPS = 1;


    @Test
    public void testBinning() throws Exception {
        int nOverlaps = NUMBER_OVERLAPS;
        IWideBinner  binner = new LinearWideBinner(MAX_VALUE, BIN_SIZE,0,true,nOverlaps);
        int[] answer = new int[1 + 2 * nOverlaps];
         // normal inrange values
        for (int i = (int)(nOverlaps + 1 * BIN_SIZE); i < MAX_VALUE - (nOverlaps + 1) * BIN_SIZE; i++) {
             final int mainBin = binner.asBin(i);
            Assert.assertEquals((int) (i / BIN_SIZE), mainBin);
            int[] bins = binner.asBins(i);
            Assert.assertEquals(answer.length,bins.length);

            for (int index = 0,  j = mainBin - nOverlaps; j <=  mainBin + nOverlaps; j++,index++) {
                  answer[index] = j;
              }
            Assert.assertArrayEquals(answer,bins);
        }
        int[] firstBins = binner.asBins(0);
        Assert.assertEquals(1 + nOverlaps,firstBins.length);
    }


    @Test
    public void testBinning2() throws Exception {
        int nOverlaps = 3;
        IWideBinner  binner = new LinearWideBinner(MAX_VALUE, BIN_SIZE,0,true,nOverlaps);
        int[] answer = new int[1 + 2 * nOverlaps];
        // normal inrange values
        for (int i = (int)((nOverlaps + 1) * BIN_SIZE); i < MAX_VALUE - (nOverlaps + 1) * BIN_SIZE; i++) {
            final int mainBin = binner.asBin(i);
            Assert.assertEquals((int) (i / BIN_SIZE), mainBin);
            int[] bins = binner.asBins(i);
            Assert.assertEquals(answer.length,bins.length);

            for (int index = 0,  j = mainBin - nOverlaps; j <=  mainBin + nOverlaps; j++,index++) {
                  answer[index] = j;
              }
            Assert.assertArrayEquals(answer,bins);
        }
        int[] firstBins = binner.asBins(0);
        Assert.assertEquals(1 + nOverlaps,firstBins.length);
    }



}
