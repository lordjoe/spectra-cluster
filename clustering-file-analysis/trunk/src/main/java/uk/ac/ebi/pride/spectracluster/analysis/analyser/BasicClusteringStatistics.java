package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import uk.ac.ebi.pride.spectracluster.analysis.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

/**
 * Created by jg on 12.07.14.
 */
public class BasicClusteringStatistics implements IClusteringSourceAnalyser {
    public static String FILE_ENDING = ".basic_statistics.txt";
    public static String DESCRIPTION = "Generates basic statistics about a .clustering file like # clusters.";
    public static final float LARGE_PRECURSOR_MZ_RANGE = 1.5F;

    private float nClusters = 0;
    private int nSingleClusters = 0;
    private float averageRatio = 0;
    private float averageClusterSize = 0;
    private int minSize = Integer.MAX_VALUE;
    private int maxSize = 0;
    private float minRatio = Float.MAX_VALUE;
    private float maxRatio = 0;
    private int stableClusters = 0;
    private float maxPrecursorMzRange = 0F;
    /**
     * Number of clusters with a large precursor m/z range
     */
    private int nLargePrecursorMzRange = 0;
    /**
     * used to calculate the average precursor m/z range
     * after the run.
     */
    private float totalPrecursorMzRange = 0F;


    @Override
    public String getFileEnding() {
        return FILE_ENDING;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        nClusters++;

        if (minSize > newCluster.getSpecCount())
            minSize = newCluster.getSpecCount();
        if (maxSize < newCluster.getSpecCount())
            maxSize = newCluster.getSpecCount();

        // ignore clusters with only 1 spectrum for any other parameter
        if (newCluster.getSpecCount() < 2) {
            nSingleClusters++;
            return;
        }

        averageRatio = averageRatio / nClusters * (nClusters - 1) + newCluster.getMaxRatio() / nClusters;
        averageClusterSize = averageClusterSize / nClusters * (nClusters - 1) + newCluster.getSpecCount() / nClusters;
        totalPrecursorMzRange += newCluster.getSpectrumPrecursorMzRange();
        if (maxPrecursorMzRange < newCluster.getSpectrumPrecursorMzRange())
            maxPrecursorMzRange = newCluster.getSpectrumPrecursorMzRange();
        if (newCluster.getSpectrumPrecursorMzRange() >= LARGE_PRECURSOR_MZ_RANGE)
            nLargePrecursorMzRange++;

        if (minRatio > newCluster.getMaxRatio())
            minRatio = newCluster.getMaxRatio();
        if (maxRatio < newCluster.getMaxRatio())
            maxRatio = newCluster.getMaxRatio();

        if (ClusterUtilities.isStableStable(newCluster))
            stableClusters++;
    }

    @Override
    public String getAnalysisResultString() {
        return String.format("Number of clusters: %.0f (%d with 1 spec)\n" +
                        "Average maximum ratio: %.3f\n" +
                        "Average cluster size: %.3f\n" +
                        "Minimum size: %d\nMaximum size: %d\n" +
                        "Minimum ratio: %.3f\nMaximum ratio: %.3f\n" +
                        "Stable clusters: %d\n" +
                        "Average precursor m/z range: %.2f\n" +
                        "Max. precursor m/z range: %.2f\n" +
                        "Clusters with precursor m/z range > %.1f: %d\n",
                nClusters, nSingleClusters, averageRatio, averageClusterSize, minSize, maxSize,
                minRatio, maxRatio, stableClusters, totalPrecursorMzRange / (nClusters - nSingleClusters),
                maxPrecursorMzRange, LARGE_PRECURSOR_MZ_RANGE, nLargePrecursorMzRange);
    }

    @Override
    public void reset() {
        nClusters = 0;
        nSingleClusters = 0;
        averageRatio = 0;
        averageClusterSize = 0;
        minSize = Integer.MAX_VALUE;
        maxSize = 0;
        minRatio = Float.MAX_VALUE;
        maxRatio = 0;
        stableClusters = 0;
        totalPrecursorMzRange = 0;
        maxPrecursorMzRange = 0;
        nLargePrecursorMzRange = 0;
    }
}
