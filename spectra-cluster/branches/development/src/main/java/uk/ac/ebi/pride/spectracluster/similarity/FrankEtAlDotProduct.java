package uk.ac.ebi.pride.spectracluster.similarity;


import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;


/**
 * Assesses the similarity between two
 * spectra using the normalized
 * dot-product as implemented by Frank et al. (2008) JPR
 * <p/>
 * This implementation uses only the k highest peaks.
 * k is calculated by taking 15 peaks per 1000 Da
 * peptide mass. Furthermore, the vectors for the
 * dot-product are filled with the 1+ln(I) where I
 * is the peak's normalized intensity.
 *
 * @author jg
 *         <p/>
 *         todo: this class needs to be reviewed
 */
public class FrankEtAlDotProduct implements SimilarityChecker {

    public static final int NUMBER_COMPARED_PEAKS = 15;

    public static final double DEFAULT_MZ_RANGE = 0.5;

    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;

    public static final double DEFAULT_RETAIN_THRESHOLD = 0.5;

    public static final int LARGE_BINNING_REGION = 1000;

    public static final int K2011_BIN_SIZE = 50;
    /**
     * The logger to use.
     */
    //private static final Logger logger = Logger.getLogger(FrankEtAlDotProduct.class);

    /**
     * The versions available from this algorithm. The only
     * difference is the way k is calculated.
     *
     * @author jg
     */
    public enum AlgorithmVersion {
        JPR_2008, NAT_METH_2011
    }

    public static final AlgorithmVersion DEFAULT_ALGORITHM = AlgorithmVersion.NAT_METH_2011;

    /**
     * Use Defaults which builds with reflection
     * Set the class with Defaults.setSimilarityCheckerClass
     */

    public FrankEtAlDotProduct() {
    }

    /**
     * return a name which should not change
     *
     * @return !null name
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * return a version number - this may be updated over time
     *
     * @return !null version
     */
    @Override
    public String getCurrentVersion() {
        return getVersion().toString();
    }

    private double mzRange = DEFAULT_MZ_RANGE;
    /**
     * The algorithm version to use. By
     * default the version described in
     * Nature Methods 2011 will be used.
     */
    private AlgorithmVersion version = DEFAULT_ALGORITHM;

    /**
     * erturn the default similarity Threshold
     *
     * @return as above
     */
    @Override
    public double getDefaultThreshold() {
        return DEFAULT_SIMILARITY_THRESHOLD;
    }

    /**
     * return the default similarity Threshold  this is
     * the threshold to keep  a spectrum in a cluster
     *
     * @return as above
     */
    @Override
    public double getDefaultRetainThreshold() {
        return DEFAULT_RETAIN_THRESHOLD;
    }

