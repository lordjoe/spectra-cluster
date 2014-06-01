package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IClusterStabilityAssessor;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class NumberOfStableClusters implements IClusterStatistics<Integer> {
    private int statistics;
    private final IClusterStabilityAssessor clusterStabilityAssessor;

    public NumberOfStableClusters(IClusterStabilityAssessor clusterStabilityAssessor) {
        this.clusterStabilityAssessor = clusterStabilityAssessor;
    }

    @Override
    public Integer getStatistics() {
        return statistics;
    }

    @Override
    public String generateDefaultReport() {
        return "Number Stable Clusters " + statistics;
    }


    @Override
    public void visit(@Nonnull ICluster pT) {
        if (clusterStabilityAssessor.isStable(pT))
            statistics++;
    }
}
