package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.consensus.JohannesSpectrumBuilder
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class JohannesSpectrumBuilder {
    /**
     * The final m/z threshold to use to combine
     * peaks as identical.
     */
    private double finalMzThreshold = 0.4;
    /**
     * The step size to use when iteratively
     * merging identical peaks. This is not
     * done with the finalMzThreshold from the
     * beginning but starts from mzThresholdStep
     * towards finalMzThreshold.
     */
    private double mzThresholdStep = 0.1;

    public List<IPeak> buildConsensusSpectrum(List<List<IPeak>> spectra) {
        // make sure something sensible was passed
        if (spectra == null || spectra.size() < 1)
            return null;

        // if there's only one spectrum in the list, return this spectrum
        if (spectra.size() == 1)
            return spectra.get(0);

        // initialize the consensus spectrum - expect 2000 peaks for the beginning
        Map<Double, IPeak> consensusSpectrum = new HashMap<Double, IPeak>(2000);

        // add the peaks from all spectra to the consensus spectrum
        addAllPeaks(consensusSpectrum, spectra);

        // merge identical peaks in the consensus spectrum
        List<IPeak> mergedConsensusSpectrum = mergeIdenticalPeaks(consensusSpectrum);

        List<IPeak> inOrder = filterNulls(mergedConsensusSpectrum);

        // adapt the IPeak intensities using the following formula: I = I * (0.95 + 0.05 * (1 + pi))^5 where pi is the peaks probability
        mergedConsensusSpectrum = adaptPeakIntensities(mergedConsensusSpectrum, spectra.size());

        // filter the spectrum
        List<IPeak> filteredSpectrum = filterSpectrum(mergedConsensusSpectrum);

        // sort the spectrum according to intensities
        Collections.sort(filteredSpectrum, PeakIntensityComparator.getInstance());

        return filteredSpectrum;
    }

   protected static List<IPeak> filterNulls(List<IPeak> withNulls) {
        List<IPeak> holder = new ArrayList<IPeak>();

        for (IPeak withNull : withNulls) {
           if(withNull != null)
               holder.add(withNull);
        }
        Collections.sort(holder,PeakMzComparator.getInstance());
        return holder;

    }

    /**
     * Filters the passed spectrum keeping only the
     * top 5 peaks per 100 Da
     *
     * @param mergedConsensusSpectrum
     * @return
     */
    private List<IPeak> filterSpectrum(
            List<IPeak> mergedConsensusSpectrum) {
        // expect to keep 1% - just a wild guess
        List<IPeak> filteredSpectrum = new ArrayList<IPeak>(mergedConsensusSpectrum.size() / 100);

        // sort the passed spectrum
        Collections.sort(mergedConsensusSpectrum, PeakMzComparator.getInstance());

        // process the peaks using the sliding window
        for (double startMz = 0, endMz = 100; endMz <= 5000; endMz += 100, startMz += 100) {
            List<IPeak> peakBuffer = new ArrayList<IPeak>();

            // fill the IPeak buffer with all peaks within that range
            for (IPeak p : mergedConsensusSpectrum) {
                if (p.getMz() < startMz)
                    continue;
                if (p.getMz() > endMz)
                    break;

                peakBuffer.add(p);
            }

            // sort the buffer
            Collections.sort(peakBuffer, PeakIntensityComparator.getInstance());

            // take the 5 highest peaks
            for (int i = peakBuffer.size() - 1, counter = 0; i >= 0 && counter < 5; i--, counter++)
                filteredSpectrum.add(peakBuffer.get(i));
        }

        return filteredSpectrum;
    }

    private List<IPeak> adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        List<IPeak> adaptedSpectrum = new ArrayList<IPeak>(mergedConsensusSpectrum.size());

        for (IPeak p : mergedConsensusSpectrum) {
            if (p == null)
                continue;

            double peakProbability = (double) p.getCount() / numberOfSpectra;
            double newIntensity = p.getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5));

            adaptedSpectrum.add(new Peak(p.getMz(), newIntensity, p.getCount()));
        }

        return adaptedSpectrum;
    }

    /**
     * Adds all peaks from the passed List of
     * spectra to the consensus spectrum.
     *
     * @param consensusSpectrum
     * @param spectra
     */
    private void addAllPeaks(Map<Double, IPeak> consensusSpectrum,
                             List<List<IPeak>> spectra) {
        // make sure the parameters are valid
        if (spectra == null || spectra.size() < 1)
            return;
        if (consensusSpectrum == null)
            return;

        // process the spectra
        for (List<IPeak> spectrum : spectra) {
            // process the current spectrum's peaks
            for (IPeak IPeak : spectrum) {
                // ignore 0 intensity peaks
                if (IPeak.getIntensity() == 0)
                    continue;

                // if the consensus spectrum doesn't contain the IPeak yet, simply add it
                if (!consensusSpectrum.containsKey(IPeak.getMz())) {
                    consensusSpectrum.put(IPeak.getMz(), IPeak);
                } else {
                    // if the IPeak already exists, sum it up
                    consensusSpectrum.put(IPeak.getMz(), new Peak(
                            IPeak.getMz(),
                            consensusSpectrum.get(IPeak.getMz()).getIntensity() + IPeak.getIntensity(),
                            consensusSpectrum.get(IPeak.getMz()).getCount() + 1)
                    );
                }
            }
        }
    }

    /**
     * @param consensusSpectrum
     */
    private List<IPeak> mergeIdenticalPeaks(Map<Double, IPeak> consensusSpectrum) {
        // convert the spectrum into a list of Peaks
        List<IPeak> peaks = new ArrayList<IPeak>(consensusSpectrum.values());

        // based on the set range
        for (double range = mzThresholdStep; range <= finalMzThreshold; range += mzThresholdStep) {
            // sort the list according to m/z values
            Collections.sort(peaks, PeakMzComparator.getInstance());

            // as the list is sorted, peaks only have to be checked in one "direction"
            for (int i = 0; i < peaks.size() - 1; i++) {
                IPeak current = peaks.get(i);
                IPeak next = peaks.get(i + 1);

                if (current == null || next == null)
                    continue;

                // check if the next IPeak falls within the range
                if (next.getMz() <= current.getMz() + range) {
                    // calculate the new weighted m/z
                    double weightedMz = (next.getIntensity() * next.getMz() + current.getIntensity() * current.getMz()) / (next.getIntensity() + current.getIntensity());

                    IPeak mergedPeak = new Peak(weightedMz, current.getIntensity() + next.getIntensity(), current.getCount() + next.getCount());

                    // remove the current IPeak from the array
                    peaks.set(i, null);
                    // set the next IPeak to the merged one
                    peaks.set(i + 1, mergedPeak);
                }
            }
        }

        return peaks;
    }
}
