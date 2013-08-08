package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * This is a rewrite of the original FrankEtAlConsensuSpectrumBuilder and produces nearly identical results
 * as the original class. The differences that are currently observed only occur under special circumstances and
 * are caused by rounding differences between double and float. This may lead to different peak merging results.
 * <p/>
 * The main difference of this class to the original one is that the consensus spectrum is generated from one
 * internal "crowded" spectrum. This "crowded" spectrum contains all peaks of all added spectra. Thereby, when new
 * spectra are added or removed it is not necessary to reprocess all previous spectra.
 * <p/>
 * Theoretically, memory can be saved since identical peaks between different spectra are only stored once. This effect
 * can be increased by rounding the m/z value to f.e. 3 digits after the comma (this does not influence the final
 * consensus spectrum). Since this change increased the processing time of adding spectra at least two fold, it was
 * removed again. The function "round" that was used to the rounding is still part of the class.
 * NOTE This is Johannes code refactored so that critical methods take and return peak lists rather than
 * using an internal list - this allows steps to be individually rewritten and tested
 * Also internal methods are protected allowing tests to access them
 */
public class OriginalConsensusSpectrum implements IConsensusSpectrumBuilder {

    public static final float NOISE_FILTER_INCREMENT = 100;

    public static final ConcensusSpectrumBuilderFactory FACTORYX = new ConsensusSpectrumFactory();

    public static class ConsensusSpectrumFactory implements ConcensusSpectrumBuilderFactory {
        private ConsensusSpectrumFactory() {
        }

        /**
         * build a new instance of the cpectrum builder
         *
         * @return !null instance
         */
        @Override
        public IConsensusSpectrumBuilder getConsensusSpectrumBuilder() {
            return new OriginalConsensusSpectrum();
        }
    }

    private final String id;
    protected int nSpectra = 0;
    protected boolean isDirty = false;
    protected float averagePrecursorMz = 0;
    protected float sumPrecursorMz = 0;
    protected float averagePrecursorIntens = 0;
    protected float sumPrecursorIntens = 0;
    protected int averageCharge = 0;
    protected int sumCharge = 0;
    protected ISpectrum consensusSpectrum;

    protected final String methodName = "Crowded Consensus Spectrum Builder";
    protected final String methodVersion = "0.1";

    /**
     * The m/z threshold to consider to peaks identical
     */
    protected final float FINAL_MZ_THRESHOLD = 0.4F;
    /**
     * The m/z threshold for identical peaks is not applied instantly, but gradually increased
     * to reach the final threshold. Each iteration increases the threshold by MZ_THRESHOLD_STEP.
     */
    protected final float MZ_THRESHOLD_STEP = 0.1F;

    protected List<SpectrumHolderListener> listeners = new ArrayList<SpectrumHolderListener>();

    /**
     * Rounding factor to use. 1000 means 3 positions after the comma.
     */
    public final static int MZ_PRECISSION = 1000; // using a precision of 1000 reduces memory usages but leads to different results.
    public final static boolean USE_ROUNDING = false;

    /**
     * Holds all peaks from all added spectra. In case an exact m/z is found twice, the intensities are added.
     * The array must always be sorted according to m/z.
     */
    private final List<IPeak> allPeaks = new ArrayList<IPeak>();
    /**
     * Peaks of the actual consensusSpectrum
     */
    private final List<IPeak> consensusPeaks = new ArrayList<IPeak>();

    /**
      * private to force use of the factory
      */
    private OriginalConsensusSpectrum() {
        this(null);
    }

    /**
      * private to force use of the factory
      */
    private OriginalConsensusSpectrum(String id) {
        this.id = id;
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        if (merged.length < 1)
            return;

        for (ISpectrum spectrum : merged) {
            List<IPeak> spectrumPeaks = spectrum.getPeaks();
            addPeaks(spectrumPeaks);

            sumCharge += spectrum.getPrecursorCharge();
            sumPrecursorMz += spectrum.getPrecursorMz();
            sumPrecursorIntens += 0; // TODO @jg: change to intensity when available

            nSpectra++;
        }

        setIsDirty(true);

        for (SpectrumHolderListener listener : listeners)
            listener.onSpectraAdd(this, merged);
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        if (removed.length < 1)
            return;

        for (ISpectrum spectrum : removed) {
            List<IPeak> spectrumPeaks = spectrum.getPeaks();
            removePeaks(spectrumPeaks);

            sumCharge -= spectrum.getPrecursorCharge();
            sumPrecursorMz -= spectrum.getPrecursorMz();
            sumPrecursorIntens -= 0; // TODO @jg: change to intensity when available

            nSpectra--;
        }

        setIsDirty(true);

        for (SpectrumHolderListener listener : listeners)
            listener.onSpectraRemove(this, removed);
    }

