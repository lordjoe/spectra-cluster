package uk.ac.ebi.pride.spectracluster.similarity;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * Assesses the similarity between two
 * spectra and returns the result of this
 * check as a double. Higher values mean
 * a higher similarity.
 * @author jg
 * @author Rui Wang
 *
 */
public interface SimilarityChecker {
    /**
     * erturn the default similarity Threshold
     * @return as above
     */
    public double getDefaultThreshold();

	/**
	 * Assesses the similarity between the two passed
	 * spectra and returns the result of this assessment
	 * as a double. A higher number reflects a higher
	 * grade of similarity.
	 * @param spectrum1 The first spectrum to compare. The list of Peaks MUST be sorted according to intensity.
	 * @param spectrum2 The second spectrum to compare. The list of Peaks MUST be sorted according to intensity.
	 * @return A score indicating the similarity between the two passed spectra.
	 */
	public double assessSimilarity(IPeptideSpectrumMatch spectrum1, IPeptideSpectrumMatch spectrum2);
}
