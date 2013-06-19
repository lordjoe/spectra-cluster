package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

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


    /**
      * Use Defaults which builds with reflection
      * Set the class with Defaults.setConsensusSpectrumBuilderClass
      */
    public FrankEtAlConsensusSpectrumBuilder() {
        this(Defaults.INSTANCE.getDefaultIntensityNormalizer());
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
        if (spectra.size() == 1) {
            ISpectrum singleSpectrum = spectra.get(0);
            List<IPeak> singleSpectrumPeaks = intensityNormalizer.normalizePeaks(singleSpectrum.getPeaks());
            return new PeptideSpectrumMatch(singleSpectrum.getId(),
                                            null,
                                            singleSpectrum.getPrecursorCharge(),
                                            singleSpectrum.getPrecursorMz(), singleSpectrumPeaks);
        }

        // add the peaks from all spectra to the consensus spectrum
        List<IPeak> allPeaks = addAllPeaks(spectra);
        // Note alllPeaks is sorted by MZ

        // merge identical peaks in the consensus spectrum
        List<IPeak> mergedConsensusSpectrum = mergeIdenticalPeaks(allPeaks);

        // adapt the peak intensities using the following formula: I = I * (0.95 + 0.05 * (1 + pi))^5 where pi is the peaks probability
        adaptPeakIntensities(mergedConsensusSpectrum, spectra.size());

        // filter the spectrum
        List<IPeak> filteredSpectrum = filterSpectrum(mergedConsensusSpectrum);

        // normalize consensus spectrum's intensity
//        filteredSpectrum = intensityNormalizer.normalizePeaks(filteredSpectrum);

        // create consensus spectrum
        PeptideSpectrumMatch consensusSpectrum = createConsensusSpectrum(spectra, filteredSpectrum);


        return consensusSpectrum;
    }

    /**
     * Create consensus spectrum with weighted m/z, total charge and total intensity
     */
    protected PeptideSpectrumMatch createConsensusSpectrum(Collection<ISpectrum> spectra, List<IPeak> filteredSpectrum) {
        double totalMz = 0.0;
        double totalCharge = 0.0;

        for (ISpectrum spectrum : spectra) {
            totalMz += spectrum.getPrecursorMz();
            // update the average charge
            totalCharge += spectrum.getPrecursorCharge();
        }

        totalMz /= spectra.size();

        return new PeptideSpectrumMatch(null, null, totalCharge / spectra.size(), totalMz, filteredSpectrum);
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
            for (int i = 0; i < Math.min(5,peakBuffer.size()); i++) {
                filteredSpectrum.add(peakBuffer.get(i));
             }
          }

        return filteredSpectrum;
    }

    protected void adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        for (IPeak p : mergedConsensusSpectrum) {
            int peakCount = p.getCount();
            double peakProbability = (double) peakCount / numberOfSpectra;
            double factor = 0.95 + 0.05 * Math.pow(1 + peakProbability, 5);
            if (Math.abs(p.getMz() - 211) < 1) {
               // break here
               peakCount = p.getCount();
            }
            double newIntensity = p.getIntensity() * factor;
            p.setIntensity(newIntensity);
        }
    }

    /**
     * Adds all peaks from the passed List of
     * spectra to the consensus spectrum.
     */
    public List<IPeak> addAllPeaks(Collection<ISpectrum> spectra) {
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
        int index = 0;
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
            index++;
        }
        returnedPeaks.add(start); //  add last peak
        return returnedPeaks;
    }

    /**
     * @param consensusSpectrum
     */
    public List<IPeak> mergeIdenticalPeaks(List<IPeak> consensusSpectrum) {
        // convert the spectrum into a list of Peaks
        List<IPeak> peaks = new ArrayList<IPeak>(consensusSpectrum);

        // based on the set range
        for (double range = DEFUALT_MZ_THRESHOLD_STEP; range <= DEFAULT_FINAL_MZ_THRESHOLD; range += DEFUALT_MZ_THRESHOLD_STEP) {

            ListIterator<IPeak> peakIter = peaks.listIterator();

            IPeak nextPeak;
            IPeak previousPeak;
            while (peakIter.hasNext()) {
                if (peakIter.hasPrevious()) {
                    // step backward one element
                    previousPeak = peakIter.previous();

                    // step forward two elements
                    peakIter.next();
                    nextPeak = peakIter.next();

                    if (previousPeak.getMz() + range >= nextPeak.getMz()) {
                        // calculate the new weighted m/z
                        double weightedMz = (previousPeak.getIntensity() * previousPeak.getMz() + nextPeak.getIntensity() * nextPeak.getMz()) / (previousPeak.getIntensity() + nextPeak.getIntensity());

                        Peak mergedPeak = new Peak(weightedMz, previousPeak.getIntensity() + nextPeak.getIntensity(), previousPeak.getCount() + nextPeak.getCount());

                        peakIter.previous();
                        peakIter.previous();
                        peakIter.remove();

                        peakIter.next();
                        peakIter.set(mergedPeak);
                    }
                } else {
                    peakIter.next();
                }
            }
        }

        return peaks;
    }

    private List<IPeak> originalMergeIdenticalPeaks(List<IPeak> peaks) {

        // based on the set range
        for (double range = DEFUALT_MZ_THRESHOLD_STEP; range <= DEFAULT_FINAL_MZ_THRESHOLD; range += DEFUALT_MZ_THRESHOLD_STEP) {
            // sort the list according to m/z values
            Collections.sort(peaks, PeakMzComparator.getInstance());

            // as the list is sorted, peaks only have to be checked in one "direction"
            for (int i = 0; i < peaks.size() - 1; i++) {
                IPeak current = peaks.get(i);
                IPeak next    = peaks.get(i + 1);

                if (current == null || next == null)
                    continue;

                // check if the next peak falls within the range
                if (next.getMz() <= current.getMz() + range) {
                    // calculate the new weighted m/z
                    double weightedMz = (next.getIntensity() * next.getMz() + current.getIntensity() * current.getMz()) / (next.getIntensity() + current.getIntensity());

                    Peak mergedPeak = new Peak(weightedMz, current.getIntensity() + next.getIntensity(), current.getCount() + next.getCount());

                    // remove the current peak from the array
                    peaks.set(i, null);
                    // set the next peak to the merged one
                    peaks.set(i + 1, mergedPeak);
                }
            }
        }

        return peaks;
    }
}
