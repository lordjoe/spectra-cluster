package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.spectrum.IMajorPeaksHolder;
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
public interface ISpectralCluster extends ISpectrumHolder, Equivalent<ISpectralCluster>, Comparable<ISpectralCluster>, IMajorPeaksHolder {

    /**
     * Get cluster id
     */
    public String getId();

    /**
     * build an id from spectral ids
     *
     * @return
     */
    @Deprecated
    public String getSpectralId();


    /**
     * concensus spectrum MZ
     *
     * @return
     */
    public float getPrecursorMz();

    /**
     * concensus spectrum Charge
     *
     * @return
     */
    public int getPrecursorCharge();

    /**
     * Get a list of peptide sequences
     *
     * @return
     */
    @Deprecated
    public
    @Nonnull
    List<String> getPeptides();

    /**
     * Get the single most common peptide sequence
     *
     * @return
     */
    @Deprecated
    public String getMostCommonPeptide();

    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    @Deprecated
    public
    @Nonnull
    List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd);


    /**
     * Get consensus spectrum
     */
    public ISpectrum getConsensusSpectrum();

    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    public ISpectrum getHighestQualitySpectrum();

    /**
     * all internally spectrum
     */
    public
    @Nonnull
    List<ISpectrum> getHighestQualitySpectra();

    /**
     * all internally spectrum
     */
    public
    @Nonnull
    List<ISpectrum> getClusteredSpectra();


    /**
     * count of internal spectrum
     */
    public int getClusteredSpectraCount();


    /**
     * return a set of all ids
     *
     * @return
     */
    public
    @Nonnull
    Set<String> getSpectralIds();

    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Deprecated
    public boolean containsMajorPeak(int mz);


    /**
     * Add a list of spectrum to cluster
     */
    public void addSpectra(List<ISpectrum> added);

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isStable();

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isSemiStable();

}
