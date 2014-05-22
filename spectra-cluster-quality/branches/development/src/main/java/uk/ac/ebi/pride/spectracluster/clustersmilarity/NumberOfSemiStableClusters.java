package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IClusterStabilityAssessor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class NumberOfSemiStableClusters implements IClusterStatistics<Integer> {
    private int statistics;

    private final IClusterStabilityAssessor clusterStabilityAssessor;

    public NumberOfSemiStableClusters(IClusterStabilityAssessor clusterStabilityAssessor) {
        this.clusterStabilityAssessor = clusterStabilityAssessor;
    }

    @Override
    public Integer getStatistics() {
        return statistics;
    }

    @Override
    public String generateDefaultReport() {
        return "Number Semi Clusters " + statistics;
    }

    @Override
    public void visit(@Nonnull IPeptideSpectralCluster pT) {
        if (clusterStabilityAssessor.isSemiStable(pT))
            statistics++;
    }
}
