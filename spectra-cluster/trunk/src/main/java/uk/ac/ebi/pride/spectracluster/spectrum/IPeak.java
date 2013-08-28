package uk.ac.ebi.pride.spectracluster.spectrum;


import uk.ac.ebi.pride.spectracluster.util.Equivalent;

/**
 * uk.ac.ebi.uk.ac.ebi.pride.spectracluster.IPeak
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IPeak extends Equivalent<IPeak>, Comparable<IPeak> {
    public static final double SMALL_MZ_DIFFERENCE = 0.002;
    public static final double SMALL_INTENSITY_DIFFERENCE = 0.1;
    public static final int HIGHEST_USABLE_MZ = 5000; // ignore peaks higher than this
    public static final int LOWEST_USABLE_MZ = 50; // ignore peaks lower than this


    /**
     * Peak m/z
     */
    public float getMz();

    /**
     * Peak intensity
     */
    public float getIntensity();

    /**
     * number associated spectra
     */
    public int getCount();
}
