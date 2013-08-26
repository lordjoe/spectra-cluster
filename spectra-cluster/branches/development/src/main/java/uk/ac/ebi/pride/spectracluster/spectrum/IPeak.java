package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.*;

/**
 * uk.ac.ebi.pride.spectracluster.IPeak
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

//    /**
//     * Set peak m/z
//     */
//    public void setMz(double mz);

    /**
     * Peak intensity
     */
    public float getIntensity();

    /**
     * Set peak intensity
     */
 //   public void setIntensity(double intensity);

    /**
     * number associated spectra
     */
    public int getCount();


}
