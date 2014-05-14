package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusterPredicate implements TypedPredicate<ISpectralCluster> {

    @Override
    public boolean apply(@Nonnull ISpectralCluster cluster, Object... objects) {

        return cluster.isStable();
    }
}
