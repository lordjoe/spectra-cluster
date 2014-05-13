package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.LimitedList;
import uk.ac.ebi.pride.spectracluster.util.TreeSetLimitedList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceSet
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceSet {

    public static final int MAX_PRESERVED_MATCHES = 3;
    private final Map<ISpectralCluster, LimitedList<ClusterDistanceItem>> bestMatches =
            new HashMap<ISpectralCluster, LimitedList<ClusterDistanceItem>>();


    public List<ClusterDistanceItem> getBestMatches(ISpectralCluster cluster) {
        LimitedList<ClusterDistanceItem> items = bestMatches.get(cluster);
        if (items == null)
            //noinspection unchecked
            return (List<ClusterDistanceItem>) Collections.EMPTY_LIST;
        else
            return items.toList();
    }

    public void addDistance(ClusterDistanceItem added) {
        ISpectralCluster baseCluster = added.getSource();
        LimitedList<ClusterDistanceItem> items = bestMatches.get(baseCluster);
        if (items == null) {
            items = buildQueue();
            bestMatches.put(baseCluster, items);
        }
        items.add(added);

    }

    protected LimitedList<ClusterDistanceItem> buildQueue() {
        //noinspection UnnecessaryLocalVariable
        LimitedList<ClusterDistanceItem> queue = new TreeSetLimitedList<ClusterDistanceItem>(ClusterDistanceItem.class, MAX_PRESERVED_MATCHES);
        return queue;

    }
}
