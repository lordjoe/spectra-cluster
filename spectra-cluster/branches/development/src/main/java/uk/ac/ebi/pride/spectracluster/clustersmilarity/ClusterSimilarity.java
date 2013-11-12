package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarity
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterSimilarity implements IClusterStatistics<ClusterDistanceSet> {
    private final ClusterDistance distanceMeasure;
    private final IClusterSet otherCluster;
    private final ClusterDistanceSet distances = new ClusterDistanceSet();

    public ClusterSimilarity(ClusterDistance distanceMeasure, IClusterSet otherCluster) {
        this.distanceMeasure = distanceMeasure;
        this.otherCluster = otherCluster;
    }

    public IClusterSet getOtherCluster() {
        return otherCluster;
    }

    public ClusterDistance getDistanceMeasure() {
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
    public void visit(@Nonnull ISpectralCluster current) {
        IClusterSet other = getOtherCluster();
        ClusterDistance dm = getDistanceMeasure();
        for (ISpectralCluster otherCluster : other.getClusters()) {
            double distance = dm.distance(current, otherCluster);
        }

    }
}
