package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * uk.ac.ebi.pride.spectracluster.IScoredSpectrum
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum, ISpectrumQuality {
    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();
}
