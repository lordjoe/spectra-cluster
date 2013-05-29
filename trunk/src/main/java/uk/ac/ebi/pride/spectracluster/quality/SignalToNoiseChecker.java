package uk.ac.ebi.pride.spectracluster.quality;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker
 *
 * @author Steve Lewis
 * @date 29/05/13
 */
public class SignalToNoiseChecker implements QualityScorer {

    public static final int NUMBER_HIGH_PEAKS = 6;
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
        ISpectrum highestNPeaks = spectrum.getHighestNPeaks(NUMBER_HIGH_PEAKS);

         // TODO: react to small spectra
		if (highestNPeaks.getPeaksCount() < NUMBER_HIGH_PEAKS)
			return 0.0;
        double meanHigh = highestNPeaks.getTotalIntensity() / NUMBER_HIGH_PEAKS;

        List<IPeak> peaks = spectrum.getPeaks();
        Collections.sort(peaks,PeakIntensityComparator.INSTANCE);

        double median;

        if(peaks.size() % 2 == 1)  {
            IPeak iPeak = peaks.get((peaks.size() / 2) + 1);
            median = iPeak.getIntensity(); // odd case

        }
        else {
            IPeak iPeak1 = peaks.get((peaks.size() / 2));
            IPeak iPeak2 = peaks.get((peaks.size() / 2) + 1);
            double intensity1 = iPeak1.getIntensity();
            double intensity2 = iPeak2.getIntensity();
             median = (intensity1 + intensity2) / 2; // even case
         }

        return meanHigh / median;
     }
}
