package uk.ac.ebi.pride.spectracluster.spectrum;


/**
 * uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
@Deprecated
public interface IPeptideSpectrumMatch extends ISpectrum {

    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();


    /**
     * return text in the id not peptide or id
     */
    public String getAnnotation();

    /**
     * true if we know this is a decoy
     *
     * @return
     */
    public boolean isDecoy();

}
