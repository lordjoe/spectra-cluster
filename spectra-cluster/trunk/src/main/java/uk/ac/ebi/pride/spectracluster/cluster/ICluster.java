package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Equivalent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface ICluster extends ISpectrumHolder,
        Equivalent<ICluster>,
        Comparable<ICluster> {

    /**
     * Get cluster id
     */
    public String getId();

    /**
     * build an id from spectral ids
     *
     * @return
     */
    public String getSpectralId();

    /**
     * concensus spectrum MZ. If not available (ie. no spectra in cluster)
     * 0 is returned.
     *
     * @return
     */
    public float getPrecursorMz();

    /**
     * concensus spectrum Charge. If not available (ie. no spectra in cluster)
     * 0 is returned.
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
    @Nonnull
    public List<ISpectrum> getHighestQualitySpectra();

    /**
     * all internally spectrum
     */
    @Nonnull
    public List<ISpectrum> getClusteredSpectra();


    /**
     * count of internal spectrum
     */
    public int getClusteredSpectraCount();


    /**
     * return a set of all ids
     *
     * @return
     */
    @Nonnull
    public Set<String> getSpectralIds();
}
