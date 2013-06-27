package uk.ac.ebi.pride.spectracluster.normalizer;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * Normalizes a spectrum's intensities so that
 * the spectrum's total intensity matches a given
 * number.
 * 
 * This method was used by Frank et al. (2008) JPR: Clustering millions of spectra
 * To calculate the dot-product they then used
 * 1+ln(intensity) as the peak's intensity
 * @author jg
 * @author Rui Wang
 *
 * todo: spectrum total intensity calculation need to be reviewed
 *
 */
public class TotalIntensityNormalizer implements IntensityNormalizer {
	private static final double DEFAULT_TOTAL_INTENSITY = 1000;

    private final double totalIntensity;
    /**
     * Use Defaults which builds with reflection
     * Set the class with Defaults.setNormalizerClass
     */
    public TotalIntensityNormalizer() {
        this(DEFAULT_TOTAL_INTENSITY);
    }

    /**
      * Use Defaults which builds with reflection
      * Set the class with Defaults.setNormalizerClass
      */
     public TotalIntensityNormalizer(double total) {
         totalIntensity = total;
     }

    public double getTotalIntensity() {
        return totalIntensity;
    }

    @Override
    public List<IPeak> normalizePeaks(List<IPeak> peaks) {
		// get the max intensity
		Double specTotalIntensity = 0.0;

 		// create the new spectrum
		List<IPeak> normalizedSpectrum = new ArrayList<IPeak>(peaks.size());


		for (IPeak p : peaks) {
			specTotalIntensity += p.getIntensity();
        }

		// if there's no suitable max intensity, return the unchanged spectrum
		if (specTotalIntensity <= 0)
			return normalizedSpectrum;

		// calculate the ratio
		double ratio = getTotalIntensity() / specTotalIntensity;


		for (IPeak p : peaks) {
			normalizedSpectrum.add(new Peak(p.getMz(),(float)(  p.getIntensity() * ratio)));
        }

        return normalizedSpectrum;
	}

}
