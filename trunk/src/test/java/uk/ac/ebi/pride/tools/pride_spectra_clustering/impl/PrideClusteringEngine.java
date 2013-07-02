package uk.ac.ebi.pride.tools.pride_spectra_clustering.impl;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;

import java.util.*;

/**
 * Implement a clustering Engine using the original johannes code
 * NOT Implemented yet
 *
 * @author Steve Lewis
 * @version $Id$
 */
public class PrideClusteringEngine implements IClusteringEngine {

    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    private List<SpectraCluster> clustersFound = null;
    private final List<ClusteringSpectrum> addedSpectra = new ArrayList<ClusteringSpectrum>();
    private final SpectraClustering clustering = new FrankEtAlClustering();

    public PrideClusteringEngine() {
        clustering.setClusteringRounds(2);
        clustering.setSimilarityThreshold(0.7);
    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        //      guaranteeClean();
        return new ArrayList<ISpectralCluster>(clusters);
    }


    /**
     * add some clusters
     */
    @Override
    public void addClusters(ISpectralCluster... cluster) {
        if (cluster != null) {
            for (ISpectralCluster sc : cluster) {
                final SpectraCluster spectraCluster = Adapters.fromSpectraCluster(sc);
                final List<ClusteringSpectrum> spectra = spectraCluster.getSpectra();
                addedSpectra.addAll(spectra);
            }

        }
        clustersFound = null;

    }


    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean mergeClusters() {
        if (clustersFound != null)
            return false; // already done

        clusters.clear();
        clustersFound = clustering.clusterConvertedSpectra(addedSpectra);
        for (SpectraCluster cluster : clustersFound) {
            final ISpectralCluster spectralCluster = Adapters.fromSpectraCluster(cluster);
            clusters.add(spectralCluster);
        }

        return false; // we are done after one pass
    }


}
