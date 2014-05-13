package uk.ac.ebi.pride.spectracluster.spectrum;


/**
 * uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum {

    public static final int MAJOR_PEAK_NUMBER = 6; // Frank et al does 5 we do 1 more

    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();


    /**
     * return text in the id not peptide or id
     */
    public String getAnnotation();

    /**
     * write basic data as TSV
     *
     * @param out
     */
    public void appendTSV(Appendable out);

    /**
     * true if we know this is a decoy
     *
     * @return
     */
    public boolean isDecoy();

}
