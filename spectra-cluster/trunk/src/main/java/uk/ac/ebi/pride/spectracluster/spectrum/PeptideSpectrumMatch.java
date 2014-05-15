package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

import java.util.List;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>  This class is effectively immutable - some measures are computed lazily but
 * it cannot be manipulated from the outside
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch extends Spectrum implements IPeptideSpectrumMatch {


    private String peptide;
    private String annotation;

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public PeptideSpectrumMatch(ISpectrum spectrum) {
        super(spectrum, spectrum.getPeaks());
        setPeptideAnnotations(spectrum);
    }

    /**
     * copy with different peaks
     *
     * @param spectrum base used for charge, mz
     * @param inpeaks    new peaks
     */
    public PeptideSpectrumMatch(ISpectrum spectrum, List<IPeak> inpeaks) {
        super(spectrum, inpeaks);
        setPeptideAnnotations(spectrum);
    }

    public PeptideSpectrumMatch(String id,
                                String peptide,
                                int precursorCharge,
                                float precursorMz,
                                List<IPeak> peaks) {
        this(id, peptide, precursorCharge, precursorMz, peaks, null);

    }


    public PeptideSpectrumMatch(String id,
                                String peptide,
                                int precursorCharge,
                                float precursorMz,
                                List<IPeak> peaks,
                                String pAnnotation) {
        super(id, precursorCharge, precursorMz, peaks);
        this.peptide = peptide;
        this.annotation = pAnnotation;
    }

    private void setPeptideAnnotations(ISpectrum spectrum) {
        if (spectrum instanceof IPeptideSpectrumMatch) {
            peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
            annotation = ((IPeptideSpectrumMatch) spectrum).getAnnotation();
        } else {
            peptide = null;
            annotation = null;
        }
    }

    /**
     * return scored peptide - maybe null
     */
    @Override
    public String getPeptide() {
        return peptide;
    }

    /**
     * return text in the id not peptide or id
     */
    @Override
    public String getAnnotation() {
        return annotation;
    }

    /**
     * true if we know this is a decoy
     *
     * @return
     */
    //todo @rw: this can be improved
    @Override
    public boolean isDecoy() {
        PSMSpectrum psm = PSMSpectrum.getSpectrum(getId());
        return psm.isDecoy();
    }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o possibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(final ISpectrum o) {
        if (!super.equivalent(o))
            return false;

        if (o instanceof PeptideSpectrumMatch) {
            PeptideSpectrumMatch realO = (PeptideSpectrumMatch) o;

            String peptide1 = getPeptide();
            String peptide2 = realO.getPeptide();
            if (!ClusterUtilities.equalObject(peptide1, peptide2))
                return false;
        }

        return true;
    }
}
