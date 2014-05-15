package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.IPeaksSpectrum
 * used by Spectra like get hightest peaks which are incomplete
 * spectra
 * User: Steve
 * Date: 6/20/13
 *
 */
public interface ISpectrum extends ISpectrumQuality, Equivalent<ISpectrum>, Comparable<ISpectrum> {

    /**
     * globally unique id
     *
     * @return !null id
     */
    String getId();

    /**
     * get precursor m/z
     */
    float getPrecursorMz();

    /**
     * get charge - mixed charge
     */
    int getPrecursorCharge();

    /**
     * return the sum of all intensities
     */
    double getTotalIntensity();

    /**
     * return the sum  Square of all intensities
     */
    double getSumSquareIntensity();

    /**
     * return unmodifiable peaks sorted by MZ
     *
     * @return !null array of peaks
     */
    List<IPeak> getPeaks();

    /**
     * return number of peaks
     *
     * @return count
     */
    int getPeaksCount();

    /**
     * get the highest intensity peaks sorted by MZ - this value may be cached
     *
     * @param numberRequested number peaks requested
     * @return Peaks spectrum
     */
    ISpectrum getHighestNPeaks(int numberRequested);


    /**
     * Get the peaks with highest intensity sort by MZ
     *
     * @return  array of major peak mz
     */
    int[] asMajorPeakMZs();
}
