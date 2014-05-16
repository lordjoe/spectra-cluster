package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.PeptideSpectralCluster;
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
                                                                      Comparator<IPeptideSpectralCluster> spectrumComparator) {
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }

    protected static class ClusteringEngineFactory implements IClusteringEngineFactory {
        private final SimilarityChecker similarityChecker;
        private final Comparator<IPeptideSpectralCluster> spectrumComparator;

        public ClusteringEngineFactory(final SimilarityChecker pSimilarityChecker, final Comparator<IPeptideSpectralCluster> pSpectrumComparator) {
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
    private final List<IPeptideSpectralCluster> clusters = new ArrayList<IPeptideSpectralCluster>();
    private final List<IPeptideSpectralCluster> clustersToAdd = new ArrayList<IPeptideSpectralCluster>();
    private final List<IProgressHandler> progressHandlers = new ArrayList<IProgressHandler>();
    private final SimilarityChecker similarityChecker;
    private final Comparator<IPeptideSpectralCluster> spectrumComparator;

    protected ClusteringEngine(SimilarityChecker similarityChecker,
                               Comparator<IPeptideSpectralCluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
    }

    protected void guaranteeClean() {
        if (isDirty()) {
            filterClustersToAdd();
            List<IPeptideSpectralCluster> myClustersToAdd = getClustersToAdd();
            Collections.sort(myClustersToAdd, getSpectrumComparator());
            addToClusters();
            setDirty(false);
        }
    }

    /**
     * Remove clusters which are size zero
     */
    protected void filterClustersToAdd() {
        List<IPeptideSpectralCluster> myClustersToAdd = getClustersToAdd();
        List<IPeptideSpectralCluster> l2 = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster sc : myClustersToAdd) {
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
    public List<IPeptideSpectralCluster> getClusters() {
        guaranteeClean();
        final ArrayList<IPeptideSpectralCluster> ret = new ArrayList<IPeptideSpectralCluster>(clusters);
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
    public void addClusters(IPeptideSpectralCluster... cluster) {
        List<IPeptideSpectralCluster> myClustersToAdd = getClustersToAdd();
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
        List<IPeptideSpectralCluster> myClustersToAdd = getClustersToAdd();
        List<IPeptideSpectralCluster> myClusters = internalGetClusters();
        SimilarityChecker sCheck = getSimilarityChecker();

        for (IPeptideSpectralCluster clusterToAdd : myClustersToAdd) {

            IPeptideSpectralCluster mostSimilarCluster = null;
            double highestSimilarityScore = 0;

            // find the cluster with the highest similarity score
            for (IPeptideSpectralCluster cluster : myClusters) {
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
                myClusters.add(new PeptideSpectralCluster(clusterToAdd));
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
        List<IPeptideSpectralCluster> myClusters = internalGetClusters();
        SimilarityChecker sCheck = getSimilarityChecker();

        while (toMerge) {
            toMerge = false;
            List<IPeptideSpectralCluster> clustersToRemove = new ArrayList<IPeptideSpectralCluster>();
            for (int i = 0; i < myClusters.size(); i++) {
                for (int j = i + 1; j < myClusters.size(); j++) {
                    IPeptideSpectralCluster clusterI = myClusters.get(i);
                    IPeptideSpectralCluster clusterJ = myClusters.get(j);
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

        List<IPeptideSpectralCluster> emptyClusters = new ArrayList<IPeptideSpectralCluster>(); // holder for any empty clusters
        List<IPeptideSpectralCluster> myClusters = internalGetClusters();

        for (IPeptideSpectralCluster cluster : myClusters) {
            List<IPeptideSpectralCluster> noneFittingSpectra = findNoneFittingSpectra(cluster);
            if (!noneFittingSpectra.isEmpty()) {
                noneFittingSpectraFound = true;

                List<ISpectrum> holder = new ArrayList<ISpectrum>();
                for (IPeptideSpectralCluster removedCluster : noneFittingSpectra) {
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
    public List<IPeptideSpectralCluster> findNoneFittingSpectra(IPeptideSpectralCluster cluster) {
        List<IPeptideSpectralCluster> noneFittingSpectra = new ArrayList<IPeptideSpectralCluster>();
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
    public List<IPeptideSpectralCluster> asWritttenSpectra(@Nonnull IPeptideSpectralCluster cluster) {
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
    protected List<IPeptideSpectralCluster> getClustersToAdd() {
        return clustersToAdd;
    }

    /**
     * used to expose internals for overridig classes only
     *
     * @return
     */
    protected List<IPeptideSpectralCluster> internalGetClusters() {
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
    protected Comparator<IPeptideSpectralCluster> getSpectrumComparator() {
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
