package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarity
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterSimilarity implements IClusterStatistics<ClusterDistanceSet> {
    private final IClusterDistance distanceMeasure;
    private final IClusterSet otherCluster;
    private final ClusterDistanceSet distances = new ClusterDistanceSet();

    public ClusterSimilarity(IClusterDistance distanceMeasure, IClusterSet otherCluster) {
        this.distanceMeasure = distanceMeasure;
        this.otherCluster = otherCluster;
    }

    public IClusterSet getOtherCluster() {
        return otherCluster;
    }

    public IClusterDistance getDistanceMeasure() {
        return distanceMeasure;
    }

    /**
     * returb an object representing statistics
     *
     * @return
     */
    @Override
    public ClusterDistanceSet getStatistics() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    /**
     * write a simple report
     *
     * @return
     */
    @Override
    public String generateDefaultReport() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    /**
     * @param pT interface implemented by the visitor pattern
     */
    @Override
    public void visit(@Nonnull IPeptideSpectralCluster current) {
        IClusterSet other = getOtherCluster();
        IClusterDistance dm = getDistanceMeasure();
        for (IPeptideSpectralCluster otherCluster : other.getClusters()) {
            double distance = dm.distance(current, otherCluster);
        }

    }
}
