package uk.ac.ebi.pride.spectracluster.similarity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.Peak;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.ArrayList;
import java.util.List;


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
 * <p/>
 * uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct
 *
 * @author jg
 *         <p/>
 */
public class FrankEtAlDotProduct implements ISimilarityChecker {
    /**
     * The logger to use.
     */
    private static final Logger logger = LoggerFactory.getLogger(FrankEtAlDotProduct.class);

    // add a nonsense park
    public static final IPeak LAST_PEAK = new Peak(Float.MAX_VALUE, 0);

    public static final int K2011_BIN_SIZE = 50;

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

    private double mzRange = Defaults.getSimilarityMZRange();
    /**
     * The algorithm version to use. By
     * default the version described in
     * Nature Methods 2011 will be used.
     */
    private AlgorithmVersion version = DEFAULT_ALGORITHM;

    /**
     * return the default similarity Threshold
     *
     * @return as above
     */
    @Override
    public double getDefaultThreshold() {
        return Defaults.getSimilarityThreshold();
    }

    /**
     * return the default similarity Threshold  this is
     * the threshold to keep  a spectrum in a cluster
     *
     * @return as above
     */
    @Override
    public double getDefaultRetainThreshold() {
        return Defaults.getRetainThreshold();
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
        ISpectrum highestPeaksSpectrum1 = spectrum1.getHighestNPeaks(numberCompared);
        double sumSquareIntensity1 = highestPeaksSpectrum1.getSumSquareIntensity();

        // the collection is immutable we need to build a new one
        List<IPeak> kHighestPeaks1 = new ArrayList(highestPeaksSpectrum1.getPeaks());
        kHighestPeaks1.add(LAST_PEAK); //add a peak we will not use
        IPeak[] peaks1 = kHighestPeaks1.toArray(new IPeak[kHighestPeaks1.size()]);

        ISpectrum highestPeaksSpectrum2 = spectrum2.getHighestNPeaks(numberCompared);
        double sumSquareIntensity2 = highestPeaksSpectrum2.getSumSquareIntensity();
        // the collection is immutable we need to build a new one
        List<IPeak> kHighestPeaks2 = new ArrayList(highestPeaksSpectrum2.getPeaks());
        kHighestPeaks2.add(LAST_PEAK); //add a peak we will not use
        IPeak[] peaks2 = kHighestPeaks2.toArray(new IPeak[kHighestPeaks2.size()]);

        double mzRange = this.getMzRange();
        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        double dotProduct = 0.0;

        while (t < peaks1.length - 1 && e < peaks2.length - 1) {
            IPeak peak1 = peaks1[t];
            double mz1 = peak1.getMz();
            IPeak peak2 = peaks2[e];
            double mz2 = peak2.getMz();

            double mass_difference = mz2 - mz1;

            if (Math.abs(mass_difference) <= mzRange) {
                // also calculate the difference for the next t and e peaks
                IPeak nextPeak1 = peaks1[t + 1];
                IPeak nextPeak2 = peaks2[e + 1];
                double mass_difference_nextT = mz2 - nextPeak1.getMz();
                double mass_difference_nextE = nextPeak2.getMz() - mz1;

                // use the next spectrum in E if it's a better match
                if (Math.abs(mass_difference_nextE) < Math.abs(mass_difference) &&
                    Math.abs(mass_difference_nextE) < Math.abs(mass_difference_nextT)) {
                    e++;
                    peak2 = nextPeak2;
                    mass_difference = mass_difference_nextE;
                }

                // do the matching
                double match1 = convertIntensity(peak1);
                double match2 = convertIntensity(peak2);

                // print the peaks if a debugOutput is supplied
                String fmt = String.format("%8.3f %8.3f  %8.3f %8.3f \n", peak1.getMz(), match1, peak2.getMz(), match2);
                logger.debug(fmt);

                dotProduct += match1 * match2;

                // increment both counters since both peaks must not be compared again
                e++;
                t++;
                continue;
            }

            if (mass_difference == 0) {
                if (lastIsT) {
                    e++;
                    lastIsT = false;
                } else {
                    t++;
                    lastIsT = true;
                }
            } else {
                if (mass_difference < 0) {
                    e++;
                } else {
                    t++;
                }
            }

        }
        // normalize the dot product
        double denom = Math.sqrt(sumSquareIntensity1 * sumSquareIntensity2);
        if (denom == 0)
            return 0;
        double normalizedDotProduct = dotProduct / denom;

        if (normalizedDotProduct >= 1)
            return normalizedDotProduct;  // todo look st this case

        return normalizedDotProduct;
    }

    /**
     * who knows why Johannes does this but we can as well
     * todo @rw: double check this wit Johannes
     */
    private double convertIntensity(IPeak p1) {
        double intensity = p1.getIntensity();
        if (intensity == 0)
            return 0;
        return 1 + Math.log(intensity);
    }

    protected int computeNumberComparedSpectra(ISpectrum spectrum1, ISpectrum spectrum2) {
        int numberComparedPeaks = Defaults.getNumberComparedPeaks();
        float precursorMz = spectrum1.getPrecursorMz();
        float precursor2 = spectrum2.getPrecursorMz();
        switch (version) {
            case NAT_METH_2011:
                numberComparedPeaks = calculateK2011(precursorMz, precursor2);
                break;
            case JPR_2008:
                numberComparedPeaks = calculateK2008(precursorMz, precursor2, spectrum1.getPrecursorCharge(), spectrum2.getPrecursorCharge());
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
            return Defaults.getNumberComparedPeaks();

        // take 15 peaks / 1000Da peptide mass
        double peptideMass = (precursor1 * charge1 + precursor2 * charge2) / 2;

        int largeBinningRegion = Defaults.getLargeBinningRegion();
        int k = Defaults.getNumberComparedPeaks() * (int) (peptideMass / largeBinningRegion);

        if (peptideMass % largeBinningRegion > 0)
            k += Defaults.getNumberComparedPeaks();

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
            return Defaults.getNumberComparedPeaks();

        // use m/z / 50
        return (int) ((precursor1 / K2011_BIN_SIZE + precursor2 / K2011_BIN_SIZE) / 2);
    }

    public double getMzRange() {
        return mzRange;
    }

    public AlgorithmVersion getVersion() {
        return version;
    }

    public void setMzRange(double mzRange) {
        this.mzRange = mzRange;
    }

    public void setVersion(AlgorithmVersion version) {
        this.version = version;
    }
}
