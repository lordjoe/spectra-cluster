package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * Default implementation of the clustering engine
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngine implements IClusteringEngine {

    private boolean dirty;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    private final List<ISpectralCluster> clustersToAdd = new ArrayList<ISpectralCluster>();
    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;

    public ClusteringEngine(SimilarityChecker similarityChecker,
                            Comparator<ISpectralCluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
    }

    protected void guaranteeClean() {
        if (isDirty()) {
            Collections.sort(clustersToAdd, spectrumComparator);
            addToClusters();
            setDirty(false);
        }
    }

    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        guaranteeClean();
        return new ArrayList<ISpectralCluster>(clusters);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(ISpectralCluster... cluster) {
        if (cluster != null) {
            clustersToAdd.addAll(Arrays.asList(cluster));
            setDirty(true);
        }

    }

    private void addToClusters() {
        for (ISpectralCluster clusterToAdd : clustersToAdd) {
            ISpectralCluster mostSimilarCluster = null;
            double highestSimilarityScore = 0;

            // find the cluster with the highest similarity score
            for (ISpectralCluster cluster : clusters) {
                ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                ISpectrum consensusSpectrum1 = clusterToAdd.getClusteredSpectra().get(0);

                double similarityScore = similarityChecker.assessSimilarity(consensusSpectrum, consensusSpectrum1);

                if (similarityScore >= similarityChecker.getDefaultThreshold() && similarityScore > highestSimilarityScore) {
                    highestSimilarityScore = similarityScore;
                    mostSimilarCluster = cluster;
                }
            }

            // add to cluster
            if (mostSimilarCluster != null) {
                ISpectrum[] clusteredSpectra = new ISpectrum[clusterToAdd.getClusteredSpectra().size()];
                mostSimilarCluster.addSpectra(clusterToAdd.getClusteredSpectra().toArray(clusteredSpectra));
            } else {
                clusters.add(clusterToAdd);
            }
        }

        clustersToAdd.clear();
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean mergeClusters() {
        guaranteeClean();

        // merge clusters
        boolean merged = mergeAllClusters();

        // remove none fitting spectra
        boolean noneFittingSpectraFound = demergeNoneFittingSpectra();

        // set dirty state
        setDirty(noneFittingSpectraFound);

        return merged || noneFittingSpectraFound;
    }


    /**
     * Merge the clusters only, all the merged cluster
     *
     * @return true if clusters have been merged
     */
    private boolean mergeAllClusters() {
        boolean modified = false;
        boolean toMerge = true;

        while (toMerge) {
            toMerge = false;
            List<ISpectralCluster> clustersToRemove = new ArrayList<ISpectralCluster>();
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    ISpectralCluster clusterI = clusters.get(i);
                    ISpectralCluster clusterJ = clusters.get(j);
                    double similarityScore = similarityChecker.assessSimilarity(clusterI.getConsensusSpectrum(), clusterJ.getConsensusSpectrum());
                    if (similarityScore >= similarityChecker.getDefaultThreshold()) {
                        toMerge = true;
                        modified = true;
                        ISpectrum[] clusteredSpectra = new ISpectrum[clusterI.getClusteredSpectra().size()];
                        clusterJ.addSpectra(clusterI.getClusteredSpectra().toArray(clusteredSpectra));
                        clustersToRemove.add(clusterI);
                        break;
                    }
                }
            }
            clusters.removeAll(clustersToRemove);
        }

        return modified;
    }

    /**
     * Remove none fitting spectra and the none fitting spectra
     * back in as new clusters
     *
     * @return true if spectra have been removed
     */
    private boolean demergeNoneFittingSpectra() {
        boolean noneFittingSpectraFound = false;

        List<ISpectralCluster> emptyClusters = new ArrayList<ISpectralCluster>(); // holder for any empty clusters

        for (ISpectralCluster cluster : clusters) {
            List<ISpectrum> noneFittingSpectra = findNoneFittingSpectra(cluster);
            if (!noneFittingSpectra.isEmpty()) {
                noneFittingSpectraFound = true;

                ISpectrum[] spectraToRemove = new ISpectrum[noneFittingSpectra.size()];
                if (cluster.getClusteredSpectraCount() == spectraToRemove.length) {
                    noneFittingSpectra = findNoneFittingSpectra(cluster);
                    emptyClusters.add(cluster); // nothing left remember this cluster
                }
                cluster.removeSpectra(noneFittingSpectra.toArray(spectraToRemove));

                for (ISpectrum noneFittingSpectrum : noneFittingSpectra) {
                    clustersToAdd.add(noneFittingSpectrum.asCluster());
                }
            }
        }
        if (!emptyClusters.isEmpty())    // any empty clusters
            clusters.removeAll(emptyClusters);   // drop them

        return noneFittingSpectraFound;
    }

    private List<ISpectrum> findNoneFittingSpectra(ISpectralCluster cluster) {
        List<ISpectrum> noneFittingSpectra = new ArrayList<ISpectrum>();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                double similarityScore = similarityChecker.assessSimilarity(cluster.getConsensusSpectrum(), spectrum);
                if (similarityScore < similarityChecker.getDefaultThreshold()) {
                    noneFittingSpectra.add(spectrum);
                }
            }
        }

        return noneFittingSpectra;
    }


}
