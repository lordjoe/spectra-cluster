package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusterPredicate implements TypedPredicate<IPeptideSpectrumCluster> {

    @Override
    public boolean apply(@Nonnull IPeptideSpectrumCluster cluster, Object... objects) {

        return cluster.isStable();
    }
}
