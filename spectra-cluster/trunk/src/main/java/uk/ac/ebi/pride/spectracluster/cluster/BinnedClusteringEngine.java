package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.IWideBinner;
import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinnedClusteringEngine
 * performs clustering for a single mz bin
 * User: Steve
 * Date: 6/28/13
 */
//todo: delete
@Deprecated
public class BinnedClusteringEngine implements IClusteringEngine {

    private final IWideBinner binner;
    private final int mainBin;
    private final IClusteringEngine engine;
    private String name;


    public BinnedClusteringEngine(final IWideBinner pBinner, final int pMainBin) {
        binner = pBinner;
        mainBin = pMainBin;
        engine = Defaults.INSTANCE.getDefaultClusteringEngine();   // need a new engine every time
        name = "Bin" + mainBin;
    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        final Collection<ISpectralCluster> clusters = engine.getClusters();
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            final float precursorMz = cluster.getPrecursorMz();
            final int bin = binner.asBin(precursorMz);
            if (bin == mainBin)
                holder.add(cluster); // only report clusters in the main bin
        }
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
            boolean useCluster = false;
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < bins.length; j++) {
                if (mainBin == bins[j]) {
                    useCluster = true;
                    break;
                }
            }
            if (useCluster)
                engine.addClusters(cl);
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
        return engine.findNoneFittingSpectra(cluster);
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
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        return engine.processClusters();
    }

    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(IProgressHandler handler) {
        engine.addProgressMonitor(handler);

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
        return engine.size();
    }


}
