package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.consensus.FrankEtAlConsensusSpectrumBuilder
 * Generates a consensus spectrum as described
 * by Frank etl al. (2008) JPR.
 *
 * @author jg
 * Debrecated - use ConsensusSpectrum
 */
@Deprecated
@SuppressWarnings({  "deprecation"})
public class FrankEtAlConsensusSpectrumBuilder implements ConsensusSpectrumBuilder {
    public static final String VERSION = "1.0";

    public static final int SLIDING_WINDOW_RETAINED_HIGHEST_PEAKS = 5;
    /**
     * width in daltons of the sliding window
     */
    public static final int SLIDING_WINDOW_WIDTH = 100; // mz
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
        throw new UnsupportedOperationException("Deprecated");
    }

    public FrankEtAlConsensusSpectrumBuilder(IntensityNormalizer intensityNormalizer) {
        this.intensityNormalizer = intensityNormalizer;
        throw new UnsupportedOperationException("Deprecated");
    }

    public void setIntensityNormalizer(IntensityNormalizer intensityNormalizer) {
        this.intensityNormalizer = intensityNormalizer;
    }


    /**
       * return a name which should not change
       *
       * @return !null name
       */
      @Override
      public String getName() {
          return getClass().getSimpleName();
      }

      /**
       * return a version number - this may be updated over time
       *
       * @return !null version
       */
      @Override
      public String getCurrentVersion() {
          return VERSION;
      }


    /**
     * Creates a consensus spectrum based on the
     * passed list of cluster.
     *
     * @param cluster A list of cluster as sorted peak lists according to intensity.
     * @return A list of Peaks sorted according to their intensities.
     */
    @Override
    public ISpectrum buildConsensusSpectrum(ISpectralCluster cluster, Object... otherData) {
        final List<ISpectrum> clusteredSpectra = cluster.getHighestQualitySpectra();     // concensus only on highest quality
   //     final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
         return buildConsensusSpectrum(clusteredSpectra);
    }


    protected ISpectrum buildConsensusSpectrum(ISpectrum... spectra) {
        // make sure something sensible was passed
        if (spectra.length == 0)
            return null;
        if (spectra.length == 1)
            return spectra[0];
        return buildConsensusSpectrum(Arrays.asList(spectra));
    }

    /**
     * interface now takes a cluster
     *
     * @param spectra
     * @return
     */
    public ISpectrum buildConsensusSpectrum(List<ISpectrum> spectra) {
        // make sure something sensible was passed
        if (spectra == null || spectra.isEmpty())
            return null;

        if (spectra.size() == 1)
             return spectra.get(0);   // ok 1 is a concensus

        // add the peaks from all spectra to the consensus spectrum
        List<IPeak> allPeaks = addAllPeaks(spectra);
        // Note alllPeaks is sorted by MZ

        // merge identical peaks in the consensus spectrum
        List<IPeak> mergedConsensusSpectrum = mergeIdenticalPeaks(allPeaks);

        // adapt the peak intensities using the following formula: I = I * (0.95 + 0.05 * (1 + pi))^5 where pi is the peaks probability
        mergedConsensusSpectrum = adaptPeakIntensities(mergedConsensusSpectrum, spectra.size());

        // filter the spectrum
        List<IPeak> filteredSpectrum = filterSpectrum(mergedConsensusSpectrum);

        // normalize consensus spectrum's intensity
        filteredSpectrum = intensityNormalizer.normalizePeaks(filteredSpectrum);

        // create consensus spectrum
        PeptideSpectrumMatch consensusSpectrum = createConsensusSpectrum(spectra, filteredSpectrum);

        return consensusSpectrum;
    }

    /**
     * Create consensus spectrum with weighted m/z, total charge and total intensity
     */
    protected PeptideSpectrumMatch createConsensusSpectrum(Collection<ISpectrum> spectra, List<IPeak> filteredSpectrum) {
        float totalMz = 0.0F;
        int totalCharge = 0;

        for (ISpectrum spectrum : spectra) {
            totalMz += spectrum.getPrecursorMz();
            // update the average charge
            totalCharge += spectrum.getPrecursorCharge();
        }

        totalMz /= spectra.size();

        final int precursorCharge = (int) ((totalCharge + 0.5) / spectra.size());
        return new PeptideSpectrumMatch(null, null, precursorCharge, totalMz, filteredSpectrum);
    }

    /**
     * Filters the passed spectrum keeping only the
     * top 5 peaks per 100 Da
     */
    // todo  using ClusterUtilities.getHighestInBins
    protected List<IPeak> filterSpectrum(List<IPeak> mergedConsensusSpectrum) {
        // expect to keep 1% - just a wild guess
        List<IPeak> filteredSpectrum = new ArrayList<IPeak>();

        if (mergedConsensusSpectrum.size() == 0)
            return filteredSpectrum; // should never happen

        int index = 0;
        // process the peaks using the sliding window
        for (double startMz = 0, endMz = SLIDING_WINDOW_WIDTH; endMz <= IPeak.HIGHEST_USABLE_MZ; endMz += SLIDING_WINDOW_WIDTH, startMz += SLIDING_WINDOW_WIDTH) {
            List<IPeak> peakBuffer = new ArrayList<IPeak>();

            // fill the peak buffer with all peaks within that range
            IPeak p = mergedConsensusSpectrum.get(index++);
            while (index < mergedConsensusSpectrum.size() && p.getMz() < startMz) {
                p = mergedConsensusSpectrum.get(index++);
            }
            while (index < mergedConsensusSpectrum.size() && p.getMz() < endMz) {
                peakBuffer.add(p);
                p = mergedConsensusSpectrum.get(index++);
            }

            // sort the buffer
            Collections.sort(peakBuffer, PeakIntensityComparator.getInstance());

            // take the 5 highest peaks
            for (int i = 0; i < Math.min(SLIDING_WINDOW_RETAINED_HIGHEST_PEAKS, peakBuffer.size()); i++) {
                filteredSpectrum.add(peakBuffer.get(i));
            }
            if (index >= mergedConsensusSpectrum.size())
                break;
        }

        return filteredSpectrum;
    }

    protected List<IPeak> adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        List<IPeak> holder = new ArrayList<IPeak>();


        for (IPeak p : mergedConsensusSpectrum) {
            int peakCount = p.getCount();
            double peakProbability = (double) peakCount / numberOfSpectra;
            double factor = 0.95 + 0.05 * Math.pow(1 + peakProbability, 5);
            double newIntensity = p.getIntensity() * factor;
            IPeak added = new Peak(p.getMz(), (float) newIntensity, peakCount);
            holder.add(added);
        }
        return holder;
    }

    /**
     * Adds all peaks from the passed List of
     * spectra to the consensus spectrum.
     */
    public List<IPeak> addAllPeaks(Collection<ISpectrum> spectra) {
        List<IPeak> allPeaks = new ArrayList<IPeak>();

        // process the spectra
        for (ISpectrum spectrum : spectra) {
            for (IPeak peak : spectrum.getPeaks()) {
                allPeaks.add(peak);
            }
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
                float intensity = start.getIntensity() + allPeak.getIntensity();
                int count = start.getCount() + allPeak.getCount();
                float mz = start.getMz();
                start = new Peak(mz, intensity, count);
            }
            else {
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
                        float weightedMz = (previousPeak.getIntensity() * previousPeak.getMz() + nextPeak.getIntensity() * nextPeak.getMz()) / (previousPeak.getIntensity() + nextPeak.getIntensity());

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

//    /**
//     * @param consensusSpectrum
//     */
//    public List<IPeak> mergeIdenticalPeaksInternal(List<IPeak> consensusSpectrum) {
//
//        // convert the spectrum into a list of Peaks
//        List<IPeak> peaks = new ArrayList<IPeak>();
//
//        float sumMz = 0;
//        float sumIntensity = 0;
//        int currentCount = 0;
//        for (int i = 0; i < consensusSpectrum.size(); i++) {
//            IPeak testPeak = consensusSpectrum.get(i);
//            final int count = testPeak.getCount();
//            if (currentCount == 0) {
//                currentCount = count;
//                sumMz = testPeak.getMz() * count;
//                sumIntensity = testPeak.getIntensity() * count;
//
//            }
//            else {
//                float currentMz = sumMz / currentCount;
//                double del = testPeak.getMz() - currentMz;
//                if (del < 0)
//                    throw new IllegalStateException("peaks not sorted");
//                if (del <= DEFUALT_MZ_THRESHOLD_STEP) {
//                    currentCount += count;
//                    sumMz += testPeak.getMz() * count;
//                    sumIntensity += testPeak.getIntensity() * count;
//
//                }
//                else {
//                    peaks.add(new Peak(currentMz, sumIntensity , currentCount));   // add current
//                    currentCount = count;  // start with new
//                    sumMz = testPeak.getMz() * count;
//                    sumIntensity = testPeak.getIntensity() * count;
//                    currentMz = sumMz / count;
//                }
//            }
//
//        }
//        if (currentCount > 0) {
//            peaks.add(new Peak(sumMz / currentCount, sumIntensity, currentCount));   // add current
//        }
//
//
//        return peaks;
//    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FrankEtAlConsensusSpectrumBuilder that = (FrankEtAlConsensusSpectrumBuilder) o;

        if (intensityNormalizer != null ? !intensityNormalizer.equals(that.intensityNormalizer) : that.intensityNormalizer != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return intensityNormalizer != null ? intensityNormalizer.hashCode() : 0;
    }
}
