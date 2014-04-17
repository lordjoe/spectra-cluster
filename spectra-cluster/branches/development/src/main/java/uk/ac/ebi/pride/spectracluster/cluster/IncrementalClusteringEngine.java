package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

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
            return new IncrementalClusteringEngine(similarityChecker, spectrumComparator, windowSize);
        }
    }


    private String name;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
    private final List<IProgressHandler> progressHandlers = new ArrayList<IProgressHandler>();
    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;
    private final double windowSize;
    private int currentMZAsInt;

    protected IncrementalClusteringEngine(SimilarityChecker sck,
                                          Comparator<ISpectralCluster> scm, double windowSize) {
        this.similarityChecker = sck;
        this.spectrumComparator = scm;
        this.windowSize = windowSize;
    }

    public double getWindowSize() {
        return windowSize;
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

                if (compareCount++ % PROGRESS_SHOW_INTERVAL == 0)
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
    protected void showProgress() {
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
        double defaultThreshold1 = getWindowSize();
        double lowestMZ = precursorMz - defaultThreshold1;
        List<ISpectralCluster> clustersToremove = new ArrayList<ISpectralCluster>();
        List<ISpectralCluster> myClusters = internalGetClusters();
        for (ISpectralCluster test : myClusters) {
            float testPrecursorMz = test.getPrecursorMz();
            if (lowestMZ > testPrecursorMz) {
                clustersToremove.add(test);
            }
        }
        if (!clustersToremove.isEmpty())
            internalGetClusters().removeAll(clustersToremove);   // might break hear


        setCurrentMZ(precursorMz);
        return clustersToremove;

    }


    /**
     * this method is called by guaranteeClean to place any added clusters in play
     * for further clustering
     */
    protected void addToClusters(final ISpectralCluster clusterToAdd) {
        List<ISpectralCluster> myClusters = internalGetClusters();
        if (myClusters.isEmpty()) {   // no checks just add
            myClusters.add(new SpectralCluster(clusterToAdd));
            numberNotMerge++;
            return;
        }

        if (handleFullContainment(clusterToAdd ))
            return; // no need to add we are contained


        SimilarityChecker sCheck = getSimilarityChecker();
        List<ISpectrum> clusteredSpectra1 = clusterToAdd.getClusteredSpectra();

        ISpectrum qc = clusterToAdd.getHighestQualitySpectrum();
        String mostCommonPeptide = "";
        if (qc instanceof IPeptideSpectrumMatch)
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            mostCommonPeptide = ((IPeptideSpectrumMatch) qc).getPeptide();

        ISpectralCluster bestMatch = null;
        double highestSimilarityScore = 0;
        int compareCount = 0;

        ISpectralCluster mostSimilarCluster = null;
        ISpectrum consensusSpectrum1 = clusterToAdd.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
        // find the cluster with the highest similarity score
        for (ISpectralCluster cluster : myClusters) {
            ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();

            double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);

            if (similarityScore > highestSimilarityScore) {
                highestSimilarityScore = similarityScore;
                bestMatch = cluster; //    track good but not great
                if (similarityScore >= sCheck.getDefaultThreshold()) {
                    mostSimilarCluster = bestMatch;
                }

            }
            if (compareCount++ % PROGRESS_SHOW_INTERVAL == 0)
                showProgress();

        }


        // add to cluster
        if (mostSimilarCluster != null) {
            ISpectrum[] clusteredSpectra = new ISpectrum[clusteredSpectra1.size()];
            final ISpectrum[] merged = clusteredSpectra1.toArray(clusteredSpectra);
            mostSimilarCluster.addSpectra(merged);
            numberGoodMerge++;
            return;
        }

        // maybe a lot of overlap here
        if (bestMatch != null) {
            if (handlePotentialOverlap(clusterToAdd, bestMatch, highestSimilarityScore))
                return;
        }
        myClusters.add(new SpectralCluster(clusterToAdd));
        numberNotMerge++;
    }

    public static final double MINIMUM_MERGE_SCORE = 0.5;
    /**
     *   figure out is
     * @param clusterToAdd
     * @return  true is we fully replace a cluster with a larger or find this fully contained
     */
    protected boolean handleFullContainment(final ISpectralCluster clusterToAdd) {
        final List<ISpectralCluster> myclusters = internalGetClusters();
        ISpectralCluster toReplace = null;
        double bestSimilarity = Double.MIN_VALUE;
        for (ISpectralCluster myCluster : myclusters) {
            double score = ClusterSimilarityUtilities.clusterFullyContainsScore(myCluster, clusterToAdd);
            if(score > bestSimilarity) {
                bestSimilarity = score;
                toReplace = myCluster;
                if(score == 1)
                    break;
              }
         }

        if (bestSimilarity >= MINIMUM_MERGE_SCORE ) {
            mergeIntoCluster(clusterToAdd, toReplace);
            return true; // done
        }
        return false;
    }

    public static final double MINIMUM_SIMILARITY_SCORE_FOR_OVERLAP = 0.2;
    public static final double BONUS_PER_OVERLAP = 0.05;

    /**
     * if there are overlapping spectra among the current cluster and the best match
     * then  firure out what is best
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    protected boolean handlePotentialOverlap(final ISpectralCluster cluster1, final ISpectralCluster cluster2, double highestSimilarityScore) {
        if (highestSimilarityScore < MINIMUM_SIMILARITY_SCORE_FOR_OVERLAP)
            return false;     // we did nothing
        Set<String> ids = cluster1.getSpectralIds();
        int numberIds = ids.size();
        Set<String> best = cluster2.getSpectralIds();
        Set<String> spectraOverlap = ClusterSimilarityUtilities.getSpectraOverlap(ids, cluster2);
        int numberOverlap = spectraOverlap.size();
        if (numberOverlap == 0)
            return false; // no overlap
        int minClusterSize = Math.min(best.size(), ids.size());

        // of a lot of overlap then force a merge
        if (numberOverlap >= minClusterSize / 2) {  // enough overlap then merge
            mergeIntoCluster(cluster1, cluster2);
            return true;
        }
        // allow a bonus for overlap
        SimilarityChecker sCheck = getSimilarityChecker();
        if (highestSimilarityScore + BONUS_PER_OVERLAP > sCheck.getDefaultThreshold()) {
            mergeIntoCluster(cluster1, cluster2);
            return true;

        }


        // force overlappping spectra into the best cluster
        return assignOverlapsToBestCluster(cluster1, cluster2, spectraOverlap);


    }

    protected void mergeIntoCluster(final ISpectralCluster mergeFrom, final ISpectralCluster mergeInto) {
        List<ISpectrum> clusteredSpectra1 = mergeFrom.getClusteredSpectra();
        ISpectrum[] clusteredSpectra = new ISpectrum[clusteredSpectra1.size()];
        final ISpectrum[] merged = clusteredSpectra1.toArray(clusteredSpectra);
        mergeInto.addSpectra(merged);
        numberLessGoodMerge++;
    }

    /**
     * this assigns
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    protected boolean assignOverlapsToBestCluster(final ISpectralCluster cluster1, final ISpectralCluster cluster2, Set<String> spectraOverlap) {
        List<ISpectrum> clusteredSpectra1 = cluster1.getClusteredSpectra();
        // I am not sure here but I think we let duplicates move to the best cluster
        SimilarityChecker sCheck = getSimilarityChecker();
        ISpectrum cs1 = cluster1.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
        ISpectrum cs2 = cluster2.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
        for (ISpectrum spc : clusteredSpectra1) {
            if (!spectraOverlap.contains(spc.getId()))
                continue; // not an overlapping spectrum
            // choose the better cluster
            double ss1 = sCheck.assessSimilarity(cs1, spc);
            double ss2 = sCheck.assessSimilarity(cs2, spc);
            if (ss1 > ss2)
                cluster1.removeSpectra(spc);
            else
                cluster2.removeSpectra(spc);
            numberReAsssigned++;
        }

        return false;
    }


    public static int numberOverlap = 0;
    public static int numberNotMerge = 0;
    public static int numberGoodMerge = 0;
    public static int numberLessGoodMerge = 0;
    public static int numberReAsssigned = 0;


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
