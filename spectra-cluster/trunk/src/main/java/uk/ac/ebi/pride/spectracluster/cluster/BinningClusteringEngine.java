package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.IWideBinner;
import com.lordjoe.algorithms.LinearWideBinner;
import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinningClusteringEngine
 * performs clustering using a series of binned engines
 * User: Steve
 * Date: 6/28/13
 */
//todo: delete
@Deprecated
public class BinningClusteringEngine implements IClusteringEngine {

    public static final int DEFAULT_MAX_BIN = 1600;
    public static final int DEFAULT_MIN_BIN = 300;


    private final IWideBinner binner;
    private final Map<Integer, IClusteringEngine> engineForBin = new HashMap<Integer, IClusteringEngine>();
    private String name = "BinningClusteringEngine";

    @SuppressWarnings("UnusedDeclaration")
    public BinningClusteringEngine() {
        this(new LinearWideBinner((int) (DEFAULT_MAX_BIN + 0.5), 1, DEFAULT_MIN_BIN, true));
    }


    public BinningClusteringEngine(final IWideBinner pBinner) {
        binner = pBinner;
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
     * add some clusters
     */
    @Override
    public void addClusters(final ISpectralCluster... cluster) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < cluster.length; i++) {
            ISpectralCluster cl = cluster[i];
            // only add ones in the right bin
            final float precursorMz = cl.getPrecursorMz();
            int[] bins = binner.asBins(precursorMz);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                IClusteringEngine engine = getEngine(bin);
                engine.addClusters(cl);
            }
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
        return ClusteringUtilities.asWritttenSpectra(cluster, this);
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
                ret = new BinnedClusteringEngine(binner, pBin);
                engineForBin.put(pBin, ret);
            }
            return ret;
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
        // todo use multiple threads
        for (IClusteringEngine engine : engineForBin.values()) {
            //noinspection UnusedDeclaration
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
