package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.math.BigInteger;

/**
 * uk.ac.ebi.uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented by a specIMajorPeaksHoldertrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectrum extends IPeaksSpectrum, ISpectrumQuality, IMajorPeaksHolder {

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
     * an optimization to return a Biginteger representing bits at the mz values where the
     * majors (top MAJOR_PEAK_NUMBER are
     * @return  !null value - lazily built
     *
     * TODO: @Steve do we really need this?
     */
    public BigInteger asMajorPeakBits();

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank et all's suggestion that all spectra in a cluster will share at least one of these
     * @return
     */
     public IPeaksSpectrum asMajorPeaks();




}
