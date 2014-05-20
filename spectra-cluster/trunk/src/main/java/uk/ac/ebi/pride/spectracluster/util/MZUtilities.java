package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MZUtilities {

    // todo: decide where to store these static variables
    public static final int MZ_RESOLUTION = 1000; // we care about differences of 0.01 dalton
    /**
     * Rounding factor to use. 1000 means 3 positions after the comma.
     */
    public final static int MZ_PRECISSION = 1000; // using a precision of 1000 reduces memory usages but leads to different results.

    /**
     * convert am int into an mz for east comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static int mzToInt(double mz) {
        return (int) ((MZ_RESOLUTION * mz) + 0.5);
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double minClusterMZ(List<ICluster> clusters) {
        double ret = Double.MAX_VALUE;
        for (ICluster cluster : clusters) {
            ret = Math.min(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double maxClusterMZ(List<ICluster> clusters) {
        double ret = 0;
        for (ICluster cluster : clusters) {
            ret = Math.max(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }


    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double minSpectraMZ(List<ISpectrum> clusters) {
        double ret = Double.MAX_VALUE;
        for (ISpectrum cluster : clusters) {
            ret = Math.min(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double maxSpectraMZ(List<ISpectrum> clusters) {
        double ret = 0;
        for (ISpectrum cluster : clusters) {
            ret = Math.max(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }

    public static String describeDaltons(double precursorMZ) {
        return "MZ" + String.format("%05d", (int) (precursorMZ + 0.5));
    }

    public static double asDaltons(String asDaltons) {
        return Integer.parseInt(asDaltons.substring(2));
    }

    /**
     * Round to certain number of decimals
     *
     * @param f
     * @return
     */
    @Deprecated
    public static double round(double f) {
        return round(f, MZ_PRECISSION);
    }

    /**
     * Round to certain number of decimals
     *
     * @param f
     * @param decimalPlace
     * @return
     */
    public static double round(double f, int decimalPlace) {
        //        BigDecimal bd = new BigDecimal(Float.toString(d));
        //        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        //        return bd.floatValue();
        int i = (int) ((f * decimalPlace) + 0.5);
        return i / (double) decimalPlace;
    }
}