    /**
     * stable clusters do not support remove others do
     *
     * @return as above
     */
    @Override
    public boolean isRemoveSupported() {
        return true;
    }

    /**
     * Removes the passed peaks from the "crowded" spectrum
     * allPeaks.
     *
     * @param peaksToRemove
     */
    protected void removePeaks(List<IPeak> peaksToRemove) {
        //TODO @jg: build in a check to find if peaks are not sorted according to m/z

        int posAllPeaks = 0;

        for (int i = 0; i < peaksToRemove.size(); i++) {
            IPeak peakToRemove = peaksToRemove.get(i);
            float mzToRemove = peakToRemove.getMz();

            if (USE_ROUNDING)
                mzToRemove = round(mzToRemove, MZ_PRECISSION);

            for (int j = posAllPeaks; j < allPeaks.size(); j++) {
                IPeak currentExistingPeak = allPeaks.get(j);

                if (mzToRemove < currentExistingPeak.getMz()) {
                    // TODO @Rui/Steve: This means that the peak does not exist, should we throw an exception here?
                    posAllPeaks = j;
                    break;
                }

                if (mzToRemove == currentExistingPeak.getMz()) {
                    allPeaks.set(j, new Peak(
                            currentExistingPeak.getMz(),
                            currentExistingPeak.getIntensity() - peakToRemove.getIntensity(),
                            currentExistingPeak.getCount() - 1)
                    );

                    posAllPeaks = j;
                    break;
                }
            }
        }

        // clear all peaks with count < 1
        List<IPeak> tmp = new ArrayList<IPeak>();
        for (IPeak p : allPeaks) {
            if (p.getCount() > 0)
                tmp.add(p);
        }
        allPeaks.clear();
        allPeaks.addAll(tmp);
    }

    @Override
    public void addSpectrumHolderListener(SpectrumHolderListener added) {
        listeners.add(added);
    }

