package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented bny a spectrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
@Deprecated
public interface ISpectrumOld extends ISpectrum, ISpectrumQuality, IMajorPeaksHolder {

    //todo: move constants out of this interface
    // if true only  MAX_PEAKS_TO_KEEP are usedI know
//    public static final boolean USE_HIGHEST_PEAKS = false;
    /**
     * after this many peaks we can drop the rest
     */
//    public static final int MAX_PEAKS_TO_KEEP = 100;

//    public static final int BAD_QUALITY_MEASURE = -1;


    /**
     * return a spectrum normalized to the specific total intensity
     *
     * @return !null spectrum - might be this
     */
    @Deprecated
    public INormalizedSpectrum asNormalizedTo(double totalIntensity);


    /**
     * does the concensus spectrum contain this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Deprecated
    public boolean containsMajorPeak(int mz);

}