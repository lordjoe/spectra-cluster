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
        for (int i = 0; i < cluster.length; i++) {
            ISpectralCluster sc = cluster[i];
            clusters.add(sc);
        }
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
}