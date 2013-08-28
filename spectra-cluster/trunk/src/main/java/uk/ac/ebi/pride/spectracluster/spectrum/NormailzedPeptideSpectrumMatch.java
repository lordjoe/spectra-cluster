package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.normalizer.IntensityNormalizer;

/**
 * uk.ac.ebi.uk.ac.ebi.pride.spectracluster.spectrum.NormailzedPeptideSpectrumMatch
 * User: Steve
 * Date: 6/20/13
 */
public class NormailzedPeptideSpectrumMatch extends PeptideSpectrumMatch {

    private final IntensityNormalizer intensityNormalizer;

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public NormailzedPeptideSpectrumMatch(ISpectrum spectrum, IntensityNormalizer intensityNormalizer) {
        super(spectrum.getId(),
                null,  // no peptide
                spectrum.getPrecursorCharge(),
                spectrum.getPrecursorMz(),
                intensityNormalizer.normalize(spectrum.getPeaks()));
        this.intensityNormalizer = intensityNormalizer;
    }

    public IntensityNormalizer getIntensityNormalizer() {
        return intensityNormalizer;
    }
}
