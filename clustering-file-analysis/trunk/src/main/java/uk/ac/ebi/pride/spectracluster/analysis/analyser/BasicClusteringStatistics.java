package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import uk.ac.ebi.pride.spectracluster.analysis.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

/**
 * Created by jg on 12.07.14.
 */
public class BasicClusteringStatistics implements IClusteringSourceAnalyser {
    public static String FILE_ENDING = ".basic_statistics.txt";
    public static String DESCRIPTION = "Generates basic statistics about a .clustering file like # clusters.";

    float nClusters = 0;
    int nSingleClusters = 0;
    float averageRatio = 0;
    float averageClusterSize = 0;
    int minSize = Integer.MAX_VALUE;
    int maxSize = 0;
    float minRatio = Float.MAX_VALUE;
    float maxRatio = 0;
    int stableClusters = 0;

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

        // ignore clusters with only 1 spectrum for any other paramter
        if (newCluster.getSpecCount() < 2) {
            nSingleClusters++;
            return;
        }

        averageRatio = averageRatio / nClusters * (nClusters - 1) + newCluster.getMaxRatio() / nClusters;
        averageClusterSize = averageClusterSize / nClusters * (nClusters - 1) + newCluster.getSpecCount() / nClusters;

        if (minRatio > newCluster.getMaxRatio())
            minRatio = newCluster.getMaxRatio();
        if (maxRatio < newCluster.getMaxRatio())
            maxRatio = newCluster.getMaxRatio();

        if (ClusterUtilities.isStableStable(newCluster))
            stableClusters++;
    }

    @Override
    public String getAnalysisResultString() {
        return String.format("Number of clusters: %.0f (%d with 1 spec)\nAverage maximum ratio: %.3f\nAverage cluster size: %.3f\n" +
                        "Minimum size: %d\nMaximum size: %d\n" +
                        "Minimum ratio: %.3f\nMaximum ratio: %.3f\n" +
                        "Stable clusters: %d\n",
                nClusters, nSingleClusters, averageRatio, averageClusterSize, minSize, maxSize, minRatio, maxRatio, stableClusters);
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
    }
}
