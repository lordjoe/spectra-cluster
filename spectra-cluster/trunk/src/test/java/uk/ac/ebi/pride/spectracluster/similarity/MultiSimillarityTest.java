package uk.ac.ebi.pride.spectracluster.similarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.util.ConsensusSpectraItems;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;

import java.io.File;
import java.net.URL;
import java.util.List;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class MultiSimillarityTest {
    private ConsensusSpectraItems[] consensusSpectraItems;

    @SuppressWarnings("UnusedDeclaration")
    public static final double SIMILAR_THRESHOLD = 1.5; // this is really big

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        // URL url = MultiSimillarityTest.class.getClassLoader().getResource ("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
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


            SimilarityChecker oldSimilarity = new FrankEtAlDotProductOld();
            SimilarityChecker newSimilarity = new FrankEtAlDotProduct();

/*            double oldDP;
            oldDP = oldSimilarity.assessSimilarity(spectra.get(2), spectra.get(3));
            double newDP;
            newDP = newSimilarity.assessSimilarity(spectra.get(2), spectra.get(3));*/

            //Assert.assertEquals(oldDP, newDP, 0.001 * (oldDP + newDP));
            //Assert.assertTrue(Math.abs(oldDP - newDP) < SIMILAR_THRESHOLD);

            for (int index1 = 0; index1 < spectra.size(); index1++) {
                for (int index2 = index1 + 1; index2 < spectra.size(); index2++) {

                    double oldDotP;
                    oldDotP = oldSimilarity.assessSimilarity(spectra.get(index1), spectra.get(index2));
                    double newDotP;
                    newDotP = newSimilarity.assessSimilarity(spectra.get(index1), spectra.get(index2));

                    Assert.assertEquals(oldDotP, newDotP, 0.00001);
                    //Assert.assertTrue(Math.abs(oldDotP - newDotP) < SIMILAR_THRESHOLD);

                }

            }




/*            for (ISpectrum iSpectrum : spectra) {


                //for (int i = 0; spectra.size() > i; i++) {
                //   ISpectrum  spectrum = spectra[i];

                double oldDP1;
                oldDP1 = oldSimilarity.assessSimilarity(iSpectrum, consensusSpectrum);
                double newDP2;
                newDP2 = newSimilarity.assessSimilarity(iSpectrum, consensusSpectrum);

                double oldDP3;
                oldDP3 = oldSimilarity.assessSimilarity(iSpectrum, iSpectrum);
                double newDP4;
                newDP4 = newSimilarity.assessSimilarity(iSpectrum, iSpectrum);

                //Assert.assertEquals(oldDP, newDP, 0.001 * (oldDP + newDP));
                Assert.assertTrue(Math.abs(oldDP1 - newDP2) < SIMILAR_THRESHOLD);


                // comparing consensusSp agains member spectra gives -Infinity intensity in the example testCluster.cgf consensus spectrum

            }*/

        }

    }
}





