package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.TypedVisitor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterStatistics implements TypedVisitor<ISpectralCluster> {
    private final List<IClusterStatistics> clusterStatisticses = new ArrayList<IClusterStatistics>();

    public ClusterStatistics() {
        clusterStatisticses.add(new NumberOfClusters());
        clusterStatisticses.add(new NumberOfStableClusters());
        clusterStatisticses.add(new NumberOfSemiStableClusters());
        clusterStatisticses.add(new PeptideToClustersStatistics());
        clusterStatisticses.add(new SpectrumToClustersStatistics());
    }


    public void addStatisticeMethod(IClusterStatistics added)   {
        clusterStatisticses.add(added);
    }

    @Override
    public void visit(@Nonnull ISpectralCluster pT) {
        for (IClusterStatistics visitor : clusterStatisticses) {
            visitor.visit(pT);
        }
    }


    public String generateReport() {
        StringBuilder sb = new StringBuilder();

        for (IClusterStatistics clusterStatisticse : clusterStatisticses) {
            sb.append(clusterStatisticse.generateDefaultReport());
            sb.append("\n");
        }

        return sb.toString();
    }
}
