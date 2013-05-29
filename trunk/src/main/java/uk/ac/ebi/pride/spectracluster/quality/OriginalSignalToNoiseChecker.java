package uk.ac.ebi.pride.spectracluster.quality;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Assesses a spectrum's signal-to-noise
 * ratio as a measure of the spectrum's
 * quality. The method used is taken from
 * Lam et al. (2008), Nat Methods 5(10):873-875 
 * @author jg
 *
 */
public class OriginalSignalToNoiseChecker implements QualityScorer {

	/**
	 * Calculates a spectrum's signal-to-noise ratio
	 * by taking the 2nd-6th highest peak's intensity
	 * and dividing it by the median intensity of all
	 * peaks.
	 * This method should only be used on normalized
	 * spectra.
	 */
    @Override
    public double calculateQualityScore(ISpectrum spectrum) {
        // get the intensities
        ArrayList<Double> intensities = new ArrayList<Double>(spectrum.getPeaks().size());
        for (IPeak p : spectrum.getPeaks())
            intensities.add(p.getIntensity());

        Collections.sort(intensities);

        // get the total intensity of the 2nd-6th highest peak
        double highestPeakIntensity = 0.0;

        // TODO: react to small spectra
        if (intensities.size() < 6)
            return 0.0;

        for (int n = intensities.size() - 6; n < intensities.size() - 1; n++)
            highestPeakIntensity += intensities.get(n);

        highestPeakIntensity = highestPeakIntensity / 5;

        // get the median
        double nPeaks = intensities.size();
        double median = 0.0;

        // check if there's an even number of peaks
        if (nPeaks %2 == 0) {
            int index1 = (int) nPeaks / 2 - 1;
            Double intensity1 = intensities.get(index1);
            int index2 = (int) nPeaks / 2;
            Double intensity2 = intensities.get(index2);
            median = (intensity1 + intensity2) / 2;
        }
        else    {
            int index = (int) nPeaks / 2;
            median = intensities.get(index);
        }

        return highestPeakIntensity / median;
    }
}