    /**
     * Assesses the spectra's similarity using
     * the normalized dot-product
     */
    @Override
    public double assessSimilarity(ISpectrum spectrum1, ISpectrum spectrum2) {
        // initialize the number of peaks1 to use with 15
        int numberCompared = computeNumberComparedSpectra(spectrum1, spectrum2);

        // get the k highest peaks1 from every spectrum
        IPeaksSpectrum highestPeaksSpectrum1 = spectrum1.getHighestNPeaks(numberCompared);
        double sumSquareIntensity1 = highestPeaksSpectrum1.getSumSquareIntensity();

        List<IPeak> kHighestPeaks1 = highestPeaksSpectrum1.getPeaks();
        IPeak[] peaks1 = kHighestPeaks1.toArray(new IPeak[kHighestPeaks1.size()]);

        IPeaksSpectrum highestPeaksSpectrum2 = spectrum2.getHighestNPeaks(numberCompared);
        double sumSquareIntensity2 = highestPeaksSpectrum2.getSumSquareIntensity();
        List<IPeak> kHighestPeaks2 = highestPeaksSpectrum2.getPeaks();
        IPeak[] peaks2 = kHighestPeaks2.toArray(new IPeak[kHighestPeaks2.size()]);

        double mzRange = this.getMzRange();
        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        int MatchingProducts = 0;
        int TotalProducts = 0;
        double dotProduct = 0.0;
        double charge = spectrum1.getPrecursorCharge();
        double charge2 = spectrum2.getPrecursorCharge();
        // todo do we penalize different charges??
        while (t < peaks1.length && e < peaks2.length) {
            TotalProducts++;
            IPeak peak1 = peaks1[t];
            double mz1 = peak1.getMz();
            IPeak peak2 = peaks2[e];
            double mz2 = peak2.getMz();

            double mass_difference = mz2 - mz1;
            if (Math.abs(mass_difference) <= mzRange) {
                double match1 = PeptideSpectrumMatch.johannesIntensityConverted(peak1);
                double match2 = PeptideSpectrumMatch.johannesIntensityConverted(peak2);
                // String fmt = String.format("%8.3f %8.3f  %8.3f %8.3f", peak1.getMz(), match1, peak2.getMz(), match2);
                //          System.out.println(fmt);

                MatchingProducts++;
                dotProduct += match1 * match2;
            }
            if (mass_difference == 0) {
                if (lastIsT) {
                    e++;
                    lastIsT = false;
                }
                else {
                    t++;
                    lastIsT = true;
                }
            }
            else {
                if (mass_difference < 0) {
                    e++;
                }
                else {
                    t++;
                }
            }

        }
        // normalize the dot product
        double denom = Math.sqrt(sumSquareIntensity1 * sumSquareIntensity2);
        if (denom == 0)
            return 0;
        double normalizedDotProduct = dotProduct / denom;

        return normalizedDotProduct;

    }

    protected int computeNumberComparedSpectra(ISpectrum spectrum1, ISpectrum spectrum2) {
        int numberComparedPeaks = NUMBER_COMPARED_PEAKS;
        switch (version) {
            case NAT_METH_2011:
                numberComparedPeaks = calculateK2011(spectrum1.getPrecursorMz(), spectrum2.getPrecursorMz());
                break;
            case JPR_2008:
                numberComparedPeaks = calculateK2008(spectrum1.getPrecursorMz(), spectrum2.getPrecursorMz(), spectrum1.getPrecursorCharge(), spectrum2.getPrecursorCharge());
                break;
        }
        return numberComparedPeaks;
    }


    /**
     * Calculate k by using 15 per 1000 Da of
     * peptide mass.
     *
     * @param precursor1
     * @param precursor2
     * @param charge1
     * @param charge2
     * @return
     */
    private int calculateK2008(Float precursor1, Float precursor2,
                               Integer charge1, Integer charge2) {
        // if any of the required values is missing, return 15
        if (precursor1 == null || precursor2 == null || charge1 == null || charge2 == null || charge1 <= 0 || charge2 <= 0)
            return NUMBER_COMPARED_PEAKS;

        // take 15 peaks / 1000Da peptide mass
        double peptideMass = (precursor1 * charge1 + precursor2 * charge2) / 2;

        int k = NUMBER_COMPARED_PEAKS * (int) (peptideMass / LARGE_BINNING_REGION);

        if (peptideMass % LARGE_BINNING_REGION > 0)
            k += NUMBER_COMPARED_PEAKS;

        return k;
    }

    /**
     * Calculate k by using the precursor m/z / 50.
     *
     * @param precursor1
     * @param precursor2
     * @return
     */
    private int calculateK2011(Float precursor1, Float precursor2) {
        // if any of the required values is missing, return 15
        if (precursor1 == null || precursor2 == null)
            return NUMBER_COMPARED_PEAKS;

        // use m/z / 50
        int k = (int) ((precursor1 / K2011_BIN_SIZE + precursor2 / K2011_BIN_SIZE) / 2);

        return k;
    }

    public double getMzRange() {
        return mzRange;
    }

    public AlgorithmVersion getVersion() {
        return version ;
    }

    public void setMzRange(double mzRange) {
        this.mzRange = mzRange;
    }

    public void setVersion(AlgorithmVersion version) {
        this.version = version;
    }
}