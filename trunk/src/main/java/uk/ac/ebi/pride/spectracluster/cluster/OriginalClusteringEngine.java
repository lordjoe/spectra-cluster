package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * User: jg
 * Date: 7/30/13
 * Time: 2:29 PM
  */
public class OriginalClusteringEngine implements IClusteringEngine {
    private static final String algorithmName = "FrankEtAlClustering2";
    /**
     * Uses the original version of the similarity checker.
     */
    private static final SimilarityChecker spectrumSimilarityChecker = new FrankEtAlDotProductOld();

    @SuppressWarnings("UnusedDeclaration")
    private static final QualityScorer qualityScorer = new SignalToNoiseChecker();

    private static final double SIMILARIY_THRESHOLD = 0.7;

    /**
     * This specific implementation of ISpectralCluster is used since it uses the original
     * consensus spectrum building code. Any other implementation might change the final
     * clustering result.
     */
    List<OriginalSpectralCluster> cluster = new ArrayList<OriginalSpectralCluster>();
    /**
     * Holds all clusters that were not processed yet.
     */
    List<ISpectralCluster> clustersToAdd = new ArrayList<ISpectralCluster>();

    @Override
    public List<ISpectralCluster> getClusters() {
        return new ArrayList<ISpectralCluster>(cluster);
    }

    @Override
    public void addClusters(ISpectralCluster... cluster) {
        //noinspection ManualArrayToCollectionCopy
        for (ISpectralCluster c : cluster) {
            // TODO: @jg normalize the spectra
            clustersToAdd.add(c);
        }
    }

    @Override
    public boolean processClusters() {
        boolean dataChanged = false;

        // Step 1 - cluster
        long start = System.currentTimeMillis();
        boolean clustersAdded = processClusterToAdd();
        //noinspection ConstantConditions
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

  //      System.out.format("Cluster1 = %d\t\tMerge = %d (%d merged)\t\tRemove = %d (%d removed)\t\tCluster2 = %d\n",
   //             durCluster1, durMerge, clustersMerged, durRemove, nSpectraRemoved, durCluster2);

        return dataChanged;
    }

    /**
     * Removes all spectra from the current existing cluster that no
     * longer match the required threshold and adds them to clusterToAdd
     * @return Number of spectra removed from clusters
     */
    private int removeNonFittingSpectra() {
        int spectraRemoved = 0;

        for (OriginalSpectralCluster currentCluster : cluster) {
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
        List<OriginalSpectralCluster> tmp = new ArrayList<OriginalSpectralCluster>();
        for (OriginalSpectralCluster c : cluster) {
            if (c.getClusteredSpectraCount() > 0)
                tmp.add(c);
        }
        cluster.clear();
        cluster.addAll(tmp);

        return spectraRemoved;
    }

    /**
     * Merge all clusters stored in cluster.
     * @return Indicates whether clusters were merged
     */
    private int mergeClusters() {
        Set<Integer> deletedClusters = new HashSet<Integer>();
        Collections.sort(cluster);

        boolean clusterMerged = true;
        int nMerged = 0;

        while (clusterMerged) {
            clusterMerged = false;

            for (int i = 0; i < cluster.size(); i++) {
                if (deletedClusters.contains(i))
                    continue;

                OriginalSpectralCluster cluster1 = cluster.get(i);
                ISpectrum spectrum1 = cluster1.getClusteredSpectraCount() > 1 ?
                        cluster1.getConsensusSpectrum() : cluster1.getHighestQualitySpectrum();

                for (int j = 0; j < cluster.size(); j++) {
                    if (deletedClusters.contains(j) || j == i)
                        continue;

                    OriginalSpectralCluster cluster2 = cluster.get(j);
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
        List<OriginalSpectralCluster> tmp = new ArrayList<OriginalSpectralCluster>();
        for (int i = 0; i < cluster.size(); i++) {
            if (deletedClusters.contains(i))
                continue;
            tmp.add(cluster.get(i));
        }
        cluster.clear();
        cluster.addAll(tmp);

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
            OriginalSpectralCluster mostSimilarCluster = null;
            double highestSimilarity = 0;
            ISpectrum spectrumToAssess = clusterToAdd.getClusteredSpectraCount() > 1 ?
                            clusterToAdd.getConsensusSpectrum() : clusterToAdd.getHighestQualitySpectrum();

            for (OriginalSpectralCluster existingCluster : cluster) {
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
                cluster.add(new OriginalSpectralCluster(clusterToAdd));
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
        return cluster.size();
    }
}