    @Override
    public void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        listeners.remove(removed);
    }

    /**
     * Adds the passed peaks to the "crowded" internal spectrum (allPeaks). The precursor m/z
     * values are rounded to MZ_PRECISION digist after the comma. This increases the probability that
     * two peaks have the identical precursor m/z and only have to be stored as one peak.
     *
     * @param peaksToAdd
     */
    protected void addPeaks(List<IPeak> peaksToAdd) {
        //TODO @jg: build in a check to find if peaks are not sorted according to m/z
        int posAllPeaks = 0;
        List<IPeak> newPeaks = new ArrayList<IPeak>(); // peaks with m/z values that do not yet exist

        for (int i = 0; i < peaksToAdd.size(); i++) {
            IPeak peakToAdd = peaksToAdd.get(i);
            float mzToAdd = peakToAdd.getMz();

            if (USE_ROUNDING)
                mzToAdd = round(mzToAdd, MZ_PRECISSION);

            boolean wasAdded = false;

            for (int j = posAllPeaks; j < allPeaks.size(); j++) {
                IPeak currentExistingPeak = allPeaks.get(j);

                if (mzToAdd < currentExistingPeak.getMz()) {
                    newPeaks.add(new Peak(mzToAdd, peakToAdd.getIntensity(), peakToAdd.getCount()));
                    posAllPeaks = j;
                    wasAdded = true;
                    break;
                }

                if (mzToAdd == currentExistingPeak.getMz()) {
                    allPeaks.set(j, new Peak(
                            currentExistingPeak.getMz(),
                            peakToAdd.getIntensity() + currentExistingPeak.getIntensity(),
                            currentExistingPeak.getCount() + peakToAdd.getCount())
                    );
                    posAllPeaks = j;
                    wasAdded = true;
                    break;
                }
            }

            if (!wasAdded)
                newPeaks.add(new Peak(mzToAdd, peakToAdd.getIntensity(), peakToAdd.getCount()));
        }

        // add all new peaks
        allPeaks.addAll(newPeaks);
        Collections.sort(allPeaks, PeakMzComparator.getInstance());
    }

    /**
     * Indicates whether the current data is up-to-date. As soon as spectra are
     * removed or added the spectrum is considered dirty.
     *
     * @return
     */
    protected boolean isDirty() {
        return isDirty;
    }

    protected void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    /**
     * Updates all properties of the consensus spectrum as well as the actual consensus
     * spectrum.
     */
    protected void update() {
        if (nSpectra > 0) {
            averagePrecursorMz = sumPrecursorMz / nSpectra;
            averageCharge = sumCharge / nSpectra;
            averagePrecursorIntens = sumPrecursorIntens / nSpectra;
        }
        else {
            averagePrecursorMz = 0;
            averageCharge = 0;
            averagePrecursorIntens = 0;
        }


        if (allPeaks.size() < 1) {
            List<IPeak> empty = new ArrayList<IPeak>();
            consensusSpectrum = new PeptideSpectrumMatch(id, null, averageCharge, averagePrecursorMz, empty);
            setIsDirty(false);
            return;
        }
        List<IPeak> newPeaks = findConsensusPeaks(allPeaks);

        // update the consensus spectrum
        consensusPeaks.clear();
        // allPeaks is always sorted according to precursor m/z
        consensusPeaks.addAll(newPeaks);

        // create the ConsensusSpectrum object
        consensusSpectrum = new PeptideSpectrumMatch(id, null, averageCharge, averagePrecursorMz, consensusPeaks);

        setIsDirty(false);
    }

    /**
     * refactored to make testing easier
     *
     * @param input !null set of all peaks
     * @return !null set of  consensus peaks
     */
    protected List<IPeak> findConsensusPeaks(List<IPeak> input) {


        // Step 1: merge identical peaks
        List<IPeak> ret = mergeIdenticalPeaks(input);

        // Step 2: addapt the peak intensities based on the probability that the peak has been obeserved
        ret = adaptPeakIntensities(ret);

        // Step 3: filter the spectrum
        ret = filterNoise(ret);
        return ret;
    }

    /**
     * Filters the consensus spectrum keeping only the top 5 peaks per 100 m/z
     */
    protected List<IPeak> filterNoise(List<IPeak> inp) {
        List<IPeak> filteredSpectrum = new ArrayList<IPeak>();

        int lowerBound = 0;
        // process the peaks using a sliding window of 100 m/z
        for (double startMz = 0, endMz = NOISE_FILTER_INCREMENT; endMz <= IPeak.HIGHEST_USABLE_MZ; endMz += NOISE_FILTER_INCREMENT, startMz += NOISE_FILTER_INCREMENT) {
            List<IPeak> peakBuffer = new ArrayList<IPeak>();

            // set the lower bound
            for (int i = lowerBound; i < inp.size(); i++) {
                if (inp.get(i).getMz() >= startMz) {
                    lowerBound = i;
                    break;
                }
            }

            if (inp.get(lowerBound).getMz() < startMz)
                continue;

            for (int i = lowerBound; i < inp.size(); i++) {
                if (inp.get(i).getMz() <= endMz) {
                    peakBuffer.add(inp.get(i));
                }
                else {
                    lowerBound = i;
                    break;
                }
            }

            if (peakBuffer.size() < 1)
                continue;

            Collections.sort(peakBuffer, PeakIntensityComparator.getInstance());

            List<IPeak> fivePeaks = new ArrayList<IPeak>(5);

            for (int i = 0; i < 5 && i < peakBuffer.size(); i++)
                fivePeaks.add(peakBuffer.get(i));

            Collections.sort(fivePeaks, PeakMzComparator.getInstance());
            filteredSpectrum.addAll(fivePeaks);
        }

        return filteredSpectrum;
    }

    /**
     * Adapt the peak intensities in consensusPeaks using the following formula:
     * I = I * (0.95 + 0.05 * (1 + pi))^5
     * where pi is the peaks probability
     */
    protected List<IPeak> adaptPeakIntensities(List<IPeak> inpx) {

        int originalCount = ClusterUtilities.getTotalCount(inpx);   // for debugging

        List<IPeak> ret = new ArrayList<IPeak>(inpx);
        for (int i = 0; i < ret.size(); i++) {
            IPeak peak = ret.get(i);
            float peakProbability = (float) peak.getCount() / (float) nSpectra;
            float newIntensity = (float) (peak.getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5)));

            ret.set(i, new Peak(peak.getMz(), newIntensity, peak.getCount()));
        }

        int finalCount = ClusterUtilities.getTotalCount(ret);   // for debugging
        if(originalCount != finalCount)
            throw new IllegalStateException("Peak merge changed total count");

        return ret;
    }

    /**
     * Merges identical peaks in the consensusPeaks List based on FINAL_MZ_THRESHOLD and
     * MZ_THRESHOLD_STEP.
     */
    protected List<IPeak> mergeIdenticalPeaks(List<IPeak> inPeaks) {
        int originalCount = ClusterUtilities.getTotalCount(inPeaks);   // for debugging

        List<IPeak> ret = new ArrayList<IPeak>();
        if (inPeaks.size() == 0)
            return ret; // should never happen

        for (float range = MZ_THRESHOLD_STEP; range <= FINAL_MZ_THRESHOLD; range += MZ_THRESHOLD_STEP) {
            List<IPeak> newPeakList = new ArrayList<IPeak>();
            IPeak currentPeak = inPeaks.get(0);

            for (int i = 1; i < inPeaks.size(); i++) {
                IPeak nextPeak = inPeaks.get(i);

                // check whether the next peak should be considered identical to the current one
                if (nextPeak.getMz() <= currentPeak.getMz() + range) {
                    // calculate the new weighted m/z
                    float weightedMz = (nextPeak.getIntensity() * nextPeak.getMz() + currentPeak.getIntensity() * currentPeak.getMz()) / (nextPeak.getIntensity() + currentPeak.getIntensity());

                    final float intensity = currentPeak.getIntensity() + nextPeak.getIntensity();
                    final int count = currentPeak.getCount() + nextPeak.getCount();
                    IPeak newPeak = new Peak(
                            weightedMz,
                            intensity,
                            count
                    );
                     currentPeak = newPeak;
                }
                else {
                    // by adding the peak in the else clause, peaks that were merged are not included in the new Peak
                    // list and are thereby removed from the consensusPeaks
                    newPeakList.add(currentPeak);
                    currentPeak = nextPeak;
                }
            }
            newPeakList.add(currentPeak);

            ret.clear();
            ret.addAll(newPeakList);

        }

        int finalCount = ClusterUtilities.getTotalCount(ret);   // for debugging
        if(originalCount != finalCount)
            throw new IllegalStateException("Peak merge changed total count");

        return ret;
    }

    @Override
    public ISpectrum getConsensusSpectrum() {
        if (isDirty())
            update();

        return internalGetConcensusSpectrum();
    }

    /**
     * access for test purposes
     *
     * @return possibly null specterum
     */
    protected ISpectrum internalGetConcensusSpectrum() {
        return consensusSpectrum;
    }


