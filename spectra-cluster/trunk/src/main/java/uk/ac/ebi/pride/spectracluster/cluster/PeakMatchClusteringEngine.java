package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.PeakMatchClusteringEngine
 * performs clustering by looking at major peaks then merging clusters
 * - the this version tracks spectra already clustered
 * Original code in  PeakMatchClusteringEngineOriginal
 * and attempts to combine clusters
 * <p/>
 * User: Steve
 * Date: 6/28/13
 */
public class PeakMatchClusteringEngine implements IClusteringEngine {


    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;
    private final List<ISpectralCluster> singleSpectrumClusters = new ArrayList<ISpectralCluster>();
    private final List<ISpectralCluster> currentClusters = new ArrayList<ISpectralCluster>();
    private final Set<ISpectrum> alreadyClustered = new HashSet<ISpectrum>();
    private final IClusteringEngineFactory factory;
    private String name = "PeakMatchClusteringEngine";

    public PeakMatchClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }


    @SuppressWarnings("UnusedDeclaration")
    public PeakMatchClusteringEngine(final Comparator<ISpectralCluster> spectrumComparator) {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker(), spectrumComparator);
    }


    @SuppressWarnings("UnusedDeclaration")
    public PeakMatchClusteringEngine(final SimilarityChecker similarityChecker) {
        this(similarityChecker, Defaults.INSTANCE.getDefaultSpectrumComparator());
    }


    public PeakMatchClusteringEngine(final SimilarityChecker similarityChecker, final Comparator<ISpectralCluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
        factory = ClusteringEngine.getClusteringEngineFactory(similarityChecker, spectrumComparator);
    }


    public SimilarityChecker getSimilarityChecker() {
        return similarityChecker;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(final ISpectralCluster... pClusters) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < pClusters.length; i++) {
            ISpectralCluster cluster = pClusters[i];
            if (cluster.getClusteredSpectraCount() == 1)
                singleSpectrumClusters.addAll(Arrays.asList(cluster));
            else
                currentClusters.add(cluster);

        }
    }


    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        if (currentClusters.isEmpty()) {
            return clusterUsingPeaks();
        } else {
            return mergeAndCombineClusters();
        }
    }

    /**
     * all subsequent passes - start  with a list if clusters and single spectrum clusters
     * merge the clusters considering only those close in mz
     *
     * @return true if anything done
     */
    protected boolean mergeAndCombineClusters() {
        int startingClusterCount = currentClusters.size() + singleSpectrumClusters.size();
        List<ISpectralCluster> singleSpectra = ClusterUtilities.removeSingleSpectrumClusters(currentClusters);
        List<ISpectralCluster> mergedClusters = mergeClusters(currentClusters);
        singleSpectra = mergeClustersWithSingles(mergedClusters, singleSpectra);

        singleSpectrumClusters.clear();
        singleSpectrumClusters.addAll(singleSpectra);

        currentClusters.clear();
        currentClusters.addAll(mergedClusters);


        int endClusterCount = currentClusters.size() + singleSpectrumClusters.size();
        return endClusterCount != startingClusterCount;
    }

    /**
     * clusters differing my more than this mx value will not be merged
     */
    public static final double MAXIMUM_MERGE_MZ_DIFFERENCE = 0.3;

    /**
     * spectra differing from clusters differing my more than this mx value will not be merged
     */
    public static final double MAXIMUM_SINGLE_SPECTRUM_MERGE_MZ_DIFFERENCE = 0.6;

    /**
     * @param mergedClusters
     * @param singleSpectra
     * @return
     */
    protected List<ISpectralCluster> mergeClustersWithSingles(List<ISpectralCluster> mergedClusters, List<ISpectralCluster> singleSpectra) {
        // let a shared function do all the dirty work so other engines can share code
        //noinspection UnnecessaryLocalVariable
        List<ISpectralCluster> retained = ClusterUtilities.mergeClustersWithSingleSpectra(mergedClusters,
                singleSpectra, internalGetSimilarityChecker(), MAXIMUM_SINGLE_SPECTRUM_MERGE_MZ_DIFFERENCE);

        return retained;
    }


    /**
     * @param mergable
     * @return
     */
    protected List<ISpectralCluster> mergeClusters(List<ISpectralCluster> mergable) {
        // let a shared function do all the dirty work so other engines can share code
        return ClusterUtilities.mergeClusters(mergable, internalGetSimilarityChecker(), MAXIMUM_MERGE_MZ_DIFFERENCE);
    }

    /**
     * generate clusters in current Clusters from read clusters
     *
     * @return always true something is dome
     */
    protected boolean clusterUsingPeaks() {
        Collections.sort(singleSpectrumClusters, QualityClusterComparator.INSTANCE);   // sort by quality
        for (int index = 0; index < singleSpectrumClusters.size(); index++) {
            ISpectralCluster readCluster = singleSpectrumClusters.get(index);
            if (readCluster.getClusteredSpectraCount() != 1)
                throw new IllegalStateException("this should be a a single spectrum cluster"); // ToDo change
            final ISpectrum theSpectrum = readCluster.getHighestQualitySpectrum();
            final int[] peaks = theSpectrum.asMajorPeakMZs();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < peaks.length; i++) {
                if (alreadyClustered.contains(theSpectrum))    // we are already in a cluster
                    continue;
                int peak = peaks[i];
                final IClusteringEngine engine = factory.getClusteringEngine();
                for (int index2 = index; index2 < singleSpectrumClusters.size(); index2++) {
                    ISpectralCluster addedCluster = singleSpectrumClusters.get(index2);
                    ISpectrum addedSpectrum = readCluster.getHighestQualitySpectrum();
                    if (alreadyClustered.contains(addedSpectrum))
                        continue;
                    if (!addedSpectrum.containsMajorPeak(peak))   // we do not have the peak
                        continue;
                    engine.addClusters(addedCluster);
                }
                if (engine.size() < 2)
                    continue; // nothing to cluster
                engine.processClusters();
                final Collection<ISpectralCluster> clusters = engine.getClusters();
                currentClusters.addAll(clusters);
                for (ISpectralCluster cluster : clusters) {
                    alreadyClustered.addAll(cluster.getClusteredSpectra());
                }
            }

        }

        alreadyClustered.clear(); // we don't need to remember these
        return true;
    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        if (currentClusters.isEmpty()) {        // pass 1
            List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(singleSpectrumClusters);
            Collections.sort(ret);
            return ret;
        } else {
            List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(currentClusters);
            ret.addAll(singleSpectrumClusters);
            Collections.sort(ret);
            return ret;

        }
    }


    /**
     * used to expose internals for overriding classes only
     *
     * @return
     */
    protected SimilarityChecker internalGetSimilarityChecker() {
        return similarityChecker;
    }

    /**
     * used to expose internals for overriding classes only
     *
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    protected Comparator<ISpectralCluster> internalGetSpectrumComparator() {
        return spectrumComparator;
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
     * allow engines to be named
     *
     * @return
     */
    @Override
    public String toString() {
        if (name != null)
            return name;
        return super.toString();
    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return singleSpectrumClusters.size();  // todo do better
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
                    noneFittingSpectra.add(spectrum.asCluster());
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
        if (true) throw new UnsupportedOperationException("Fix This");

    }
}
