package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.IPeaksSpectrum
 * used by Spectra like get hightest peaks which are incomplete
 * spectra
 * User: Steve
 * Date: 6/20/13
 */
public interface IPeaksSpectrum extends Equivalent<ISpectrum>, IPeaksHolder, Comparable<ISpectrum> {

    /**
     * globally unique id
     *
     * @return !null id
     */
    public String getId();

    /**
     * get precursor m/z
     */
    public float getPrecursorMz();

    /**
     * get charge - mixed charge
     */
    public int getPrecursorCharge();

    /**
     * return the sum of all intensities
     */
    public double getTotalIntensity();

    /**
     * return the sum  Square of all intensities
     */
    public double getSumSquareIntensity();
}
