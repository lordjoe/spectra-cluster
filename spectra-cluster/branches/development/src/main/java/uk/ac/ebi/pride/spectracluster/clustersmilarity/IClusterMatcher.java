package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterMatcher {

    public IClusterMatch match(ISpectralCluster source, ISpectralCluster target);

}
