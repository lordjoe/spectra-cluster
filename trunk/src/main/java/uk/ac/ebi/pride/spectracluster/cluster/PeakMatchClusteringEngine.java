package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.PeakMatchClusteringEngine
 * performs clustering by looking at major peaks then merging clusters
 * - the this version tracks spectra already clustered
 * Original code in  PeakMatchClusteringEngineOriginal
 * and attempts to combine clusters * User: Steve
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


    public PeakMatchClusteringEngine(final Comparator<ISpectralCluster> spectrumComparator) {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker(), spectrumComparator);
    }


    public PeakMatchClusteringEngine(final SimilarityChecker similarityChecker) {
        this(similarityChecker, Defaults.INSTANCE.getDefaultSpectrumComparator());
    }


    public PeakMatchClusteringEngine(final SimilarityChecker similarityChecker, final Comparator<ISpectralCluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
        factory = ClusteringEngine.getClusteringEngineFactory(similarityChecker, spectrumComparator);
    }


    /**
     * add some clusters
     */
    @Override
    public void addClusters(final ISpectralCluster... pClusters) {
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
        boolean anythingDone = false;
        if (currentClusters.isEmpty()) {
            return clusterUsingPeaks();
        }
        else {
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
     * @param mergable
     * @return
     */
    protected List<ISpectralCluster> mergeClustersWithSingles(List<ISpectralCluster> mergedClusters, List<ISpectralCluster> singleSpectra) {
        // let a shared function do all the dirty work so other engines can share code
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
                final List<ISpectralCluster> clusters = engine.getClusters();
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
        }
        else {
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

}
