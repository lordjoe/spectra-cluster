package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterSet {

    public Collection<ISpectralCluster> getClusters();

    public Collection<ISpectralCluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<ISpectralCluster> getBestMatchingClusters(ISpectralCluster cluster, int maxMatches);

}
