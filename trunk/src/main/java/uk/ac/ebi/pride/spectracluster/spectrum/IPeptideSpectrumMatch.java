package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.Equivalent;

/**
 * uk.ac.ebi.pride.spectracluster.IScoredSpectrum
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum {

    public static final int MAJOR_PEAK_NUMBER = 6; // Frand et al does 5 we do 1 more
    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();

}
