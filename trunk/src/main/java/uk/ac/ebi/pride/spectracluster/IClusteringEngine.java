package uk.ac.ebi.pride.spectracluster;

/**
 * uk.ac.ebi.pride.spectracluster.IClusteringEngine
 *
 * This object does the clusters
 * @author Steve Lewis
 * @date 10/05/13
 */
public interface IClusteringEngine {
    public static IClusteringEngine[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IClusteringEngine.class;

    public ISpectralCluster[] getClusters();

    /**
     * add some clusters
     * @param cluster
     */
    public void addClusters(ISpectralCluster... cluster);

    /**
     * clusters are merged in the internal collection
     * @return true is  anything happened
     */
    public boolean mergeClusters();


}
