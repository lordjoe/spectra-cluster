package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum
 * interface implemented bny a spectrum which may or mat not be a cluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectrum {

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
     * Replace existing peaks with a new collection of peaks
     */
    public void setPeaks(Collection<IPeak> peaks);


    public void appendMGF(Appendable out);

}
