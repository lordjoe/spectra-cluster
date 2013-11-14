package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.math.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented bny a spectrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectrum extends IPeaksSpectrum, ISpectrumQuality,IMajorPeaksHolder {

    /**
     * sort spectra by ID
     */
    public static final Comparator<ISpectrum>   ID_COMAPRATOR = new Comparator<ISpectrum>() {
          @Override
        public int compare(ISpectrum o1, ISpectrum o2) {
           return o1.getId().compareTo(o2.getId());
        }
    };
    /**
     * make a cluster contaiming a single spectrum - this
     *
     * @return
     */
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
     * @return  !null value - lazily built
     */
    public BigInteger asMajorPeakBits();

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     * @return
     */
     public IPeaksSpectrum asMajorPeaks();

    /**
      * does the concensus spectrum contin this is a major peak
      * @param mz   peak as int
      * @return  true if so
      */
     public boolean containsMajorPeak(int mz);

    /**
     * an optimization for computing distance based on dot product
     * @return number > 0
     */
    public double getSelfDotProduct();

}
