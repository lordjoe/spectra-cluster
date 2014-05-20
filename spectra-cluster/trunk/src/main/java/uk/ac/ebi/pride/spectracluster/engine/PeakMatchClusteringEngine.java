package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.comparator.QualityClusterComparator;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.engine.PeakMatchClusteringEngine
 * performs clustering by looking at major peaks then merging clusters
 * - the this version tracks spectra already clustered
 * and attempts to combine clusters
 * <p/>
 * User: Steve
 * Date: 6/28/13
 *
 * todo: development
 */
@Deprecated
public class PeakMatchClusteringEngine implements IClusteringEngine {
    // Frank et al does 5 we do 1 more
    public static final int MAJOR_PEAK_NUMBER = 6;

    private final ISimilarityChecker similarityChecker;
    private final Comparator<ICluster> spectrumComparator;
    private final List<ICluster> singleSpectrumClusters = new ArrayList<ICluster>();
    private final List<ICluster> currentClusters = new ArrayList<ICluster>();
    private final Set<ISpectrum> alreadyClustered = new HashSet<ISpectrum>();
    private final ClusteringEngineFactory factory;

    public PeakMatchClusteringEngine(final ISimilarityChecker similarityChecker, final Comparator<ICluster> spectrumComparator) {
        this.similarityChecker = similarityChecker;
        this.spectrumComparator = spectrumComparator;
        factory = new ClusteringEngineFactory(similarityChecker, spectrumComparator);
    }


    @Override
    public ISimilarityChecker getSimilarityChecker() {
        return similarityChecker;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(final ICluster... pClusters) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < pClusters.length; i++) {
            ICluster cluster = pClusters[i];
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
        List<ICluster> singleSpectra = ClusterUtilities.removeSingleSpectrumClusters(currentClusters);
        List<ICluster> mergedClusters = mergeClusters(currentClusters);
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
    protected List<ICluster> mergeClustersWithSingles(List<ICluster> mergedClusters, List<ICluster> singleSpectra) {
        // let a shared function do all the dirty work so other engines can share code
        //noinspection UnnecessaryLocalVariable
        List<ICluster> retained = ClusterUtilities.mergeClustersWithSingleSpectra(mergedClusters,
                singleSpectra, internalGetSimilarityChecker(), MAXIMUM_SINGLE_SPECTRUM_MERGE_MZ_DIFFERENCE);

        return retained;
    }


    /**
     * @param mergable
     * @return
     */
    protected List<ICluster> mergeClusters(List<ICluster> mergable) {
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
            ICluster readCluster = singleSpectrumClusters.get(index);
            if (readCluster.getClusteredSpectraCount() != 1)
                throw new IllegalStateException("this should be a a single spectrum cluster"); // ToDo change
            final ISpectrum theSpectrum = readCluster.getHighestQualitySpectrum();
            final int[] peaks = theSpectrum.asMajorPeakMZs(MAJOR_PEAK_NUMBER);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < peaks.length; i++) {
                if (alreadyClustered.contains(theSpectrum))    // we are already in a cluster
                    continue;
                int peak = peaks[i];
                final IClusteringEngine engine = factory.getClusteringEngine();
                for (int index2 = index; index2 < singleSpectrumClusters.size(); index2++) {
                    ICluster addedCluster = singleSpectrumClusters.get(index2);
                    ISpectrum addedSpectrum = readCluster.getHighestQualitySpectrum();
                    if (alreadyClustered.contains(addedSpectrum))
                        continue;
                    if (!addedSpectrum.containsMajorPeak(peak, MAJOR_PEAK_NUMBER))   // we do not have the peak
                        continue;
                    engine.addClusters(addedCluster);
                }
                if (engine.size() < 2)
                    continue; // nothing to cluster
                engine.processClusters();
                final Collection<ICluster> clusters = engine.getClusters();
                currentClusters.addAll(clusters);
                for (ICluster cluster : clusters) {
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
    public List<ICluster> getClusters() {
        if (currentClusters.isEmpty()) {        // pass 1
            List<ICluster> ret = new ArrayList<ICluster>(singleSpectrumClusters);
            Collections.sort(ret);
            return ret;
        } else {
            List<ICluster> ret = new ArrayList<ICluster>(currentClusters);
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
    protected ISimilarityChecker internalGetSimilarityChecker() {
        return similarityChecker;
    }

    /**
     * used to expose internals for overriding classes only
     *
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    protected Comparator<ICluster> internalGetSpectrumComparator() {
        return spectrumComparator;
    }

    /**
     * allow engines to be named
     *
     * @return
     */
    @Override
    public String toString() {
        String name = this.getClass().getName();
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
