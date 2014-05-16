package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterStatistics implements TypedVisitor<IPeptideSpectrumCluster> {
    private final List<IClusterStatistics> clusterStatisticses = new ArrayList<IClusterStatistics>();
    private final IClusterSet clusters;
    private final ISpectrumRetriever spectra;


    public ClusterStatistics(ISpectrumRetriever spectra, IClusterSet clusters, IClusterStatistics... other) {
        this.clusters = clusters;
        this.spectra = spectra;

        clusterStatisticses.add(new NumberOfClusters());
        clusterStatisticses.add(new NumberOfStableClusters());
        clusterStatisticses.add(new NumberOfSemiStableClusters());
        clusterStatisticses.add(new PeptideToClustersStatistics());
        clusterStatisticses.add(new SpectrumToClustersStatistics(spectra));

        for (IClusterStatistics o : other) {
            addStatisticMethod(o);
        }
    }

    public void gatherData() {
        List<IPeptideSpectrumCluster> clusters1 = clusters.getClusters();
        for (IPeptideSpectrumCluster sc : clusters1) {
            visit(sc);
        }
    }

    public IClusterSet getClusters() {
        return clusters;
    }

    public ISpectrumRetriever getSpectra() {
        return spectra;
    }

    public void addStatisticMethod(IClusterStatistics added) {
        clusterStatisticses.add(added);
    }

    @Override
    public void visit(@Nonnull IPeptideSpectrumCluster pT) {
        for (IClusterStatistics visitor : clusterStatisticses) {
            visitor.visit(pT);
        }
    }


    public String generateReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("Report on clusters " + clusters.getName() + "\n");
        for (IClusterStatistics clusterStatisticse : clusterStatisticses) {
            sb.append(clusterStatisticse.generateDefaultReport());
            sb.append("\n");
        }

        return sb.toString();
    }
}
