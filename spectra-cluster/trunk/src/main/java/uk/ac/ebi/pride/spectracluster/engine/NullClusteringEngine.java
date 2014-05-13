package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.engine.NullClusteringEngine
 * this version of the clustering engine never does anything - simply
 * returns the clusters passed in
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
//todo: delete
@Deprecated
@SuppressWarnings("UnusedDeclaration")
public class NullClusteringEngine implements IClusteringEngine {

    private boolean dirty;
    private final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();

    public NullClusteringEngine() {
    }


    protected void guaranteeClean() {
        if (isDirty()) {
            Collections.sort(clusters);  // sort by mz
            setDirty(false);
        }
    }

    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        guaranteeClean();
        //noinspection UnnecessaryLocalVariable
        ArrayList<ISpectralCluster> scs = new ArrayList<ISpectralCluster>(clusters);
        return scs;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(ISpectralCluster... cluster) {
        clusters.addAll(Arrays.asList(cluster));

        setDirty(true);

    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    @Override
    public List<ISpectralCluster> findNoneFittingSpectra(final ISpectralCluster cluster) {
        return new ArrayList<ISpectralCluster>();
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

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        guaranteeClean();
        return false;
    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return "NullClusteringEngine";
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {

    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return 0;
    }
}
