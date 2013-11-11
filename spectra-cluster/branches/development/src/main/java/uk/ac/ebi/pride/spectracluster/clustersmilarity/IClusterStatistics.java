package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.TypedVisitor;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterStatistics<T> extends TypedVisitor<ISpectralCluster> {

    /**
     * returb an object representing ststistics
     * @return
     */
    public T getStatistics();

    /**
     * write a simple report
     * @return
     */
    public String generateDefaultReport();
}
