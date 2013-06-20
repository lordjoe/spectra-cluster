package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.*;


import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.IPeaksSpectrum
 * used by Spectra like get hightest peaks which are incomplete
 * spectra
 * User: Steve
 * Date: 6/20/13
 */
public interface IPeaksSpectrum extends Equivalent<ISpectrum>, Comparable<ISpectrum> {

    /**
     * globally unique id
     *
     * @return !null id
     */
    public String getId();

    /**
     * get precursor m/z
     */
    public double getPrecursorMz();

    /**
     * get charge - mixed charge
     */
    public double getPrecursorCharge();

    /**
     * return unmodifiable peaks sorted by MZ
     *
     * @return !null array of peaks
     */
    public List<IPeak> getPeaks();

    /**
     * return number of peaks
     * @return count
     */
    public int getPeaksCount();

    public void appendMGF(Appendable out);

    /**
     * return the sum of all intensities
     */
    public double getTotalIntensity();

    /**
     * return the sum  Square of all intensities
     */
    public double getSumSquareIntensity();
}
