package uk.ac.ebi.pride.spectracluster.normalizer;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

/**
 * Normalizes a spectrum's intensities.
 * @author Rui Wang
 */
public interface IntensityNormalizer {
	/**
	 * Normalizes the given spectrum's intensities.
	 * @param spectrum The spectrum as a Map with the m/z values as key and their intensities as values.
	 */
	public void normalizeSpectrum(ISpectrum spectrum);
}
