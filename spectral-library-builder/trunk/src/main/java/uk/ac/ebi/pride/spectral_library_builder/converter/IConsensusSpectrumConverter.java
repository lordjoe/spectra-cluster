package uk.ac.ebi.pride.spectral_library_builder.converter;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectral_library_builder.util.ParameterExtractor;

/**
 * Created by jg on 07.12.14.
 */
public interface IConsensusSpectrumConverter {
    public String convertConsensusSpectrum(ISpectrum spectrum, ParameterExtractor parameterExtractor);
}
