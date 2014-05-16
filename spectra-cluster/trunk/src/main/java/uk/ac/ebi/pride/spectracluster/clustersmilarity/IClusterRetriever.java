package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterRetriever {

    public IPeptideSpectrumCluster retrieve(String clusterId);

    public Collection<IPeptideSpectrumCluster> retrieve(double minMz, double maxMz);
}
