package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.engine.ClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngineFactory;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.PeakMatchClusteringEngineOriginal
 * performs clustering by looking at major peaks then merging clusters
 * This version works but is inefficient - the next version tracks spectra already clustered
 * and attempts to combine clusters
 * User: Steve
 * Date: 6/28/13
 */
@Deprecated
@SuppressWarnings("UnusedDeclaration")
public class PeakMatchClusteringEngineOriginal implements IClusteringEngine {


    private final SimilarityChecker similarityChecker;
    private final Comparator<ISpectralCluster> spectrumComparator;
    private final Map<Integer, IClusteringEngine> engineForBin = new HashMap<Integer, IClusteringEngine>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Set<ISpectralCluster> readClusters = new HashSet<ISpectralCluster>();
    private final IClusteringEngineFactory factory;
    private String name = "PeakMatchClusteringEngineOriginal";

    public PeakMatchClusteringEngineOriginal() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }


    public PeakMatchClusteringEngineOriginal(final Comparator<ISpectralCluster> spectrumComparator) {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker(), spectrumComparator);
    }


    public PeakMatchClusteringEngineOriginal(final SimilarityChecker similarityChecker) {
        this(similarityChecker, Defaults.INSTANCE.getDefaultSpectrumComparator());
    }


    public PeakMatchClusteringEngineOriginal(final SimilarityChecker similarityChecker, final Comparator<ISpectralCluster> spectrumComparator) {
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
     * find the engine for a bin creating one as needed
     *
     * @return
     */
    protected IClusteringEngine getSomeEngine() {
        synchronized (engineForBin) {
            if (engineForBin.isEmpty()) {
                return getEngine(0);
            } else {
                return engineForBin.entrySet().iterator().next().getValue();
            }
        }
    }

    /**
     * find the engine for a bin creating one as needed
     *
     * @param pBin
     * @return
     */
    protected IClusteringEngine getEngine(final int pBin) {
        synchronized (engineForBin) {
            IClusteringEngine ret = engineForBin.get(pBin);
            if (ret == null) {
                ret = factory.getClusteringEngine();
                ret.setName("Peak " + pBin);
                engineForBin.put(pBin, ret);
            }
            return ret;
        }
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    @Override
    public List<ISpectralCluster> findNoneFittingSpectra(final ISpectralCluster cluster) {
        return getSomeEngine().findNoneFittingSpectra(cluster);
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
        return ClusteringUtilities.asWritttenSpectra(cluster, getSomeEngine());
    }


    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {

        boolean anythingDone = false;
        // todo use multiple threads
        for (IClusteringEngine engine : engineForBin.values()) {
            final Collection<ISpectralCluster> clusters = engine.getClusters();
            anythingDone |= engine.processClusters();
        }
        return anythingDone;
    }

    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(IProgressHandler handler) {
        // todo will this work as engines created dynamicallys
        for (IClusteringEngine engine : engineForBin.values()) {
            engine.addProgressMonitor(handler);
        }

    }

    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (IClusteringEngine engine : engineForBin.values()) {
            final Collection<ISpectralCluster> clusters = engine.getClusters();
            holder.addAll(clusters);
        }
        Collections.sort(holder);
        return holder;
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
        int n = 0;
        // todo use multiple threads
        for (IClusteringEngine engine : engineForBin.values()) {
            n += engine.size();
        }
        return n;
    }

}
