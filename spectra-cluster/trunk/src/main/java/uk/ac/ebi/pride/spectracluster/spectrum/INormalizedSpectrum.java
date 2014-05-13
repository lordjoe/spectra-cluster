package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.INormalizedSpectrum
 * User: Steve
 * Date: 6/20/13
 */
public interface INormalizedSpectrum extends ISpectrum {

    /**
     * normalized spectral are normalized to a specific total intensity'this is that value
     *
     * @return as above
     */
    public double getRequiredTotalIntensity();
}
