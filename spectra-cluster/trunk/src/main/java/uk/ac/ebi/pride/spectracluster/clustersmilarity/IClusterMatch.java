package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

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
    public IPeptideSpectrumCluster getSource();

    /**
     * !null target cluster
     *
     * @return
     */
    public IPeptideSpectrumCluster getTarget();

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @return
     */
    public double getDistance();

}
