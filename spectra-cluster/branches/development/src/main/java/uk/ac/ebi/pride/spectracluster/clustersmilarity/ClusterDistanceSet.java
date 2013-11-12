package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceSet
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceSet {

    public static final int MAX_PRESERVED_MATCHES = 3;
    private final Map<ISpectralCluster, PriorityQueue<ClusterDistanceItem>> bestMatches =
            new HashMap<ISpectralCluster, PriorityQueue<ClusterDistanceItem>>();


    public List<ClusterDistanceItem> getBestMatches(ISpectralCluster cluster) {
        PriorityQueue<ClusterDistanceItem> items = bestMatches.get(cluster);
        if (items == null)
            return Collections.EMPTY_LIST;
        else
            return new ArrayList<ClusterDistanceItem>(items);
    }

    public void addDistance(ClusterDistanceItem added) {
        ISpectralCluster baseCluster = added.getBaseCluster();
        PriorityQueue<ClusterDistanceItem> items = bestMatches.get(baseCluster);
        if (items == null) {
            items = buildQueue();
            bestMatches.put(baseCluster, items);
        }
        items.add(added);

    }

    protected PriorityQueue<ClusterDistanceItem> buildQueue() {
          PriorityQueue<ClusterDistanceItem> queue = new PriorityQueue<ClusterDistanceItem>(MAX_PRESERVED_MATCHES);
        return queue;

    }
}
