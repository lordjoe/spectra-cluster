package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.NullClusteringEngine
 * this version of the clustering engine never does anything - simply
 * returns the clusters passed in
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
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
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean mergeClusters() {
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
