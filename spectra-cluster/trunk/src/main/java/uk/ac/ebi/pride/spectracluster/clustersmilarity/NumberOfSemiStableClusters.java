package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class NumberOfSemiStableClusters implements IClusterStatistics<Integer> {
    private int statistics;

    @Override
    public Integer getStatistics() {
        return statistics;
    }

    @Override
    public String generateDefaultReport() {
        return "Number Semi Clusters " + statistics;
    }

    @Override
    public void visit(@Nonnull IPeptideSpectrumCluster pT) {
        if (pT.isSemiStable())
            statistics++;
    }
}
