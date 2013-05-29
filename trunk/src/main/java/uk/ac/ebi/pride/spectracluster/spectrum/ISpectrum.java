package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented bny a spectrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectrum extends ISpectrumQuality, Equivalent<ISpectrum>, Comparable<ISpectrum> {

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
     * return peaks sorted by MZ
     *
     * @return !null array of peaks
     */
    public List<IPeak> getPeaks();


    /**
     * return number of peaks
     * @return count
     */
    public int getPeaksCount();

    /**
     * Replace existing peaks with a new collection of peaks
     */
    public void setPeaks(Collection<IPeak> peaks);


    public void appendMGF(Appendable out);


    /**
      * make a cluster contaiming a single spectrum - this
      * @return
      */
     public ISpectralCluster asCluster();

    /**
     * get the highest intensity peaks sorted by MZ - this value may be cached
     * @param numberRequested  number peaks requested
     * @return  list of no more than  numberRequested peaks in Mz order
     */
    public ISpectrum  getHighestNPeaks(int numberRequested);

    /**
     * return the sum of all intensities
     */
    public double getTotalIntensity();

    /**
     * return the sum  Square of all intensities
     */
    public double getSumSquareIntensity();


}
