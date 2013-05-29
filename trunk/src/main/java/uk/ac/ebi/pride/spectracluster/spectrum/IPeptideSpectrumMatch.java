package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;

/**
 * uk.ac.ebi.pride.spectracluster.IScoredSpectrum
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum {
    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();

}
