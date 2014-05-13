package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.io.IOException;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceItem
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceItem implements IClusterMatch {
    private final ISpectralCluster source;
    private final ISpectralCluster target;
    private final double distance;

    public ClusterDistanceItem(ISpectralCluster baseCluster, ISpectralCluster otherCluster, double distance) {
        this.source = baseCluster;
        this.target = otherCluster;
        this.distance = distance;
    }

    public boolean isIdentical() {
        Set<String> commonSpectralIds = ClusterSimilarityUtilities.commonSpectralIds(source, target);
        if (commonSpectralIds.size() != source.getClusteredSpectraCount())
            return false;
        if (commonSpectralIds.size() != target.getClusteredSpectraCount())
            return false;
        return true;
    }


    public boolean isSubset() {
        Set<String> allSpectrumIds = ClusterSimilarityUtilities.allSpectralIds(source, target);
        if (allSpectrumIds.size() == source.getClusteredSpectraCount())
            return true;
        if (allSpectrumIds.size() == target.getClusteredSpectraCount())
            return true;
        return false;
    }


    public static final int MINIMUM_CLOSE_SUBSET_SIZE = 10;
    public static final double MINIMUM_CLOSE_SUBSET_FRACTION = 0.9;

    public boolean isCloseSubset() {
        double testSize = getSmallerClusterSize();
        if (testSize < MINIMUM_CLOSE_SUBSET_SIZE)
            return false;
        double frac = getCommonFraction();
        if (frac > MINIMUM_CLOSE_SUBSET_FRACTION)
            return true;
        return false;
    }


    public double getCommonFraction() {
        double testSize = Math.min(source.getClusteredSpectraCount(), target.getClusteredSpectraCount());
        if (testSize == 0)
            return 0;
        Set<String> commonSpectralIds = ClusterSimilarityUtilities.commonSpectralIds(source, target);

        double frac = commonSpectralIds.size() / testSize;
        return frac;
    }

    public int getSmallerClusterSize() {
        return Math.min(source.getClusteredSpectraCount(), target.getClusteredSpectraCount());
    }

    /**
     * !null source cluster
     *
     * @return
     */
    @Override
    public ISpectralCluster getTarget() {
        return target;
    }

    /**
     * !null target cluster
     *
     * @return
     */
    @Override
    public ISpectralCluster getSource() {
        return source;
    }

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @return
     */
    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(IClusterMatch o) {
        double delDist = getDistance() - o.getDistance();
        if (delDist != 0)
            return delDist < 0 ? -1 : 1;
        if (getTarget() != o.getTarget()) {
            return System.identityHashCode(getTarget()) >
                    System.identityHashCode(o.getTarget()) ? -1 : 1;

        }
        if (getSource() != o.getSource()) {
            return System.identityHashCode(getSource()) >
                    System.identityHashCode(o.getSource()) ? -1 : 1;

        }
        return 0;
    }

    public void appendReport(Appendable sb) {
        try {
            sb.append("Source id " + source.getId());
            sb.append(" size " + source.getClusteredSpectraCount());
            sb.append(" most peptide " + source.getMostCommonPeptide());
            sb.append("\n");

            sb.append("Target id " + target.getId());
            sb.append(" size " + target.getClusteredSpectraCount());
            sb.append(" most peptide " + target.getMostCommonPeptide());
            sb.append("\n");

            sb.append(String.format("Distance %8.3f", getDistance()));
//        sb.append("\n");
            Set<String> commonIds = ClusterSimilarityUtilities.commonSpectralIds(source, target);
//        String common = ClusterSimilarityUtilities.idsToString(commonIds) ;
            sb.append(" common spectra " + commonIds.size());
            Set<String> allSpectrumIds = ClusterSimilarityUtilities.allSpectralIds(source, target);
            sb.append(" all spectra " + allSpectrumIds.size());
            sb.append("\n");

            Set<String> commonPeptides = ClusterSimilarityUtilities.commonPeptides(source, target);
            String common = ClusterSimilarityUtilities.idsToString(commonPeptides);
            sb.append(" common peptide " + common);

            Set<String> allPeptides = ClusterSimilarityUtilities.allPeptides(source, target);
            String all = ClusterSimilarityUtilities.idsToString(allPeptides);
            sb.append(" All peptide " + all);
        } catch (IOException e) {
            new RuntimeException(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendReport(sb);
        return sb.toString();
    }
}
