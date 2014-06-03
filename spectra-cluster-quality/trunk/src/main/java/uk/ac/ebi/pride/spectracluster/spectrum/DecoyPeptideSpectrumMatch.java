package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class DecoyPeptideSpectrumMatch extends Spectrum implements IDecoyPeptideSpectrumMatch {

    public DecoyPeptideSpectrumMatch(ISpectrum spectrum) {
        super(spectrum);
    }

    public DecoyPeptideSpectrumMatch(ISpectrum spectrum, List<IPeak> inpeaks) {
        super(spectrum, inpeaks);
    }

    public DecoyPeptideSpectrumMatch(String id, String peptide, int precursorCharge, float precursorMz, List<IPeak> peaks) {
        super(id, precursorCharge, precursorMz,Defaults.getDefaultQualityScorer(), peaks);
     }

    public DecoyPeptideSpectrumMatch(String id, String peptide, int precursorCharge, float precursorMz, List<IPeak> peaks, String pAnnotation) {
        super(id,  precursorCharge, precursorMz, Defaults.getDefaultQualityScorer(), peaks);
        setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY,peptide);
        setProperty(KnownProperties.ANNOTATION_KEY,pAnnotation);
    }

    public DecoyPeptideSpectrumMatch(String id, String peptide, int precursorCharge,
                                     float precursorMz, List<IPeak> peaks, IQualityScorer qualityScorer, String pAnnotation) {
        super(id,  precursorCharge, precursorMz, qualityScorer, peaks);
        setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY,peptide);
        setProperty(KnownProperties.ANNOTATION_KEY,pAnnotation);
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
}
