package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.IMajorPeaksHolder;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Equivalent;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectralCluster extends ISpectrumHolder, IPeaksHolder, Equivalent<ISpectralCluster>, Comparable<ISpectralCluster>, IMajorPeaksHolder {
    /**
     * Get cluster id
     */
    public String getId();

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
    public List<ISpectrum> getHighestQualitySpectra();

    /**
     * all internally spectrum
     */
    public List<ISpectrum> getClusteredSpectra();

    /**
     * count of internal spectrum
     */
    public int getClusteredSpectraCount();


    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    public boolean containsMajorPeak(int mz);


    /**
     * Add a list of spectrum to cluster
     */
    public void addSpectra(List<ISpectrum> added);


}