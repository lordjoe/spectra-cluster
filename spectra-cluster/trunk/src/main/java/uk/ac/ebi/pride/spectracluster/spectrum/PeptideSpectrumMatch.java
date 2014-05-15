package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Constants;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.PeakIntensityComparator;

import java.util.*;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>  This class is effectively immutable - some measures are computed lazily but
 * it cannot be manipulated from the outside
 *
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch extends PeaksSpectrum implements IPeptideSpectrumMatch {


    private final String peptide;
    private final String annotation;
    // Dot products always get the highest peaks of a specific intensity -
    // this caches those and returns a list sorted by MZ
    private final Map<Integer, ISpectrum> highestPeaks = new HashMap<Integer, ISpectrum>();
    private double qualityMeasure = Constants.BAD_QUALITY_MEASURE;
    private Set<Integer> majorPeakMZ = new HashSet<Integer>();

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public PeptideSpectrumMatch(ISpectrum spectrum) {
        super(spectrum, spectrum.getPeaks());
        if (spectrum instanceof IPeptideSpectrumMatch) {
            peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
            annotation = ((IPeptideSpectrumMatch) spectrum).getAnnotation();
        } else {
            peptide = null;
            annotation = null;
        }
    }

    /**
     * copy with different peaks
     *
     * @param spectrum base used for charge, mz
     * @param inpeaks    new peaks
     */
    public PeptideSpectrumMatch(ISpectrum spectrum, List<IPeak> inpeaks) {
        super(spectrum, inpeaks);
        if (spectrum instanceof IPeptideSpectrumMatch) {
            peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
            annotation = ((IPeptideSpectrumMatch) spectrum).getAnnotation();
        } else {
            peptide = null;
            annotation = null;
        }
    }


    public PeptideSpectrumMatch(String id,
                                String peptide,
                                int precursorCharge,
                                float precursorMz,
                                List<IPeak> peaks) {
        this(id, peptide, precursorCharge, precursorMz, peaks, null);

    }

    public PeptideSpectrumMatch(String id,
                                String peptide,
                                int precursorCharge,
                                float precursorMz,
                                List<IPeak> peaks,
                                String pAnnotation) {
        super(id, precursorCharge, precursorMz, peaks);
        this.peptide = peptide;
        this.annotation = pAnnotation;
    }

    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Override
    public boolean containsMajorPeak(final int mz) {
        guaranteeMajorPeaks();
        return majorPeakMZ.contains(mz);
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Override
    public int[] asMajorPeakMZs() {
        guaranteeMajorPeaks();
        final Integer[] peaks = majorPeakMZ.toArray(new Integer[majorPeakMZ.size()]);
        Arrays.sort(peaks);
        int[] ret = new int[peaks.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = peaks[i];

        }
        return ret;
    }

    private void guaranteeMajorPeaks() {
        if (majorPeakMZ.isEmpty()) {
            ISpectrum peaks = getHighestNPeaks(MAJOR_PEAK_NUMBER);
            for (IPeak peak : peaks.getPeaks()) {
                majorPeakMZ.add((int) peak.getMz());
            }
        }
    }


    /**
     * return scored peptide - maybe null
     */
    @Override
    public String getPeptide() {
        return peptide;
    }

    /**
     * return text in the id not peptide or id
     */
    @Override
    public String getAnnotation() {
        return annotation;
    }

    /**
     * return a spectrum normalized to the specific total intensity
     *
     * @return !null spectrum - might be this
     */
    @Deprecated
    public INormalizedSpectrum asNormalizedTo(final double totalIntensity) {
        return new NormailzedPeptideSpectrumMatch(this, totalIntensity);   // build a new normalized intensity
    }


    protected double buildQualityMeasure() {
        return Defaults.INSTANCE.getDefaultQualityScorer().calculateQualityScore(this);

    }

    public double getQualityScore() {
        //   guaranteeClean();
        if (qualityMeasure == Constants.BAD_QUALITY_MEASURE) {
            qualityMeasure = buildQualityMeasure();
        }
        return qualityMeasure;
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

    /**
     * true if we know this is a decoy
     *
     * @return
     */
    @Override
    public boolean isDecoy() {
        PSMSpectrum psm = PSMSpectrum.getSpectrum(getId());
        return psm.isDecoy();
    }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o possibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(final ISpectrum o) {
        if (!super.equivalent(o))
            return false;
        if (o instanceof PeptideSpectrumMatch) {
            PeptideSpectrumMatch realO = (PeptideSpectrumMatch) o;

            String peptide1 = getPeptide();
            String peptide2 = realO.getPeptide();
            if (!ClusterUtilities.equalObject(peptide1, peptide2))
                return false;
        }

        return true;
    }
}
