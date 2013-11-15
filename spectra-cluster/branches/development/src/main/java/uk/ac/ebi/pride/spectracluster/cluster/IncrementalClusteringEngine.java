package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

import com.lordjoe.utilities.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IncrementalClusteringEngine
 * a version of a clustering enging in which spectra are added incrementatlly and
 * clusters are shed when they are too far to use
 * <p/>
 * <p/>
 * User: Steve
 * Date: 7/5/13
 */
@SuppressWarnings("UnusedDeclaration")
public class IncrementalClusteringEngine implements IIncrementalClusteringEngine {


    public static final int PROGRESS_SHOW_INTERVAL = 100;

    @SuppressWarnings("UnusedDeclaration")
    public static IIncrementalClusteringEngineFactory getClusteringEngineFactory() {
        return getClusteringEngineFactory(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }

    public static IIncrementalClusteringEngineFactory getClusteringEngineFactory(SimilarityChecker similarityChecker,
                                                                                 Comparator<ISpectralCluster> spectrumComparator) {
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }

    protected static class ClusteringEngineFactory implements IIncrementalClusteringEngineFactory {
        private final SimilarityChecker similarityChecker;
        private final Comparator<ISpectralCluster> spectrumComparator;

        public ClusteringEngineFactory(final SimilarityChecker pSimilarityChecker, final Comparator<ISpectralCluster> pSpectrumComparator) {
            similarityChecker = pSimilarityChecker;
            spectrumComparator = pSpectrumComparator;
        }


        /**
         * build a new version
         *
         * @return
         */
        @Override
        public IIncrementalClusteringEngine getIncrementalClusteringEngine(double windowSize) {
            return new IncrementalClusteringEngine(similarityChecker, spectrumComparator,windowSize);
        }
    }


    private String name;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    private final List<IProgressHandler> progressHandlers = new ArrayList<IProgressHandler>();
    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;
    private final double defaultThreshold;
    private int currentMZAsInt;

    protected IncrementalClusteringEngine(SimilarityChecker sck,
                                          Comparator<ISpectralCluster> scm,double windowSize) {
        this.similarityChecker = sck;
        this.spectrumComparator = scm;
        defaultThreshold = windowSize;

    }

    public double getDefaultThreshold() {
        return defaultThreshold;
    }


    public int getCurrentMZ() {
        return currentMZAsInt;
    }


    public void setCurrentMZ(final double pCurrentMZ) {
        int test = ClusterUtilities.mzToInt(pCurrentMZ);
        if (getCurrentMZ() > test) {  // all ow roundoff error but not much
            double del = getCurrentMZ() - test;  // difference

            if (Math.abs(del) > ClusterUtilities.MZ_RESOLUTION * IPeak.SMALL_MZ_DIFFERENCE) {
                throw new IllegalStateException("mz values MUST be added in order - was "
                        + Util.formatDouble(getCurrentMZ(), 3) + " new " +
                        Util.formatDouble(pCurrentMZ, 3) + " del  " +
                        del
                );

            }

        }
        currentMZAsInt = test;
    }

//    protected void guaranteeClean() {
//        if (isDirty()) {
//            filterClustersToAdd();
//            List<ISpectralCluster> myClustersToAdd = getClustersToAdd();
//            Collections.sort(myClustersToAdd, getSpectrumComparator());
//            addToClusters();
//            setDirty(false);
//        }
//    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        // guaranteeClean();        incremental is ALWAYS clean
        final ArrayList<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(clusters);
        Collections.sort(ret);
        return ret;
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    @Override
    public List<ISpectralCluster> findNoneFittingSpectra(final ISpectralCluster cluster) {
        List<ISpectralCluster> noneFittingSpectra = new ArrayList<ISpectralCluster>();
        SimilarityChecker sCheck = getSimilarityChecker();

        int compareCount = 0;

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = sCheck.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = sCheck.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(spectrum.asCluster());
                }

                if(compareCount++ % PROGRESS_SHOW_INTERVAL == 0)
                    showProgress();
            }
        }

        return noneFittingSpectra;
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
     * show some work is going on
      */
    protected void showProgress()
    {
        for (IProgressHandler pm : progressHandlers) {
               pm.incrementProgress(1);
        }
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(ISpectralCluster... cluster) {
        // or use a WrappedIncrementalClusteringEngine
        throw new UnsupportedOperationException("Use addClusterIncremental instead or use a WrappedIncrementalClusteringEngine ");

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
     * add one cluster and return any clusters which are too far in mz from further consideration
     *
     * @return !null Cluster
     */
    @Override
    public List<ISpectralCluster> addClusterIncremental(final ISpectralCluster added) {
        double precursorMz = added.getPrecursorMz();
        List<ISpectralCluster> clustersToremove = findClustersTooLow(precursorMz);
        // either add as an existing cluster if make a new cluster
        addToClusters(added);
        return clustersToremove;
    }

    /**
     * return a list of clusters whose mz is too low to mrege with the current cluster
     * these are dropped and will be handled as never modifies this pass
     *
     * @param precursorMz new Mz
     * @return !null list of clusters to remove
     */
    protected List<ISpectralCluster> findClustersTooLow(double precursorMz) {
        double oldMZ = getCurrentMZ();
        double lowestMZ = precursorMz - getDefaultThreshold();
        List<ISpectralCluster> clustersToremove = new ArrayList<ISpectralCluster>();
        List<ISpectralCluster> myClusters = internalGetClusters();
        for (ISpectralCluster test : myClusters) {
            if (lowestMZ > test.getPrecursorMz()) {
                clustersToremove.add(test);
            }
        }
        internalGetClusters().removeAll(clustersToremove);


        setCurrentMZ(precursorMz);
        return clustersToremove;

    }


    /**
     * this method is called by guaranteeClean to place any added clusters in play
     * for further clustering
     */
    protected void addToClusters(final ISpectralCluster clusterToAdd) {
        List<ISpectralCluster> myClusters = internalGetClusters();
        SimilarityChecker sCheck = getSimilarityChecker();

        double highestSimilarityScore = 0;
        int compareCount = 0;

        ISpectralCluster mostSimilarCluster = null;
        ISpectrum consensusSpectrum1 = clusterToAdd.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
        // find the cluster with the highest similarity score
        for (ISpectralCluster cluster : myClusters) {
            ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();

            double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);

            if (similarityScore >= sCheck.getDefaultThreshold() && similarityScore > highestSimilarityScore) {
                highestSimilarityScore = similarityScore;
                mostSimilarCluster = cluster;
            }
            if(compareCount++ % PROGRESS_SHOW_INTERVAL == 0)
                 showProgress();

        }

        // add to cluster
        if (mostSimilarCluster != null) {
            ISpectrum[] clusteredSpectra = new ISpectrum[clusterToAdd.getClusteredSpectra().size()];
            final ISpectrum[] merged = clusterToAdd.getClusteredSpectra().toArray(clusteredSpectra);
            mostSimilarCluster.addSpectra(merged);
        } else {
            myClusters.add(new SpectralCluster(clusterToAdd));
        }
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        throw new UnsupportedOperationException("Don\'t do this using an IncrementalClusteringEngine use a WrappedIncrementalClusteringEngine"); // ToDo
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
    @SuppressWarnings("UnusedDeclaration")
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
        return clusters.size();
    }
}
