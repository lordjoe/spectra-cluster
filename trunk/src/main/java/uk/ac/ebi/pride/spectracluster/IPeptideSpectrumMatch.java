package uk.ac.ebi.pride.spectracluster;

/**
 * uk.ac.ebi.pride.spectracluster.IScoredSpectrum
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum  {
    public static IPeptideSpectrumMatch[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IPeptideSpectrumMatch.class;

    /**
     * return scored peptide - maybe null
     * @return
     */
    public String getPeptide();
}
