package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterMatch extends Comparable<IClusterMatch>{

    public ISpectralCluster getSource();

    public ISpectralCluster getTarget();

}
