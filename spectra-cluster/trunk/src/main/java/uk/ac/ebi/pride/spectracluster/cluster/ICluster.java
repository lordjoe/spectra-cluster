package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface ICluster extends ISpectrumHolder,
                                          Equivalent<ICluster>,
                                          Comparable<ICluster>{

    /**
     * Get cluster id
     */
    String getId();

    /**
     * build an id from spectral ids
     *
     * @return
     *
     * todo: for development, Steve is feeling strong about this
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
     *
     * todo: for development
     */
    @Deprecated
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
}