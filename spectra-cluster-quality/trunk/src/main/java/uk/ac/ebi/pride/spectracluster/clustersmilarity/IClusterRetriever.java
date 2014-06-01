package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterRetriever {

    public ICluster retrieve(String clusterId);

    public Collection<ICluster> retrieve(double minMz, double maxMz);
}
