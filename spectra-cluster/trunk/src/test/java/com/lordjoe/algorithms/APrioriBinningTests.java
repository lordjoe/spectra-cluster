package com.lordjoe.algorithms;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.APrioriBinning;

import java.util.Collections;
import java.util.List;

/**
 * com.lordjoe.algorithms.APrioriBinningTests
 * User: Steve
 * Date: 4/24/2014
 */
public class APrioriBinningTests {

    @Test
    public void testResourceBinning() {
        List<MarkedNumber<String>> markedNumbers = APrioriBinning.readFromResource();
        List<MarkedNumber<String>> normalized = MarkedNumber.normalize(markedNumbers);


    }

    @Test
    public void testBinning() {
        List<MarkedNumber<String>> markedNumbers = APrioriBinning.readFromResource();
        List<MarkedNumber<String>> normalized = MarkedNumber.normalize(markedNumbers);
        Collections.sort(markedNumbers);
        double lastEntry = Double.MAX_VALUE;
        for (MarkedNumber<String> markedNumber : markedNumbers) {
            double current = markedNumber.getValue();
            Assert.assertTrue(current <= lastEntry);
            lastEntry = current;
        }

        Assert.assertEquals(1.0, MarkedNumber.total(normalized), 0.001);

    }

    @Test
    public void testSort() {
        List<MarkedNumber<String>> markedNumbers = APrioriBinning.readFromResource();
        Collections.sort(markedNumbers);
        double lastEntry = Double.MAX_VALUE;
        for (MarkedNumber<String> markedNumber : markedNumbers) {
            double current = markedNumber.getValue();
            Assert.assertTrue(current <= lastEntry);
            lastEntry = current;
        }


    }

    @Test
    public void testNormalizedSort() {
        List<MarkedNumber<String>> markedNumbers = APrioriBinning.readFromResource();
        List<MarkedNumber<String>> normalized = MarkedNumber.normalize(markedNumbers);
        double lastEntry = Double.MAX_VALUE;
        for (MarkedNumber<String> markedNumber : normalized) {
            double current = markedNumber.getValue();
            Assert.assertTrue(current <= lastEntry);
            lastEntry = current;
        }

    }

}
