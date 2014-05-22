package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.IClusterStabilityAssessor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SemiStableClusterPredicate implements TypedPredicate<IPeptideSpectralCluster> {

    private IClusterStabilityAssessor clusterStabilityAssessor;

    public SemiStableClusterPredicate(IClusterStabilityAssessor clusterStabilityAssessor) {
        this.clusterStabilityAssessor = clusterStabilityAssessor;
    }

    @Override
    public boolean apply(@Nonnull IPeptideSpectralCluster cluster, Object... objects) {

        return clusterStabilityAssessor.isSemiStable(cluster);
    }
}
