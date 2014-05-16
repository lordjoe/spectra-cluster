package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusterSetStatistics implements IClusterStatistics<MostSimilarClusterSet> {

    private final MostSimilarClusterSet mostSimilarClusterSet;
    private final List<IPeptideSpectrumCluster> clusters;
    private boolean dirty;


    public MostSimilarClusterSetStatistics(IClusterSet baseClusterSet, IClusterDistance clusterDistance) {
        this.mostSimilarClusterSet = new MostSimilarClusterSet(baseClusterSet, clusterDistance);
        this.clusters = new ArrayList<IPeptideSpectrumCluster>();
    }


    @Override
    public MostSimilarClusterSet getStatistics() {
        return mostSimilarClusterSet;
    }

    @Override
    public String generateDefaultReport() {
        guaranteeClean();

        return mostSimilarClusterSet.getReport();
    }

    private void guaranteeClean() {
        if (dirty) {
            mostSimilarClusterSet.clear();
            mostSimilarClusterSet.addOtherSet(new SimpleClusterSet(clusters));
        }
    }

    @Override
    public void visit(@Nonnull IPeptideSpectrumCluster pT) {
        dirty = true;
        clusters.add(pT);
    }
}
