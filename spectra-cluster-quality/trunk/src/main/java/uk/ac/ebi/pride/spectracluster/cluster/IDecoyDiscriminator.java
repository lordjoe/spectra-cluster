package uk.ac.ebi.pride.spectracluster.cluster;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator
 * User: Steve
 * Date: 1/24/14
 *
 * todo: move to similarity
 */

public interface IDecoyDiscriminator {


    public boolean isDecoy(String peptideSequence);
}
