package uk.ac.ebi.pride.tools.pride_spectra_clustering.impl;

import uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;

import java.util.ArrayList;
import java.util.List;

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
    public boolean processClusters() {
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

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(final ISpectralCluster cluster) {
        List<ISpectralCluster> noneFittingSpectra = new ArrayList<ISpectralCluster>();
        SimilarityChecker sCheck = Defaults.INSTANCE.getDefaultSimilarityChecker();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = sCheck.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = sCheck.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(spectrum.asCluster());
                }
            }
        }

        return noneFittingSpectra;

    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return "PrideClusteringEngine";
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {

    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return clusters.size();
    }
}
