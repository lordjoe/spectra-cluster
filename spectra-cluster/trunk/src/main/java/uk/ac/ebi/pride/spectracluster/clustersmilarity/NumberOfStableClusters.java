package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class NumberOfStableClusters implements IClusterStatistics<Integer> {
    private int statistics;

    @Override
    public Integer getStatistics() {
        return statistics;
    }

    @Override
    public String generateDefaultReport() {
        return "Number Stable Clusters " + statistics;
    }


    @Override
    public void visit(@Nonnull IPeptideSpectralCluster pT) {
        if (pT.isStable())
            statistics++;
    }
}
