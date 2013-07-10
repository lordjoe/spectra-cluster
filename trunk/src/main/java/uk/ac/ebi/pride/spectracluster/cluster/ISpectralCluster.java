package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectralCluster extends Equivalent<ISpectralCluster>,Comparable<ISpectralCluster>,IMajorPeaksHolder  {

    // only highest quality spectra used for concensus
    public static final int NUMBER_SPECTRA_FOR_CONSENSUS = 10;
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
     * concensus spectrum Charge
     *
     * @return
     */
    public List<IPeak> getPeaks();

    /**
     * Get consensus spectrum
     */
    public ISpectrum getConsensusSpectrum();

    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
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
     * Add a array of spectrum to cluster
     */
    public void addSpectra(ISpectrum... merged);

    /**
     * Remove an array of spectrum from cluster
     */
    public void removeSpectra(ISpectrum... removed);


    public void append(Appendable out);


    public void appendSpectra(Appendable out) ;

    /**
     * needed so copy constructors work with the interface
     * @return
     */
    public ConsensusSpectrumBuilder getConsensusSpectrumBuilder() ;

    /**
     * does the concensus spectrum contin this is a major peak
     * @param mz   peak as int
     * @return  true if so
     */
    public boolean containsMajorPeak(int mz);


}
