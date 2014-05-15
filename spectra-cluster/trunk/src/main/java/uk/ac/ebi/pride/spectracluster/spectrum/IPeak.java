package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;

/**
 * IPeak is an interface which represents a peak in a spectrum
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IPeak extends Equivalent<IPeak>, Comparable<IPeak> {

    /**
     * Peak m/z
     */
    public float getMz();

    /**
     * Peak intensity
     */
    public float getIntensity();

    /**
     * If the peak is part of a consensus spectrum this number represents the number of
     * spectra making up the consensus spectrum that contain the respective peak. In normal spectra
     * this number is always 1.
     */
    public int getCount();


}
