package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterStatistics<T> extends TypedVisitor<IPeptideSpectralCluster> {

    /**
     * returb an object representing ststistics
     *
     * @return
     */
    public T getStatistics();

    /**
     * write a simple report
     *
     * @return
     */
    public String generateDefaultReport();
}