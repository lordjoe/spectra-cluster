package uk.ac.ebi.pride.spectracluster.similarity;


import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
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
 * uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct
 * @author jg
 *         <p/>
 *         todo: this class needs to be reviewed
 */
public class FrankEtAlDotProduct implements SimilarityChecker {


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

    private double mzRange = Defaults.getSimilarityMZRange();
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

        Appendable debugOutput = Defaults.INSTANCE.getDebugOutput(); // if someone wants to see internal data write here
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
        double dotProduct = 0.0;

        while (t < peaks1.length && e < peaks2.length) {
            IPeak peak1 = peaks1[t];
            double mz1 = peak1.getMz();
            IPeak peak2 = peaks2[e];
            double mz2 = peak2.getMz();

            double mass_difference = mz2 - mz1;

            // also calculate the difference for the next t and e peaks
            double mass_difference_nextT = 100;
            if (t + 1 < peaks1.length) {
                mass_difference_nextT = mz2 - peaks1[t+1].getMz();
            }
            double mass_difference_nextE = 100;
            if (e + 1 < peaks2.length) {
                mass_difference_nextE = peaks2[e+1].getMz() - mz1;
            }

            if (Math.abs(mass_difference) <= mzRange) {

                double match1 = PeptideSpectrumMatch.johannesIntensityConverted(peak1);
                double match2 = PeptideSpectrumMatch.johannesIntensityConverted(peak2);

                // print the peaks if a debugOutput is supplied
                if(debugOutput != null)   {
                      String fmt = String.format("%8.3f %8.3f  %8.3f %8.3f \n", peak1.getMz(), match1, peak2.getMz(), match2);
                    try {
                        debugOutput.append(fmt);
                    }
                    catch (IOException e1) {
                        throw new RuntimeException(e1);

                    }
                 }

                dotProduct += match1 * match2;

                // increment both counters since both peaks must not be compared again
                e++;
                t++;
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
            return  Defaults.getNumberComparedPeaks();;

        // take 15 peaks / 1000Da peptide mass
        double peptideMass = (precursor1 * charge1 + precursor2 * charge2) / 2;

        int largeBinningRegion = Defaults.getLargeBinningRegion();
        int k =  Defaults.getNumberComparedPeaks() * (int) (peptideMass / largeBinningRegion);

        if (peptideMass % largeBinningRegion > 0)
            k +=  Defaults.getNumberComparedPeaks();;

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
