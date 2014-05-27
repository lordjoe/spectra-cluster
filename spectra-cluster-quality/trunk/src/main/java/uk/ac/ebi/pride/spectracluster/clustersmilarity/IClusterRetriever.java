package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterRetriever {

    public IPeptideSpectralCluster retrieve(String clusterId);

    public Collection<IPeptideSpectralCluster> retrieve(double minMz, double maxMz);
}
