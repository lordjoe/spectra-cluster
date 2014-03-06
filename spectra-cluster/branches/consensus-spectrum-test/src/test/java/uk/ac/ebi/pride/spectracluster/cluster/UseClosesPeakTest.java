package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 2/19/14
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class UseClosesPeakTest {
    private FrankEtAlDotProduct checker = new FrankEtAlDotProduct();
    List<IPeptideSpectrumMatch> spectra;


    @Before
    public void setUp() throws Exception {
        spectra = ClusteringTestUtilities.readISpectraFromResource();
    }

    @Test
    public void testDifferentDotProducts() {
        int total = 0;
        int different = 0;

        for (IPeptideSpectrumMatch psm1 : spectra) {
            for (IPeptideSpectrumMatch psm2 : spectra) {
                checker.setUseClosestPeak(true);
                double dotOrg = checker.assessSimilarity(psm1, psm2);

                checker.setUseClosestPeak(false);
                double dotNew = checker.assessSimilarity(psm1, psm2);

                if (dotOrg -0.01 > dotNew || dotOrg + 0.01 < dotNew) {
                    different++;

                    if (dotOrg >= 0.7)
                        System.out.printf("Old: %f\tNew: %f\tDiff: %f\n", dotOrg, dotNew, dotOrg - dotNew);
                }



                total++;
            }
        }

        TestCase.assertEquals(total, different, 0.2);
    }
}
