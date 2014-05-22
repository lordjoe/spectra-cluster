package uk.ac.ebi.pride.spectracluster.spectrum;


import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.MZIntensityUtilities;
import uk.ac.ebi.pride.spectracluster.util.comparator.PeakIntensityComparator;
import uk.ac.ebi.pride.spectracluster.util.comparator.PeakMzComparator;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.PeaksSpectrum
 * @author Steve Lewis
 * @author Rui Wang
 * Date: 6/20/13
 */
public class Spectrum implements ISpectrum {

    private static final int BAD_QUALITY_MEASURE = -1;

    private final String id;
    private final int precursorCharge;
    private final float precursorMz;
    private final List<IPeak> peaks = new ArrayList<IPeak>();

    private double totalIntensity;
    private double sumSquareIntensity;

    private final IQualityScorer qualityScorer;
    private double qualityMeasure = BAD_QUALITY_MEASURE;

    // Dot products always get the highest peaks of a specific intensity -
    // this caches those and returns a list sorted by MZ
    private final Map<Integer, ISpectrum> highestPeaks = new HashMap<Integer, ISpectrum>();
    private final Set<Integer> majorPeakMZ = new HashSet<Integer>();
    // the number of peaks considered as "major" when the majorPeakMZ Set was filled the last time.
    private int currentMajorPeakCount = 0;


    public Spectrum(final String pId,
                    final int pPrecursorCharge,
                    final float pPrecursorMz,
                    final List<IPeak> inpeaks) {
        this(pId, pPrecursorCharge, pPrecursorMz, Defaults.INSTANCE.getDefaultQualityScorer(), inpeaks);
    }


    public Spectrum(final String pId,
                    final int pPrecursorCharge,
                    final float pPrecursorMz,
                    final IQualityScorer qualityScorer,
                    final List<IPeak> inpeaks) {
        this.id = pId;
        this.precursorCharge = pPrecursorCharge;
        this.precursorMz = pPrecursorMz;
        this.qualityScorer = qualityScorer;

        this.peaks.clear();
        Collections.sort(inpeaks);
        this.peaks.addAll(inpeaks);
        Collections.sort(this.peaks, PeakMzComparator.INSTANCE);

        calculateIntensities();
    }

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public Spectrum(final ISpectrum spectrum) {
        this(spectrum, spectrum.getPeaks());
    }

    /**
     * copy with different peaks
     *
     * @param spectrum base used for charge, mz
     * @param inpeaks    new peaks
     */
    public Spectrum(final ISpectrum spectrum,
                    final List<IPeak> inpeaks) {

        this.id = spectrum.getId();
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.precursorMz = spectrum.getPrecursorMz();
        this.qualityScorer = spectrum.getQualityScorer();

        peaks.clear();
        peaks.addAll(inpeaks);
        Collections.sort(this.peaks, PeakMzComparator.INSTANCE);

        calculateIntensities();

    }

    private void calculateIntensities() {
        double totalIntensityX = 0;
        double sumSquareIntensityX = 0;
        for (IPeak peak : peaks) {
            double intensity = peak.getIntensity();
            totalIntensityX += intensity;
            double ji = convertIntensity(peak);
            sumSquareIntensityX += ji * ji;
        }
        totalIntensity = totalIntensityX;
        sumSquareIntensity = sumSquareIntensityX;
    }

    /**
     * who knows why Johannes does this but we can as well
     * todo @rw: double check this wit Johannes
     */
    double convertIntensity(IPeak p1) {
        double intensity = p1.getIntensity();
        if (intensity == 0)
            return 0;
        return 1 + Math.log(intensity);
    }

    public String getId() {
        return id;
    }

    public float getPrecursorMz() {
        return precursorMz;
    }

    public int getPrecursorCharge() {
        return precursorCharge;
    }

    public double getTotalIntensity() {
        return totalIntensity;
    }

    /**
     * return the sum  Square of all intensities
     */
    public double getSumSquareIntensity() {
        return sumSquareIntensity;
    }

    /**
     * return an unmodifiable version of the internal list
     *
     * @return as above
     */
    @Override
    public List<IPeak> getPeaks() {
        return Collections.unmodifiableList(peaks);
    }

    /**
     * return internal array - use internally when safe
     *
     * @return
     */
    protected List<IPeak> internalGetPeaks() {
        return peaks;
    }

    /**
     * return number of peaks
     *
     * @return count
     */
    public int getPeaksCount() {
        return peaks.size();
    }

