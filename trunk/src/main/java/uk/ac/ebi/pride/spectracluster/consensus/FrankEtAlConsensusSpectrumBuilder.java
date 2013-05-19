package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * Generates a consensus spectrum as described
 * by Frank etl al. (2008) JPR.
 *
 * @author jg
 */
public class FrankEtAlConsensusSpectrumBuilder implements ConsensusSpectrumBuilder {
    /**
     * The final m/z threshold to use to combine
     * peaks as identical.
     */
    private final static double DEFAULT_FINAL_MZ_THRESHOLD = 0.4;
    /**
     * The step size to use when iteratively
     * merging identical peaks. This is not
     * done with the DEFAULT_FINAL_MZ_THRESHOLD from the
     * beginning but starts from DEFUALT_MZ_THRESHOLD_STEP
     * towards DEFAULT_FINAL_MZ_THRESHOLD.
     */
    private final static double DEFUALT_MZ_THRESHOLD_STEP = 0.1;

    private IntensityNormalizer intensityNormalizer;

    public FrankEtAlConsensusSpectrumBuilder() {
        this(new TotalIntensityNormalizer());
    }

    public FrankEtAlConsensusSpectrumBuilder(IntensityNormalizer intensityNormalizer) {
        this.intensityNormalizer = intensityNormalizer;
    }

    public void setIntensityNormalizer(IntensityNormalizer intensityNormalizer) {
        this.intensityNormalizer = intensityNormalizer;
    }

    public ISpectrum buildConsensusSpectrum(ISpectrum... spectra) {
        // make sure something sensible was passed
        if (spectra.length == 0)
            return null;
        if (spectra.length == 1)
            return spectra[0];
        return buildConsensusSpectrum(Arrays.asList(spectra));
    }

    public ISpectrum buildConsensusSpectrum(List<ISpectrum> spectra) {
        // make sure something sensible was passed
        if (spectra == null || spectra.isEmpty())
            return null;

        // if there's only one spectrum in the list, return this spectrum
        if (spectra.size() == 1)
            return spectra.get(0);

        // add the peaks from all spectra to the consensus spectrum
        List<IPeak> allPeaks = addAllPeaks(spectra);
        // Note alllPeaks is sorted by MZ

        // merge identical peaks in the consensus spectrum
        List<IPeak> mergedConsensusSpectrum = mergeIdenticalPeaks(allPeaks);

        // adapt the peak intensities using the following formula: I = I * (0.95 + 0.05 * (1 + pi))^5 where pi is the peaks probability
        adaptPeakIntensities(mergedConsensusSpectrum, spectra.size());

        // filter the spectrum
        List<IPeak> filteredSpectrum = filterSpectrum(mergedConsensusSpectrum);

        // create consensus spectrum
        PeptideSpectrumMatch consensusSpectrum = createConsensusSpectrum(spectra, filteredSpectrum);

        // normalize consensus spectrum's intensity
        intensityNormalizer.normalizeSpectrum(consensusSpectrum);

        return consensusSpectrum;
    }

    /**
     * Create consensus spectrum with weighted m/z, total charge and total intensity
     */
    protected PeptideSpectrumMatch createConsensusSpectrum(Collection<ISpectrum> spectra, List<IPeak> filteredSpectrum) {
        double weightedMz = 0.0;
        double totalIntensity = 0.0;
        double totalCharge = 0.0;

        for (ISpectrum spectrum : spectra) {
            weightedMz += spectrum.getPrecursorMz() * spectrum.getPrecursorIntensity();
            // update the average intensity
            totalIntensity += spectrum.getPrecursorIntensity();
            // update the average charge
            totalCharge += spectrum.getPrecursorCharge();
        }

        weightedMz /= totalIntensity;

        return new PeptideSpectrumMatch(null, null, totalCharge / spectra.size(), weightedMz, totalIntensity, filteredSpectrum);
    }

