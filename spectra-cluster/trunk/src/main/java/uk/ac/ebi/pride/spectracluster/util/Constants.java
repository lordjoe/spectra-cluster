package uk.ac.ebi.pride.spectracluster.util;

/**
 * Keep all the constants used
 *
 * @author Rui Wang
 * @version $Id$
 */
public final class Constants {

    private Constants() {
    }

    public static final double SMALL_MZ_DIFFERENCE = 0.002;
    public static final double SMALL_INTENSITY_DIFFERENCE = 0.1;
    public static final int HIGHEST_USABLE_MZ = 5000; // ignore peaks higher than this
    public static final int LOWEST_USABLE_MZ = 50; // ignore peaks lower than this


    // if true only  MAX_PEAKS_TO_KEEP are usedI know
    public static final boolean USE_HIGHEST_PEAKS = false;
    /**
     * after this many peaks we can drop the rest
     */
    public static final int MAX_PEAKS_TO_KEEP = 100;

    public static final int BAD_QUALITY_MEASURE = -1;
}