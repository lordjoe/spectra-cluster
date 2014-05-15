package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
// TODO JG: remove the functions of the former IPeaksHolder from the implementations
public interface ISpectralCluster extends ISpectrumHolder,
                                          Equivalent<ISpectralCluster>,
                                          Comparable<ISpectralCluster>{

    /**
     * Get cluster id
     */
    String getId();

    /**
     * build an id from spectral ids
     *
     * @return
     */
    @Deprecated
    String getSpectralId();


    /**
     * concensus spectrum MZ
     *
     * @return
     */
    float getPrecursorMz();

    /**
     * concensus spectrum Charge
     *
     * @return
     */
    int getPrecursorCharge();

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


    /**
     * Get consensus spectrum
     */
    ISpectrum getConsensusSpectrum();

    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    ISpectrum getHighestQualitySpectrum();

    /**
     * all internally spectrum
     */
    @Nonnull
    List<ISpectrum> getHighestQualitySpectra();

    /**
     * all internally spectrum
     */
    @Nonnull
    List<ISpectrum> getClusteredSpectra();


    /**
     * count of internal spectrum
     */
    int getClusteredSpectraCount();


    /**
     * return a set of all ids
     *
     * @return
     */
    @Nonnull
    Set<String> getSpectralIds();


    /**
     * Add a list of spectrum to cluster
     */
    void addSpectra(List<ISpectrum> added);

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    boolean isStable();

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    boolean isSemiStable();

}
