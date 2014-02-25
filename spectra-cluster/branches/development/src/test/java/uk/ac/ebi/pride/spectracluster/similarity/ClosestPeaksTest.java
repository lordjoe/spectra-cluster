package uk.ac.ebi.pride.spectracluster.similarity;

import junit.framework.*;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 2/19/14
 * Time: 9:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClosestPeaksTest {

    public static final String[] INTERESTING_IDS =
            {
                    "1248200",
                    "1249768",
                    "1249841",
                    "1250984",
//                    "1252317",
//                    "1252580",
//                    "1252923",
//                    "18834",
//                    "58604",
//                    "99609",
            };

    public static Set<String> INTERESTING_ID_SET = new HashSet<String>(Arrays.asList(INTERESTING_IDS));

    /**
     * really here to print out the peaks compared on a few interesting cases where the results are different
     */
    @Test
    public void showHandledPeaksForInterestingCases() {
        List<IPeptideSpectrumMatch> spectra = ClusteringTestUtilities.readISpectraFromResource();

        IPeptideSpectrumMatch[] spectrums = (IPeptideSpectrumMatch[]) spectra.toArray();

        SimilarityChecker checker = new FrankEtAlDotProductTester();
        SimilarityChecker currentChecker = new FrankEtAlDotProduct();

        Set<String> interestingIds = new HashSet<String>();


        for (int i = 0; i < spectrums.length; i++) {
            IPeptideSpectrumMatch psm1 = spectrums[i];
            String id1 = psm1.getId();
            if (!INTERESTING_ID_SET.contains(id1))
                continue; // not an interesting case

            for (int j = i + 1; j < spectrums.length; j++) {
                IPeptideSpectrumMatch psm2 = spectrums[j];

                String id2 = psm2.getId();
                if (!INTERESTING_ID_SET.contains(id2))
                    continue; // not an interesting case

                System.out.println("Comparing " + id1 + " " + id2);

                StringBuilder usedPeaksTester = new StringBuilder();
                Defaults.INSTANCE.setDebugOutput(usedPeaksTester);


                  double dotOrg = checker.assessSimilarity(psm1, psm2);

                System.out.println("Peaks compared original Frank Et Al (when the code is written)");
                // print usage
                System.out.println(usedPeaksTester.toString());

                usedPeaksTester.setLength(0);  // clear debug output
                double dotNew = currentChecker.assessSimilarity(psm1, psm2);

                // print usage
                System.out.println("Peaks compared current Frank Et Al ");
                System.out.println(usedPeaksTester.toString());


            }

        }
    }

    /**
     * test that latest and refactored dot products give the same answer
     */
    @Test
    public void testDifferentDotProducts() {
        List<IPeptideSpectrumMatch> spectra = ClusteringTestUtilities.readISpectraFromResource();

        IPeptideSpectrumMatch[] spectrums = (IPeptideSpectrumMatch[]) spectra.toArray();

        int total = 0;
        int different = 0;
        SimilarityChecker checker = new FrankEtAlDotProduct();
        SimilarityChecker currentChecker = new FrankEtAlDotProductJohannes();

        Set<String> interestingIds = new HashSet<String>();


        for (int i = 0; i < spectrums.length; i++) {
            IPeptideSpectrumMatch psm1 = spectrums[i];
            for (int j = i + 1; j < spectrums.length; j++) {
                IPeptideSpectrumMatch psm2 = spectrums[j];
                double dotOrg = checker.assessSimilarity(psm1, psm2);
                double dotNew = currentChecker.assessSimilarity(psm1, psm2);

                if (dotOrg - 0.01 > dotNew || dotOrg + 0.01 < dotNew) {
                    different++;

                    StringBuilder usedPeaksTester = new StringBuilder();
                    Defaults.INSTANCE.setDebugOutput(usedPeaksTester);

                    // these are the really interesting cases
                    dotOrg = checker.assessSimilarity(psm1, psm2);

                    double noClosestPeak = dotNew;
                    dotNew = currentChecker.assessSimilarity(psm1, psm2);
                    String id2 = psm2.getId();
                    String id1 = psm1.getId();
                    interestingIds.add(id1);
                    interestingIds.add(id2);

                    System.out.println(usedPeaksTester.toString());
                    Defaults.INSTANCE.setDebugOutput(null);
                    System.out.printf(id2 + ":" + id1 + " " + "Old: %8.3f Newx: %8.3f New: %8.3f\tDiff: %8.3f\n", dotOrg, noClosestPeak, dotNew, dotOrg - dotNew);
                }
                total++;

            }

        }

        List<String> sorted = new ArrayList<String>(interestingIds);
        Collections.sort(sorted);
        System.out.println("Interesting Ids");
        for (String s : sorted) {
            System.out.println(s);
        }


        TestCase.assertEquals(0, different );
    }

    /**
     * test that latest and refactored dot products give the same answer
     */
    @Test
    public void testDifferentDotProductsOlder() {
        List<IPeptideSpectrumMatch> spectra = ClusteringTestUtilities.readISpectraFromResource();

        IPeptideSpectrumMatch[] spectrums = (IPeptideSpectrumMatch[]) spectra.toArray();

        int total = 0;
        int different = 0;
        SimilarityChecker checker = new FrankEtAlDotProduct();
        FrankEtAlDotProduct.CHECK_BEST_PEAK_SPEC1 = false;
        SimilarityChecker oldChecker = new FrankEtAlDotProductTester();

        Set<String> interestingIds = new HashSet<String>();


        for (int i = 0; i < spectrums.length; i++) {
            IPeptideSpectrumMatch psm1 = spectrums[i];
            for (int j = i + 1; j < spectrums.length; j++) {
                IPeptideSpectrumMatch psm2 = spectrums[j];
                double dotOrg = checker.assessSimilarity(psm1, psm2);
                double dotNew = oldChecker.assessSimilarity(psm1, psm2);

                if (dotOrg - 0.01 > dotNew || dotOrg + 0.01 < dotNew) {
                    different++;
                    StringBuilder usedPeaksTester = new StringBuilder();
                        Defaults.INSTANCE.setDebugOutput(usedPeaksTester);
                    // these are the really interesting cases
                    dotOrg = checker.assessSimilarity(psm1, psm2);

                    double noClosestPeak = dotNew;
                    dotNew = oldChecker.assessSimilarity(psm1, psm2);
                    String id2 = psm2.getId();
                    String id1 = psm1.getId();
                    interestingIds.add(id1);
                    interestingIds.add(id2);

                    System.out.println(usedPeaksTester.toString());
                         Defaults.INSTANCE.setDebugOutput(null);
                    System.out.printf(id2 + ":" + id1 + " " + "Old: %8.3f Newx: %8.3f New: %8.3f\tDiff: %8.3f\n", dotOrg, noClosestPeak, dotNew, dotOrg - dotNew);
                }
                total++;

            }

        }

        List<String> sorted = new ArrayList<String>(interestingIds);
        Collections.sort(sorted);
        System.out.println("Interesting Ids");
        for (String s : sorted) {
            System.out.println(s);
        }


        TestCase.assertEquals(0, different );
    }

}
