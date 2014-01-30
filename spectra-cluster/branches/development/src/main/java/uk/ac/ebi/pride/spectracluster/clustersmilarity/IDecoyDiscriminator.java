package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator
 * User: Steve
 * Date: 1/24/14
 */
public interface IDecoyDiscriminator {


    public boolean isDecoy(String peptideSequence);
}
