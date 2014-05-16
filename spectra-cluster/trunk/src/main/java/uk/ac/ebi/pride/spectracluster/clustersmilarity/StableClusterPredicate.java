package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusterPredicate implements TypedPredicate<IPeptideSpectralCluster> {

    @Override
    public boolean apply(@Nonnull IPeptideSpectralCluster cluster, Object... objects) {

        return cluster.isStable();
    }
}
