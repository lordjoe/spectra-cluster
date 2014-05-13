package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IPeaksHolder
 * User: Steve
 * Date: 7/10/13
 */
public interface IPeaksHolder {

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

}
