package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Constants;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.math.BigInteger;
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
    private final Map<String, String> properties = new HashMap<String, String>();
    // Dot products always get the highest peaks of a specific intensity -
    // this caches thoes and returns a list sorted by MZ
    private final Map<Integer, ISpectrum> highestPeaks = new HashMap<Integer, ISpectrum>();
    private double qualityMeasure = Constants.BAD_QUALITY_MEASURE;
    private BigInteger majorBits;
    private Set<Integer> majorPeakMZ = new HashSet<Integer>();
    private Double selfDotProduct;

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
        makeAdvancedCalculations();
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
                                String pAnnotation
    ) {
        super(id, precursorCharge, precursorMz, peaks);
        this.peptide = peptide;
        this.annotation = pAnnotation;

        makeAdvancedCalculations();

    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }


    public Map<String, String> getProperties() {
        return new HashMap<String, String>(properties);
    }

    /**
     * set bits numbered by the highest MAJOR_PEAK_NUMBER(6) peaks
     *
     * @return constructed BigInteger
     */
    @Deprecated
    private BigInteger buildMajorBits() {
        BigInteger ret = BigInteger.ZERO;
        final ISpectrum highestNPeaks = asMajorPeaks();
        final List<IPeak> iPeaks = ((PeaksSpectrum) highestNPeaks).internalGetPeaks();
        for (IPeak pk : iPeaks) {
            ret = ret.setBit((int) pk.getMz());
        }
        return ret;
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Deprecated
    private ISpectrum asMajorPeaks() {
        return getHighestNPeaks(MAJOR_PEAK_NUMBER);
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Deprecated
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

    protected void makeAdvancedCalculations() {
        highestPeaks.clear(); // highest peaks may have changed
        qualityMeasure = Constants.BAD_QUALITY_MEASURE;  // be laze here
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
     * make a cluster contaiming a single spectrum - this
     *
     * @return
     */
//    public ISpectralCluster asCluster() {
//        //   guaranteeClean();
//        // id will be spectrum id
//        SpectralCluster ret = new SpectralCluster();
//        ret.addSpectra(this);
//        return ret;
//    }

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


            for (String key : properties.keySet()) {
                String me = getProperty(key);
                String them = realO.getProperty(key);
                if (!me.equals(them))
                    return false;
            }

        }

        return true;
    }
}
