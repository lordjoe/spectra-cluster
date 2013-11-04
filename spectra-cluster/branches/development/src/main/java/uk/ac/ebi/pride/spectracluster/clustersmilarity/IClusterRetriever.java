package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterRetriever {

    public ISpectralCluster retrieve(String clusterId);

    public Collection<ISpectralCluster> retrieve(double minMz, double maxMz);
}
