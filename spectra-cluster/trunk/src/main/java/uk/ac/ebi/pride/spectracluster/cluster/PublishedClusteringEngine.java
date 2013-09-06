package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.QualityScorer;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.QualityClusterComparator;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 9/6/13
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublishedClusteringEngine implements IClusteringEngine {
    private static final String algorithmName = "PRIDE Cluster v1 (re-impl.)";
    private static final SimilarityChecker spectrumSimilarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
    private static final QualityScorer qualityScorer = Defaults.INSTANCE.getDefaultQualityScorer();

    private static final double SIMILARIY_THRESHOLD = 0.7;

    /**
     * This specific implementation of ISpectralCluster is used since it uses the original
     * consensus spectrum building code. Any other implementation might change the final
     * clustering result.
     */
    List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    /**
     * Holds all clusters that were not processed yet.
     */
    List<ISpectralCluster> clustersToAdd = new ArrayList<ISpectralCluster>();

    @Override
    public List<ISpectralCluster> getClusters() {
        //guaranteeClean();

        // TODO @Rui / Steve: I'm not quite sure how we should react to calls to this function if new cluster were added
        //               and processCluster wasn't called yet. "Correct" clustering results currently depend on
        //               "processClusters" to be called N often (4 at the moment). Only then, the results can be considered
        //               clean. If we follow the approach that getClusters only returns result once this has been done
        //               I'd believe that "processClusters" should be set protected as then the ClusterinEngine is taking
        //               care of all required steps (in my opinion the best solution).

        final ArrayList<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(clusters);
        Collections.sort(ret);
        return ret;
    }

    @Override
    public void addClusters(ISpectralCluster... cluster) {
        if (cluster != null) {
            // TODO: @jg/steve/rui normalize the spectra or are spectra already normalized before somewhere?
            clustersToAdd.addAll(Arrays.asList(cluster));
        }
    }

    @Override
    public boolean processClusters() {
        boolean dataChanged = false;

        // Step 1 - cluster
        long start = System.currentTimeMillis();
        boolean clustersAdded = processClusterToAdd();
        dataChanged = dataChanged || clustersAdded;
        long durCluster1 = System.currentTimeMillis() - start;

        // Step 2 - merge
        int clustersMerged = mergeClusters();
        dataChanged = dataChanged || clustersMerged > 0;
        long durMerge = System.currentTimeMillis() - start - durCluster1;

        // Step 3- remove non-fitting peptides
        int nSpectraRemoved = removeNonFittingSpectra();
        dataChanged = dataChanged || nSpectraRemoved > 0;
        long durRemove = System.currentTimeMillis() - start - durCluster1 - durMerge;

        // Step 4 - cluster non-fitting peptides again, repeat at step 2
        clustersAdded = processClusterToAdd();
        dataChanged = dataChanged || clustersAdded;
        long durCluster2 = System.currentTimeMillis() - start - durCluster1 - durMerge - durRemove;

        System.out.format("Cluster1 = %d\t\tMerge = %d (%d merged)\t\tRemove = %d (%d removed)\t\tCluster2 = %d\n",
                durCluster1, durMerge, clustersMerged, durRemove, nSpectraRemoved, durCluster2);

        return dataChanged;
    }

    /**
     * Removes all spectra from the current existing cluster that no
     * longer match the required threshold and adds them to clusterToAdd
     * @return Number of spectra removed from clusters
     */
    private int removeNonFittingSpectra() {
        int spectraRemoved = 0;

        for (ISpectralCluster currentCluster : clusters) {
            if (currentCluster.getClusteredSpectraCount() < 2)
                continue;

            List<ISpectrum> nonFittingSpectra = new ArrayList<ISpectrum>();

            for (ISpectrum spectrum : currentCluster.getClusteredSpectra()) {
                double similarity = spectrumSimilarityChecker.assessSimilarity(currentCluster.getConsensusSpectrum(), spectrum);

                if (similarity < SIMILARIY_THRESHOLD)
                    nonFittingSpectra.add(spectrum);
            }

            currentCluster.removeSpectra(nonFittingSpectra.toArray(new ISpectrum[nonFittingSpectra.size()]));
            clustersToAdd.addAll(ClusterUtilities.asClusters(nonFittingSpectra));
            spectraRemoved += nonFittingSpectra.size();
        }

        // remove empty clusters
        List<ISpectralCluster> tmp = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster c : clusters) {
            if (c.getClusteredSpectraCount() > 0)
                tmp.add(c);
        }
        clusters.clear();
        clusters.addAll(tmp);

        return spectraRemoved;
    }

    /**
     * Merge all clusters stored in cluster.
     * @return Indicates whether clusters were merged
     */
    private int mergeClusters() {
        Set<Integer> deletedClusters = new HashSet<Integer>();
        Collections.sort(clusters); // TODO @jg: make sure this search is sensible

        boolean clusterMerged = true;
        int nMerged = 0;

        while (clusterMerged) {
            clusterMerged = false;

            for (int i = 0; i < clusters.size(); i++) {
                if (deletedClusters.contains(i))
                    continue;

                ISpectralCluster cluster1 = clusters.get(i);
                ISpectrum spectrum1 = cluster1.getClusteredSpectraCount() > 1 ?
                        cluster1.getConsensusSpectrum() : cluster1.getHighestQualitySpectrum();

                for (int j = 0; j < clusters.size(); j++) {
                    if (deletedClusters.contains(j) || j == i)
                        continue;

                    ISpectralCluster cluster2 = clusters.get(j);
                    ISpectrum spectrum2 = cluster2.getClusteredSpectraCount() > 1 ?
                            cluster2.getConsensusSpectrum() : cluster2.getHighestQualitySpectrum();

                    double similarity = spectrumSimilarityChecker.assessSimilarity(spectrum1, spectrum2);

                    if (similarity >= SIMILARIY_THRESHOLD) {
                        cluster1.addSpectra(cluster2.getClusteredSpectra());
                        deletedClusters.add(j);
                        clusterMerged = true;
                        nMerged++;
                    }
                }
            }
        }

        // remove all deleted clusters
        List<ISpectralCluster> tmp = new ArrayList<ISpectralCluster>();
        for (int i = 0; i < clusters.size(); i++) {
            if (deletedClusters.contains(i))
                continue;
            tmp.add(clusters.get(i));
        }
        clusters.clear();
        clusters.addAll(tmp);

        return nMerged;
    }

    /**
     * Processes all clusters in clusterToAdd and clusters them
     * with the clusters in cluster.
     * @return Indicates if something changed
     */
    private boolean processClusterToAdd() {
        if (clustersToAdd.size() < 1)
            return false;

        boolean spectraClustered = false;
        Collections.sort(clustersToAdd, QualityClusterComparator.INSTANCE);

        for (ISpectralCluster clusterToAdd : clustersToAdd) {
            ISpectralCluster mostSimilarCluster = null;
            double highestSimilarity = 0;
            // TODO: @Rui/Steve: it makes quite a difference whether we only use consensus spectra for comparison or for single spectra cluster the actual unchanged spectrum (the way it was done in the original algorithm and by Frank et al.)
            ISpectrum spectrumToAssess = clusterToAdd.getClusteredSpectraCount() > 1 ?
                    clusterToAdd.getConsensusSpectrum() : clusterToAdd.getClusteredSpectra().get(0);

            for (ISpectralCluster existingCluster : clusters) {
                double similarity = spectrumSimilarityChecker.assessSimilarity(spectrumToAssess, existingCluster.getConsensusSpectrum());

                if (similarity >= SIMILARIY_THRESHOLD && similarity > highestSimilarity) {
                    highestSimilarity = similarity;
                    mostSimilarCluster = existingCluster;
                }
            }

            if (mostSimilarCluster != null) {
                mostSimilarCluster.addSpectra(clusterToAdd.getClusteredSpectra());
                spectraClustered = true;
            }
            else {
                clusters.add(new SpectralCluster(clusterToAdd));
            }
        }

        clustersToAdd.clear();
        return spectraClustered;
    }

    @Override
    public String getName() {
        return algorithmName;
    }

    @Override
    public void setName(String pName) {
        // This should not be used, as far as I understand it, the name represents the algorithm and shouldn't be changeable
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return clusters.size();
    }
}