//    /**
//     * return a copy of the spectrum builder used to put in a new concensus spectrum
//     *
//     * @return !null Spectrum
//     */
//    @Override
//    public IConsensusSpectrumBuilder cloneSpectrumBuilder() {
//        return new OriginalConsensusSpectrum();
//    }

    @Override
    public void clear() {
        sumCharge = 0;
        sumPrecursorMz = 0;
        sumPrecursorIntens = 0;
        nSpectra = 0;

        allPeaks.clear();
        consensusPeaks.clear();

        setIsDirty(true);
    }

    @Override
    public int getSpectraCount() {
        return nSpectra;
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public String getCurrentVersion() {
        return methodVersion;
    }

    @Override
    public void onSpectraAdd(ISpectrumHolder holder, ISpectrum... added) {
        addSpectra(added);
    }

    @Override
    public void onSpectraRemove(ISpectrumHolder holder, ISpectrum... removed) {
        removeSpectra(removed);
    }

    /**
     * Round to certain number of decimals
     *
     * @param f
     * @param decimalPlace
     * @return
     */
    public static float round(float f, int decimalPlace) {
//        BigDecimal bd = new BigDecimal(Float.toString(d));
//        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
//        return bd.floatValue();
        int i = (int) ((f * decimalPlace) + 0.5);
        return (float) i / (float) decimalPlace;
    }
}
