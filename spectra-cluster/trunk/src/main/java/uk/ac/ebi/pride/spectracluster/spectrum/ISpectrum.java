package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.math.BigInteger;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented bny a spectrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectrum extends IPeaksSpectrum, ISpectrumQuality, IMajorPeaksHolder {

    //todo: move constants out of this interface
    // if true only  MAX_PEAKS_TO_KEEP are usedI know
//    public static final boolean USE_HIGHEST_PEAKS = false;
    /**
     * after this many peaks we can drop the rest
     */
//    public static final int MAX_PEAKS_TO_KEEP = 100;

//    public static final int BAD_QUALITY_MEASURE = -1;


    /**
     * make a cluster containing a single spectrum - this
     *
     * @return
     */
    // todo: provide either util or constructor implmementation of this
    @Deprecated
    public ISpectralCluster asCluster();

    /**
     * get the highest intensity peaks sorted by MZ - this value may be cached
     *
     * @param numberRequested number peaks requested
     * @return Peaks spectrum
     */
    public IPeaksSpectrum getHighestNPeaks(int numberRequested);

    /**
     * return a spectrum normalized to the specific total intensity
     *
     * @return !null spectrum - might be this
     */
    public INormalizedSpectrum asNormalizedTo(double totalIntensity);

    /**
     * an optimization to return a Biginteger representing bits at the mz values where the
     * majors (top MAJOR_PEAK_NUMBER are
     *
     * @return !null value - lazily built
     */
    @Deprecated
    public BigInteger asMajorPeakBits();

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Deprecated
    public IPeaksSpectrum asMajorPeaks();

    /**
     * does the concensus spectrum contain this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Deprecated
    public boolean containsMajorPeak(int mz);

    /**
     * an optimization for computing distance based on dot product
     *
     * @return number > 0
     */
    public double getSelfDotProduct();

    /**
     * Get the quality measure of a spectrum
     */
    public double getQualityScore();

}
