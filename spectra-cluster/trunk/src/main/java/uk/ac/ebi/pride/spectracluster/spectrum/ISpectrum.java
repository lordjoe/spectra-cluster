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
 * todo: Replace all the ISpectrum with IPeaksSpectrum, then rename IPeaksSpectrum to ISpectrum
 */
public interface ISpectrum extends Equivalent<ISpectrum>, Comparable<ISpectrum> {

    //todo: move constants out of this interface
    // if true only  MAX_PEAKS_TO_KEEP are usedI know
//    public static final boolean USE_HIGHEST_PEAKS = false;
    /**
     * after this many peaks we can drop the rest
     */
//    public static final int MAX_PEAKS_TO_KEEP = 100;

//    public static final int BAD_QUALITY_MEASURE = -1;

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

    /**
     * return unmodifiable peaks sorted by MZ
     *
     * @return !null array of peaks
     */
    public List<IPeak> getPeaks();

    /**
     * return number of peaks
     *
     * @return count
     */
    public int getPeaksCount();

    /**
     * get the highest intensity peaks sorted by MZ - this value may be cached
     *
     * @param numberRequested number peaks requested
     * @return Peaks spectrum
     */
    public ISpectrum getHighestNPeaks(int numberRequested);
}
