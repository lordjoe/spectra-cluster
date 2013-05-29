package uk.ac.ebi.pride.spectracluster.similarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static org.junit.Assert.*;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class MultiSimillarityTest {
    private ConsensusSpectraItems[] consensusSpectraItems;

    public static final double SIMILAR_THRESHOLD = 10000; // this is really big

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = MultiSimillarityTest.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        consensusSpectraItems = ParserUtilities.readClusters(inputFile);


    }

    @Test
    public void testBuildConsensusSpectrum() throws Exception {
        // iterate over all clusters
        int index = 0;
        for (ConsensusSpectraItems cluster : consensusSpectraItems) {
            ISpectrum consensusSpectrum = cluster.getConcensus();
            List<ISpectrum> spectra = cluster.getSpectra();
            if(spectra.size() == 1)
                continue;
            // attila  do work!!!

            //       Assert.assertTrue(Math.abs(oldDP - newDP) < SIMILAR_THRESHOLD);
             if(true)
                throw new UnsupportedOperationException("Fix This"); // ToDo add test here
              index++; // track where we are
        }
    }




}
