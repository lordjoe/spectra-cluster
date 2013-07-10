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
    private final List<ISpectralCluster> readClusters = new ArrayList<ISpectralCluster>();
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
    public void addClusters(final ISpectralCluster... cluster) {
        readClusters.addAll(Arrays.asList(cluster));
//        for (int i = 0; i < cluster.length; i++) {
//            ISpectralCluster cl = cluster[i];
//            // only add ones in the right bin
//            final int[] bins = cl.asMajorPeakMZs();
//            for (int j = 0; j < bins.length; j++) {
//                int bin = bins[j];
//                if(bin == 0)
//                    continue;
//                IClusteringEngine engine = getEngine(bin);
//                engine.addClusters(cl);
//            }
//        }
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
            Collections.sort(readClusters, QualityClusterComparator.INSTANCE);   // sort by quality
            for (int index = 0; index < readClusters.size(); index++) {
                ISpectralCluster readCluster = readClusters.get(index);
                if (readCluster.getClusteredSpectraCount() != 1)
                    throw new IllegalStateException("this should be a a single spectrum cluster"); // ToDo change
                final ISpectrum theSpectrum = readCluster.getHighestQualitySpectrum();
                final int[] peaks = theSpectrum.asMajorPeakMZs();
                for (int i = 0; i < peaks.length; i++) {
                    if (alreadyClustered.contains(theSpectrum))    // we are already in a cluster
                        continue;
                    int peak = peaks[i];
                    final IClusteringEngine engine = factory.getClusteringEngine();
                    for (int index2 = index; index2 < readClusters.size(); index2++) {
                        ISpectralCluster addedCluster = readClusters.get(index2);
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
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        if (currentClusters.isEmpty()) {        // pass 1
            List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(readClusters);
            Collections.sort(ret);
            return ret;
        }
        else {
            List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(currentClusters);
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

        return readClusters.size();  // todo do better
    }

}
