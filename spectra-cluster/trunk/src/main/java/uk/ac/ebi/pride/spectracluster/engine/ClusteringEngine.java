package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Default implementation of the clustering engine
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngine implements IClusteringEngine {


    public static IClusteringEngineFactory getClusteringEngineFactory() {
        return getClusteringEngineFactory(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }

    public static IClusteringEngineFactory getClusteringEngineFactory(SimilarityChecker similarityChecker,
                                                                      Comparator<ISpectralCluster> spectrumComparator) {
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }

    protected static class ClusteringEngineFactory implements IClusteringEngineFactory {
        private final SimilarityChecker similarityChecker;
        private final Comparator<ISpectralCluster> spectrumComparator;

        public ClusteringEngineFactory(final SimilarityChecker pSimilarityChecker, final Comparator<ISpectralCluster> pSpectrumComparator) {
            similarityChecker = pSimilarityChecker;
            spectrumComparator = pSpectrumComparator;
        }

        /**
         * make a copy of the clustering engine
         *
         * @return
         */
        @Override
        public IClusteringEngine getClusteringEngine(Object... otherdata) {
            return new ClusteringEngine(similarityChecker, spectrumComparator);
        }
    }


    private boolean dirty;
    private String name;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    private final List<ISpectralCluster> clustersToAdd = new ArrayList<ISpectralCluster>();
    private final List<IProgressHandler> progressHandlers = new ArrayList<IProgressHandler>();
    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;

    protected ClusteringEngine(SimilarityChecker similarityChecker,
                               Comparator<ISpectralCluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
    }

    protected void guaranteeClean() {
        if (isDirty()) {
            filterClustersToAdd();
            List<ISpectralCluster> myClustersToAdd = getClustersToAdd();
            Collections.sort(myClustersToAdd, getSpectrumComparator());
            addToClusters();
            setDirty(false);
        }
    }

    /**
     * Remove clusters which are size zero
     */
    protected void filterClustersToAdd() {
        List<ISpectralCluster> myClustersToAdd = getClustersToAdd();
        List<ISpectralCluster> l2 = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster sc : myClustersToAdd) {
            if (sc.getClusteredSpectraCount() > 0)
                l2.add(sc);
            else
                //noinspection UnusedAssignment
                sc = null; // break point here for debugging
        }
        myClustersToAdd.clear();
        myClustersToAdd.addAll(l2);
    }

    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        guaranteeClean();
        final ArrayList<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(clusters);
        Collections.sort(ret);
        return ret;
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(ISpectralCluster... cluster) {
        List<ISpectralCluster> myClustersToAdd = getClustersToAdd();
        if (cluster != null) {
            myClustersToAdd.addAll(Arrays.asList(cluster));
            setDirty(true);
        }

    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {
        name = pName;
    }

    /**
     * this method is called by guaranteeClean to place any added clusters in play
     * for further clustering
     */
    protected void addToClusters() {
        List<ISpectralCluster> myClustersToAdd = getClustersToAdd();
        List<ISpectralCluster> myClusters = internalGetClusters();
        SimilarityChecker sCheck = getSimilarityChecker();

        for (ISpectralCluster clusterToAdd : myClustersToAdd) {

            ISpectralCluster mostSimilarCluster = null;
            double highestSimilarityScore = 0;

            // find the cluster with the highest similarity score
            for (ISpectralCluster cluster : myClusters) {
                ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                ISpectrum consensusSpectrum1 = clusterToAdd.getConsensusSpectrum();  // subspectra are really only one spectrum clusters

                double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);

                if (similarityScore >= sCheck.getDefaultThreshold() && similarityScore > highestSimilarityScore) {
                    highestSimilarityScore = similarityScore;
                    mostSimilarCluster = cluster;
                }
            }

            // add to cluster
            if (mostSimilarCluster != null) {
                ISpectrum[] clusteredSpectra = new ISpectrum[clusterToAdd.getClusteredSpectra().size()];
                mostSimilarCluster.addSpectra(clusterToAdd.getClusteredSpectra().toArray(clusteredSpectra));
            } else {
                myClusters.add(new SpectralCluster(clusterToAdd));
            }
        }

        myClustersToAdd.clear();
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        guaranteeClean();

        if (size() < 2)
            return false; // nothing to do

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
    public boolean mergeAllClusters() {
        boolean modified = false;
        boolean toMerge = true;
        List<ISpectralCluster> myClusters = internalGetClusters();
        SimilarityChecker sCheck = getSimilarityChecker();

        while (toMerge) {
            toMerge = false;
            List<ISpectralCluster> clustersToRemove = new ArrayList<ISpectralCluster>();
            for (int i = 0; i < myClusters.size(); i++) {
                for (int j = i + 1; j < myClusters.size(); j++) {
                    ISpectralCluster clusterI = myClusters.get(i);
                    ISpectralCluster clusterJ = myClusters.get(j);
                    double similarityScore = sCheck.assessSimilarity(clusterI.getConsensusSpectrum(), clusterJ.getConsensusSpectrum());
                    if (similarityScore >= sCheck.getDefaultThreshold()) {
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
    protected boolean demergeNoneFittingSpectra() {
        boolean noneFittingSpectraFound = false;

        List<ISpectralCluster> emptyClusters = new ArrayList<ISpectralCluster>(); // holder for any empty clusters
        List<ISpectralCluster> myClusters = internalGetClusters();

        for (ISpectralCluster cluster : myClusters) {
            List<ISpectralCluster> noneFittingSpectra = findNoneFittingSpectra(cluster);
            if (!noneFittingSpectra.isEmpty()) {
                noneFittingSpectraFound = true;

                List<ISpectrum> holder = new ArrayList<ISpectrum>();
                for (ISpectralCluster removedCluster : noneFittingSpectra) {
                    holder.addAll(removedCluster.getClusteredSpectra());
                    clustersToAdd.add(removedCluster);
                }

                ISpectrum[] spectraToRemove = holder.toArray(new ISpectrum[holder.size()]);
                cluster.removeSpectra(spectraToRemove);

                if (cluster.getClusteredSpectraCount() == 0) {
                    emptyClusters.add(cluster); // nothing left remember this cluster
                }
            }
        }
        if (!emptyClusters.isEmpty())    // any empty clusters
            clusters.removeAll(emptyClusters);   // drop them

        return noneFittingSpectraFound;
    }


    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(ISpectralCluster cluster) {
        List<ISpectralCluster> noneFittingSpectra = new ArrayList<ISpectralCluster>();
        SimilarityChecker sCheck = getSimilarityChecker();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = sCheck.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = sCheck.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(ClusterUtilities.asCluster(spectrum));
                }
            }
        }

        return noneFittingSpectra;
    }


    /**
     * allow nonfitting spectra to leave and return a list of clusters to write out
     *
     * @param cluster
     * @return !null List<ISpectralCluster
     */
    @Nonnull
    @Override
    public List<ISpectralCluster> asWritttenSpectra(@Nonnull ISpectralCluster cluster) {
        return ClusteringUtilities.asWritttenSpectra(cluster, this);
    }

    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(IProgressHandler handler) {
        progressHandlers.add(handler);

    }

    /**
     * used to expose internals for overridig classes only
     *
     * @return
     */
    protected List<ISpectralCluster> getClustersToAdd() {
        return clustersToAdd;
    }

    /**
     * used to expose internals for overridig classes only
     *
     * @return
     */
    protected List<ISpectralCluster> internalGetClusters() {
        return clusters;
    }

    /**
     * used to expose internals for overridig classes only
     *
     * @return
     */
    protected SimilarityChecker getSimilarityChecker() {
        return similarityChecker;
    }

    /**
     * used to expose internals for overridig classes only
     *
     * @return
     */
    protected Comparator<ISpectralCluster> getSpectrumComparator() {
        return spectrumComparator;
    }

    /**
     * allow engines to be named
     *
     * @return
     */
    @Override
    public String toString() {
        int nClusters = size();
        if (name != null)
            return name + " with " + nClusters;
        return super.toString();
    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return clustersToAdd.size() + clusters.size();
    }
}
