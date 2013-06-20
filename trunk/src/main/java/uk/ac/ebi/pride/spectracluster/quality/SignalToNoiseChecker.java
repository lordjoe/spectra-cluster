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
        IPeaksSpectrum highestNPeaks = spectrum.getHighestNPeaks(NUMBER_HIGH_PEAKS);

        // TODO: react to small spectra - this is johannes todo
        if (highestNPeaks.getPeaksCount() < NUMBER_HIGH_PEAKS)
            return 0.0;
        double totalIntensity = highestNPeaks.getTotalIntensity();
        double highestPeak = 0;
        for (IPeak peak : highestNPeaks.getPeaks()) {
            highestPeak = Math.max(peak.getIntensity(), highestPeak);
        }

        double meanHigh = (totalIntensity - highestPeak) / (NUMBER_HIGH_PEAKS - 1);

        List<IPeak> peaks = new ArrayList<IPeak>(spectrum.getPeaks());
        Collections.sort(peaks, PeakIntensityComparator.INSTANCE);

        double median;

        int peakSize = peaks.size();
        if (peakSize % 2 == 1) {
            int index = peakSize / 2;
            IPeak iPeak = peaks.get(index);
            median = iPeak.getIntensity(); // odd case

        } else {
            int index2 = (peakSize / 2);
            int index1 = index2 - 1;
            IPeak iPeak1 = peaks.get(index1);
            IPeak iPeak2 = peaks.get(index2);
            double intensity1 = iPeak1.getIntensity();
            double intensity2 = iPeak2.getIntensity();
            median = (intensity1 + intensity2) / 2; // even case
        }

        return meanHigh / median;
    }
}
