package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterMatch extends Comparable<IClusterMatch> {

    /**
     * !null source cluster
     *
     * @return
     */
    public IPeptideSpectralCluster getSource();

    /**
     * !null target cluster
     *
     * @return
     */
    public IPeptideSpectralCluster getTarget();

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @return
     */
    public double getDistance();

}
