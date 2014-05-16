package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.engine.WrappedIncrementalClusteringEngine
 * Wraps an  IIncrementalClusteringEngine to do one pass clustering
 * User: Steve
 * Date: 8/13/13
 */
public class WrappedIncrementalClusteringEngine implements IClusteringEngine {

    @SuppressWarnings("UnusedDeclaration")
    public static IClusteringEngineFactory getClusteringEngineFactory() {
        return getClusteringEngineFactory(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }

    public static IClusteringEngineFactory getClusteringEngineFactory(SimilarityChecker similarityChecker,
                                                                      Comparator<IPeptideSpectralCluster> spectrumComparator) {
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }

    protected static class ClusteringEngineFactory implements IClusteringEngineFactory {
        private final IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory incrementalFactory;

        public ClusteringEngineFactory(final SimilarityChecker pSimilarityChecker, final Comparator<IPeptideSpectralCluster> pSpectrumComparator) {
            incrementalFactory = IncrementalClusteringEngine.getClusteringEngineFactory(pSimilarityChecker, pSpectrumComparator);
        }

        /**
         * make a copy of the clustering engine
         *
         * @return
         */
        @Override
        public IClusteringEngine getClusteringEngine(Object... otherdata) {
            if (otherdata.length < 1)
                throw new IllegalArgumentException("WrappedClusteringEngine needs a Double as WindowSize"); //
            double windowSize = (Double) otherdata[0];
            return new WrappedIncrementalClusteringEngine(incrementalFactory.getIncrementalClusteringEngine(windowSize));
        }
    }


    private boolean dirty;
    private final IIncrementalClusteringEngine realEngine;
    private final List<IPeptideSpectralCluster> clusters = new ArrayList<IPeptideSpectralCluster>();

    public WrappedIncrementalClusteringEngine(final IIncrementalClusteringEngine pRealEngine) {
        realEngine = pRealEngine;
    }

    public IIncrementalClusteringEngine getRealEngine() {
        return realEngine;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(final boolean pDirty) {
        dirty = pDirty;
    }

    /**
     * simple get of raw clusters array for internal use
     *
     * @return
     */
    protected List<IPeptideSpectralCluster> internalGetClusters() {
        return clusters;
    }

    /**
     * Get clustered clusters sorted by MZ is useful
     *
     * @return !null list this will be sorted by mz a include clusters of all sizes
     */
    @Override
    public List<IPeptideSpectralCluster> getClusters() {
        final List<IPeptideSpectralCluster> internalClusters = internalGetClusters();
        Set<IPeptideSpectralCluster> internalSet = new HashSet<IPeptideSpectralCluster>(internalClusters);
        IIncrementalClusteringEngine engine = getRealEngine();
        final Collection<IPeptideSpectralCluster> lastClusters = engine.getClusters();
        internalSet.addAll(lastClusters);
        List<IPeptideSpectralCluster> ret = new ArrayList<IPeptideSpectralCluster>(internalSet);
        Collections.sort(ret);
        return ret;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(final IPeptideSpectralCluster... cluster) {
        final List<IPeptideSpectralCluster> internalClusters = internalGetClusters();
        IIncrementalClusteringEngine engine = getRealEngine();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < cluster.length; i++) {
            IPeptideSpectralCluster added = cluster[i];
            final Collection<IPeptideSpectralCluster> finalClusters = engine.addClusterIncremental(added);
            if (!finalClusters.isEmpty())
                internalClusters.addAll(finalClusters);
        }
        setDirty(true);
    }

    /**
     * clusters are merged in the internal collection
     * processing is incremental
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        boolean wasDirty = isDirty();
        setDirty(false);
        return wasDirty;
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    @Override
    public List<IPeptideSpectralCluster> findNoneFittingSpectra(final IPeptideSpectralCluster cluster) {
        return realEngine.findNoneFittingSpectra(cluster);
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
        return ClusteringUtilities.asWritttenSpectra(cluster, realEngine);
    }


    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(IProgressHandler handler) {
        realEngine.addProgressMonitor(handler);

    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return getRealEngine().getName();
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {
        getRealEngine().setName(pName);
    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return internalGetClusters().size();
    }
}
