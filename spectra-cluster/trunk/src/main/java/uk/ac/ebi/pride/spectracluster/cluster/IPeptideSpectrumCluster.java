package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
// TODO JG: remove the functions of the former IPeaksHolder from the implementations
public interface IPeptideSpectrumCluster extends ISpectralCluster {

    /**
     * build an id from spectral ids
     *
     * @return
     */
    @Deprecated
    String getSpectralId();

    /**
     * Get a list of peptide sequences
     *
     * @return
     */
    @Deprecated
    @Nonnull
    List<String> getPeptides();

    /**
     * Get the single most common peptide sequence
     *
     * @return
     */
    @Deprecated
    String getMostCommonPeptide();

    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    @Deprecated
    @Nonnull
    List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd);

}
