package uk.ac.ebi.pride.spectracluster.analysis.util;

import uk.ac.ebi.pride.spectracluster.analysis.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.analysis.objects.SequenceCount;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jg on 14.07.14.
 */
public class ClusterUtilities {
    private ClusterUtilities() {

    }

    /**
     * Returns a cluster's maximum ratio treating I and L as indistinguishable.
     * @param cluster
     * @return
     */
    public static float getClusterILAgnosticMaxRatio(ICluster cluster) {
        Map<String, Integer> sequenceCounts = new HashMap<String, Integer>(cluster.getSequenceCounts().size());

        for (SequenceCount sequenceCount : cluster.getSequenceCounts()) {
            String sequence = sequenceCount.getSequence();
            sequence = sequence.replaceAll("I", "L");

            if (!sequenceCounts.containsKey(sequence)) {
                sequenceCounts.put(sequence, 0);
            }

            sequenceCounts.put(sequence, sequenceCounts.get(sequence) + sequenceCount.getCount());
        }

        int maxCount = 0;

        for (String sequence : sequenceCounts.keySet()) {
            if (maxCount < sequenceCounts.get(sequence)) {
                maxCount = sequenceCounts.get(sequence);
            }
        }

        return (float) maxCount / (float) cluster.getSpecCount();

        // TODO: write test
    }

    public static boolean isStableStable(ICluster cluster) {
        return cluster.getSpecCount() >= 10 && cluster.getMaxRatio() >= 0.7;
    }
}
