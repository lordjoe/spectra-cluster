package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.google.common.collect.MinMaxPriorityQueue;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceSet
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceSet {

    public static final int MAX_PRESERVED_MATCHES = 3;
    private final Map<ISpectralCluster, MinMaxPriorityQueue<ClusterDistanceItem>> bestMatches =
            new HashMap<ISpectralCluster, MinMaxPriorityQueue<ClusterDistanceItem>>();


    public List<ClusterDistanceItem> getBestMatches(ISpectralCluster cluster) {
        MinMaxPriorityQueue<ClusterDistanceItem> items = bestMatches.get(cluster);
        if (items == null)
            return Collections.EMPTY_LIST;
        else
            return new ArrayList<ClusterDistanceItem>(items);
    }

    public void addDistance(ClusterDistanceItem added) {
        ISpectralCluster baseCluster = added.getBaseCluster();
        MinMaxPriorityQueue<ClusterDistanceItem> items = bestMatches.get(baseCluster);
        if (items == null) {
            items = buildQueue();
            bestMatches.put(baseCluster, items);
        }
        items.add(added);

    }

    protected MinMaxPriorityQueue<ClusterDistanceItem> buildQueue() {
          MinMaxPriorityQueue<ClusterDistanceItem> queue = MinMaxPriorityQueue.maximumSize(MAX_PRESERVED_MATCHES).create();
        return queue;

    }
}
