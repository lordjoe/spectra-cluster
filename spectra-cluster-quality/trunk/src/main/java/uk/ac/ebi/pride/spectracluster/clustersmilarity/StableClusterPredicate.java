package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.IClusterStabilityAssessor;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusterPredicate implements TypedPredicate<ICluster> {

    private IClusterStabilityAssessor clusterStabilityAssessor;

    public StableClusterPredicate(IClusterStabilityAssessor clusterStabilityAssessor) {
        this.clusterStabilityAssessor = clusterStabilityAssessor;
    }

    @Override
    public boolean apply(@Nonnull ICluster cluster, Object... objects) {

        return clusterStabilityAssessor.isStable(cluster);
    }
}
