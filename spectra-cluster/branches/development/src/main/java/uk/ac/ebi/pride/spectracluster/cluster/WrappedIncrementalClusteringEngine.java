package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.WrappedIncrementalClusteringEngine
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
                                                                      Comparator<ISpectralCluster> spectrumComparator) {
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }

    protected static class ClusteringEngineFactory implements IClusteringEngineFactory {
          private final IClusteringEngineFactory incrementalFactory;

        public ClusteringEngineFactory(final SimilarityChecker pSimilarityChecker, final Comparator<ISpectralCluster> pSpectrumComparator) {
                incrementalFactory = IncrementalClusteringEngine.getClusteringEngineFactory(pSimilarityChecker, pSpectrumComparator);
        }

        /**
         * make a copy of the clustering engine
         *
         * @return
         */
        @Override
        public IClusteringEngine getClusteringEngine() {
            return new WrappedIncrementalClusteringEngine((IIncrementalClusteringEngine)incrementalFactory.getClusteringEngine());
        }
    }


    private boolean dirty;
    private final IIncrementalClusteringEngine realEngine;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();

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
     * @return
     */
      protected List<ISpectralCluster> internalGetClusters() {
           return clusters;
    }

    /**
     * Get clustered clusters sorted by MZ is useful
     *
     * @return !null list this will be sorted by mz a include clusters of all sizes
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        final List<ISpectralCluster> internalClusters = internalGetClusters();
        Set<ISpectralCluster> internalSet = new HashSet<ISpectralCluster>(internalClusters) ;
        IIncrementalClusteringEngine engine = getRealEngine() ;
        final List<ISpectralCluster> lastClusters = engine.getClusters();
        internalSet.addAll(lastClusters);
        List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(internalSet);
         Collections.sort(ret);
        return ret;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(final ISpectralCluster... cluster) {
        final List<ISpectralCluster> internalClusters = internalGetClusters();
        IIncrementalClusteringEngine engine = getRealEngine() ;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < cluster.length; i++) {
            ISpectralCluster added = cluster[i];
            final List<ISpectralCluster> finalClusters = engine.addClusterIncremental(added);
            internalClusters.addAll(finalClusters);
        }
        setDirty(true);
    }

    /**
     * clusters are merged in the internal collection
     *   processing is incremental
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        boolean wasDirty = isDirty();
        setDirty(false);
        return wasDirty;
     }


    @SuppressWarnings("UnusedDeclaration")
    protected List<ISpectrum> findNoneFittingSpectra(ISpectralCluster cluster) {
        List<ISpectrum> noneFittingSpectra = new ArrayList<ISpectrum>();
        SimilarityChecker sCheck = ((IncrementalClusteringEngine)getRealEngine()).getSimilarityChecker();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = sCheck.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = sCheck.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(spectrum);
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