    /**
     * Filters the passed spectrum keeping only the
     * top 5 peaks per 100 Da
     */
    // todo  using ClusterUtilities.getHighestInBins
    protected List<IPeak> filterSpectrum(List<IPeak> mergedConsensusSpectrum) {
        // expect to keep 1% - just a wild guess
        List<IPeak> filteredSpectrum = new ArrayList<IPeak>(mergedConsensusSpectrum.size() / 100);

        // process the peaks using the sliding window
        for (double startMz = 0, endMz = 100; endMz <= 5000; endMz += 100, startMz += 100) {
            List<IPeak> peakBuffer = new ArrayList<IPeak>();

            // fill the peak buffer with all peaks within that range
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

    protected void adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        for (IPeak p : mergedConsensusSpectrum) {
            double peakProbability = (double) p.getCount() / numberOfSpectra;
            double newIntensity = p.getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5));
            p.setIntensity(newIntensity);
        }
    }

    /**
     * Adds all peaks from the passed List of
     * spectra to the consensus spectrum.
     */
    protected List<IPeak> addAllPeaks(Collection<ISpectrum> spectra) {
        List<IPeak> allPeaks = new ArrayList<IPeak>();

        // process the spectra
        for (ISpectrum spectrum : spectra) {
            allPeaks.addAll(spectrum.getPeaks());
        }

        if (allPeaks.size() == 0)
            return allPeaks;
        // sort by m/z allPeaks
        Collections.sort(allPeaks, PeakMzComparator.getInstance());
        List<IPeak> returnedPeaks = new ArrayList<IPeak>();
        IPeak start = null;
        for (IPeak allPeak : allPeaks) {
            if (allPeak.getIntensity() == 0)
                continue;
            if (start == null) {    // grab a peak
                start = allPeak;
                continue;
            }
            // re have a peak - start and another peak allpeak - do we merge
            double diff = allPeak.getMz() - start.getMz();
            if (diff < 0.00001) {
                double intensity = start.getIntensity() + allPeak.getIntensity();
                int count = start.getCount() + allPeak.getCount();
                double mz = start.getMz();
                start = new Peak(mz, intensity, count);
            } else {
                returnedPeaks.add(start); // not merging
                start = allPeak;  //  start is next peak
            }

        }
        return returnedPeaks;

    }

    /**
     * old code compare
     *
     * @param spectra
     * @return
     */
    protected Collection<IPeak> originalAddAllPeaks(Collection<ISpectrum> spectra) {
        // initialize the consensus spectrum - expect 2000 peaks for the beginning
        Map<Double, IPeak> consensusSpectrum = new HashMap<Double, IPeak>(2000);

        // process the spectra
        for (ISpectrum spectrum : spectra) {
            // process the current spectrum's peaks
            for (IPeak peak : spectrum.getPeaks()) {
                double mz = peak.getMz();
                double intensity = peak.getIntensity();

                // ignore 0 intensity peaks
                if (intensity <= 0) {
                    continue;
                }

                // if the consensus spectrum doesn't contain the peak yet, simply add it
                if (!consensusSpectrum.containsKey(mz)) {
                    consensusSpectrum.put(mz, peak);
                } else {
                    // if the peak already exists, sum it up
                    consensusSpectrum.put(mz, new Peak(mz, consensusSpectrum.get(mz).getIntensity() + intensity, consensusSpectrum.get(mz).getCount() + 1));
                }
            }
        }

        return consensusSpectrum.values();
    }

//    /**
//     * @param consensusSpectrum
//     */
//    private List<IPeak> mergeIdenticalPeaks(List<IPeak> peaks, double range) {
//        ListIterator<IPeak> peakIter = peaks.listIterator();
//        List<IPeak> returned = new ArrayList<IPeak>();
//        IPeak currentPeak;
//        IPeak previousPeak;
//        while (peakIter.hasNext()) {
//            currentPeak = peakIter.next();
//            if (peakIter.hasPrevious()) {
//                previousPeak = peakIter.previous();
//
//                if (previousPeak.getMz() <= currentPeak.getMz() + range) {
//                    // calculate the new weighted m/z
//                    double weightedMz = (previousPeak.getIntensity() * previousPeak.getMz() + currentPeak.getIntensity() * currentPeak.getMz()) / (previousPeak.getIntensity() + currentPeak.getIntensity());
//
//                    Peak mergedPeak = new Peak(weightedMz, previousPeak.getIntensity() + currentPeak.getIntensity(), previousPeak.getCount() + currentPeak.getCount());
//
//                    returned.add(mergedPeak);
//                } else {
//                    returned.add(currentPeak);
//                }
//
//            } else {
//                returned.add(currentPeak);
//            }
//        }
//        return returned;
//    }


    /**
     * @param consensusSpectrum
     */
    protected List<IPeak> mergeIdenticalPeaks(List<IPeak> consensusSpectrum) {
        // convert the spectrum into a list of Peaks
        List<IPeak> peaks = new ArrayList<IPeak>(consensusSpectrum);

        // sort the list according to m/z values
        // only need to sort once
        // NOt needed because the input is already sorted
        //   Collections.sort(peaks, PeakMzComparator.getInstance());

        // based on the set range
        for (double range = DEFUALT_MZ_THRESHOLD_STEP; range <= DEFAULT_FINAL_MZ_THRESHOLD; range += DEFUALT_MZ_THRESHOLD_STEP) {

            ListIterator<IPeak> peakIter = peaks.listIterator();

            IPeak currentPeak;
            IPeak previousPeak;
            while (peakIter.hasNext()) {
                currentPeak = peakIter.next();
                if (peakIter.hasPrevious()) {
                    previousPeak = peakIter.previous();

                    if (previousPeak.getMz() <= currentPeak.getMz() + range) {
                        // calculate the new weighted m/z
                        double weightedMz = (previousPeak.getIntensity() * previousPeak.getMz() + currentPeak.getIntensity() * currentPeak.getMz()) / (previousPeak.getIntensity() + currentPeak.getIntensity());

                        Peak mergedPeak = new Peak(weightedMz, previousPeak.getIntensity() + currentPeak.getIntensity(), previousPeak.getCount() + currentPeak.getCount());

                        peakIter.remove();

                        peakIter.next();
                        peakIter.set(mergedPeak);
                    }
                }
            }
        }

        return peaks;
    }
}