    /**
     * does the concensus spectrum contain this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Override
    public boolean containsMajorPeak(final int mz, int majorPeakCount) {
        guaranteeMajorPeaks(majorPeakCount);
        return majorPeakMZ.contains(mz);
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank et all suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Override
    public int[] asMajorPeakMZs(int majorPeakCount) {
        guaranteeMajorPeaks(majorPeakCount);
        final Integer[] peaks = majorPeakMZ.toArray(new Integer[majorPeakMZ.size()]);
        Arrays.sort(peaks);
        int[] ret = new int[peaks.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = peaks[i];

        }
        return ret;
    }

    private void guaranteeMajorPeaks(int majorPeakCount) {
        if (majorPeakMZ.isEmpty() || majorPeakCount != currentMajorPeakCount) {
            majorPeakMZ.clear();
            ISpectrum peaks = getHighestNPeaks(currentMajorPeakCount);
            for (IPeak peak : peaks.getPeaks()) {
                majorPeakMZ.add((int) peak.getMz());
            }
            currentMajorPeakCount = majorPeakCount;
        }
    }

    public double getQualityScore() {
        if (qualityMeasure == BAD_QUALITY_MEASURE) {
            qualityMeasure = qualityScorer.calculateQualityScore(this);
        }

        return qualityMeasure;
    }

    @Override
    public IQualityScorer getQualityScorer() {
        return qualityScorer;
    }

    /**
     * get the highest intensity peaks sorted by MZ - this value may be cached
     *
     * @param numberRequested number peaks requested
     * @return list of no more than  numberRequested peaks in Mz order
     */
    @Override
    public ISpectrum getHighestNPeaks(int numberRequested) {
        //  guaranteeClean();
        ISpectrum ret = highestPeaks.get(numberRequested);
        if (ret == null) {
            ret = buildHighestPeaks(numberRequested);
            int numberPeaks = ret.getPeaksCount();
            // remember the result and if less than requested remember for all
            // requests above or equal to the size
            for (int i = numberRequested; i >= numberPeaks; i--) {
                highestPeaks.put(i, ret);  // todo fix
            }
        }
        return ret;
    }

    /**
     * return a list of the highest peaks sorted by intensity
     *
     * @param numberRequested number peaks requested
     * @return !null array of size <= numberRequested;
     */
    protected ISpectrum buildHighestPeaks(int numberRequested) {
        List<IPeak> byIntensity = new ArrayList<IPeak>(getPeaks());
        Collections.sort(byIntensity, PeakIntensityComparator.INSTANCE); // sort by intensity
        List<IPeak> holder = new ArrayList<IPeak>();
        for (IPeak iPeak : byIntensity) {
            holder.add(iPeak);
            if (holder.size() >= numberRequested)
                break;
        }
        //noinspection UnnecessaryLocalVariable
        PeptideSpectrumMatch ret = new PeptideSpectrumMatch(this, holder);
        return ret;
    }

    @Override
    public String toString() {
        return getId();
    }


    /**
     * natural sort order is first charge then mz
     * finally compare id
     *
     * @param o !null other spectrum
     * @return as above
     */
    @Override
    public int compareTo(ISpectrum o) {
        if (this == o)
            return 0;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return getPrecursorCharge() < o.getPrecursorCharge() ? -1 : 1;
        if (getPrecursorMz() != o.getPrecursorMz())
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;

        return getId().compareTo(o.getId());


    }


    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o possibly null other object
     * @return true if other is "similar enough to this"
     */
    public boolean equivalent(ISpectrum o) {
        if (o == this)
            return true;

        if (Math.abs(o.getPrecursorMz() - getPrecursorMz()) > MZIntensityUtilities.SMALL_MZ_DIFFERENCE) {
            return false;
        }

        final List<IPeak> iPeaks = internalGetPeaks();
        IPeak[] peaks = iPeaks.toArray(new IPeak[iPeaks.size()]);
        IPeak[] peaks1;
        if (o instanceof Spectrum) {
            final List<IPeak> iPeaks1 = ((Spectrum) o).internalGetPeaks();
            peaks1 = iPeaks1.toArray(new IPeak[iPeaks1.size()]);

        } else {
            final List<IPeak> peaks2 = o.getPeaks();
            peaks1 = peaks2.toArray(new IPeak[peaks2.size()]);

        }

        if (peaks.length != peaks1.length) {
            return false;
        }

        for (int i = 0; i < peaks1.length; i++) {
            IPeak pk0 = peaks[i];
            IPeak pk1 = peaks1[i];
            if (!pk0.equivalent(pk1))
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Spectrum that = (Spectrum) o;

        if (precursorCharge != that.precursorCharge) return false;
        if (Float.compare(that.precursorMz, precursorMz) != 0) return false;
        if (!id.equals(that.id)) return false;
        if (peaks.size() != that.peaks.size()) {
            return false;
        }

        for (int i = 0; i < peaks.size(); i++) {
            IPeak pk0 = peaks.get(i);
            IPeak pk1 = that.peaks.get(i);
            if (!pk0.equals(pk1))
                return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + precursorCharge;
        result = 31 * result + (precursorMz != +0.0f ? Float.floatToIntBits(precursorMz) : 0);
        for (IPeak pk0 : peaks) {
            result = 31 * result + pk0.hashCode();
        }

        return result;
    }
}
