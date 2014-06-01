package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideToClustersStatistics implements IClusterStatistics<PeptideToClusters> {
    private final PeptideToClusters peptideToClusters = new PeptideToClusters();

    @Override
    public PeptideToClusters getStatistics() {
        return peptideToClusters;
    }


    @Override
    public String generateDefaultReport() {
        return "Not done yet";
    }

    @Override
    public void visit(@Nonnull ICluster pT) {
        peptideToClusters.addCluster(pT);
    }
}
