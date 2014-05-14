package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

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
    public ISpectralCluster getSource();

    /**
     * !null target cluster
     *
     * @return
     */
    public ISpectralCluster getTarget();

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @return
     */
    public double getDistance();

}