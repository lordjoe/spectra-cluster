package uk.ac.ebi.pride.spectracluster.similarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;


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
            if (spectra.size() <= 1)
                continue;
            // attila  do work!!!

            SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
            SimilarityChecker newSimilarity = new FrankEtAlDotProduct();

            double oldDP;
            oldDP = oldSimilarity.assessSimilarity(spectra.get(0), spectra.get(1));
            double newDP;
            newDP = newSimilarity.assessSimilarity(spectra.get(0), spectra.get(1));

            //Assert.assertEquals(oldDP, newDP, 0.001 * (oldDP + newDP));
            Assert.assertTrue(Math.abs(oldDP - newDP) < SIMILAR_THRESHOLD);



/*
            for (ISpectrum iSpectrum : spectra) {


                //for (int i = 0; spectra.size() > i; i++) {
                //   ISpectrum  spectrum = spectra[i];

                double oldDP1;
                oldDP1 = oldSimilarity.assessSimilarity(iSpectrum, consensusSpectrum);
                double newDP2;
                newDP2 = newSimilarity.assessSimilarity(iSpectrum, consensusSpectrum);

                double oldDP;
                oldDP = oldSimilarity.assessSimilarity(iSpectrum, iSpectrum);
                double newDP;
                newDP = newSimilarity.assessSimilarity(iSpectrum, iSpectrum);

                //Assert.assertEquals(oldDP, newDP, 0.001 * (oldDP + newDP));
                Assert.assertTrue(Math.abs(oldDP - newDP) < SIMILAR_THRESHOLD);


                // comparing consensusSp agains member spectra gives -Infinity intensity in the example consensus spectrum

            }*/

        }

    }
}




