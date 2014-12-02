package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cluster_fetcher;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jg on 02.12.14.
 * Extracts the defined clusters from a clustering file
 */
public class ClusterFetcher implements IClusterSourceListener {
    /**
     * The ids to extract from the clustering source
     */
    private final Collection<String> clusterIds;
    private List<ICluster> extractedClusters = new ArrayList<ICluster>();

    /**
     * Creates a new instance of ClusterFetcher.
     * @param clusterIds The clusterIds to extract from the ClusterSource
     */
    public ClusterFetcher(Collection<String> clusterIds) {
        this.clusterIds = clusterIds;
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        // simply ignore clusters that are not found in the id collection
        if (!clusterIds.contains(newCluster.getId())) {
            return;
        }

        extractedClusters.add(newCluster);
    }

    public void reset() {
        extractedClusters = new ArrayList<ICluster>();
    }

    public List<ICluster> getExtractedClusters() {
        return extractedClusters;
    }
}
