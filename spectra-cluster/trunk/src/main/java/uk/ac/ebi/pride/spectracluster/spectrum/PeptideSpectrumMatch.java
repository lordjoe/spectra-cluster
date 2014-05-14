package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Constants;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.IOException;
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
     * @param peaks    new peaks
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
     * an optimization to return a Biginteger representing bits at the mz values where the
     * majors (top MAJOR_PEAK_NUMBER are
     *
     * @return !null value - lazily built
     */
    @Override
    public BigInteger asMajorPeakBits() {
        if (majorBits == null) {
            majorBits = buildMajorBits();
        }
        return majorBits;
    }

    /**
     * set bits numbered by the highest MAJOR_PEAK_NUMBER(6) peaks
     *
     * @return constructed BigInteger
     */
    protected BigInteger buildMajorBits() {
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
    @Override
    public ISpectrum asMajorPeaks() {
        return getHighestNPeaks(MAJOR_PEAK_NUMBER);
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

    protected void guaranteeMajorPeaks() {
        if (majorPeakMZ.isEmpty()) {
            ISpectrum peaks = asMajorPeaks();
            for (IPeak peak : peaks.getPeaks()) {
                majorPeakMZ.add((int) peak.getMz());
            }
        }
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
    @Override
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
    public ISpectralCluster asCluster() {
        //   guaranteeClean();
        // id will be spectrum id
        SpectralCluster ret = new SpectralCluster();
        ret.addSpectra(this);
        return ret;
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
     * override to add peptide
     *
     * @param out
     * @throws java.io.IOException
     */
    @Override
    protected void appendTitle(final Appendable out) throws IOException {
        String csq = "TITLE=id=" + getId();
        final String peptide = getPeptide();
        if (peptide != null && peptide.length() > 0)
            csq = "TITLE=id=" + getId() + ",sequence=" + peptide;
        out.append(csq);
    }


    @Override
    protected void appendMSFStart(final Appendable out) {
        String peptide = getPeptide();
        try {
            out.append(ParserUtilities.NAME_START + " " + peptide + "/" + getPrecursorCharge());
            out.append("\n");
            String id = getId();
            if (id != null) {
                out.append(ParserUtilities.LIBID_START + " " + getId());
                out.append("\n");

            }
            //        out.append(ParserUtilities.MW_START + String.format("%10.3f", getPrecursorMz() * getPrecursorCharge()).trim());
            //        out.append("\n");
            out.append(ParserUtilities.PRECURSORMZ_START + " " + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");

            String prop = getProperty("Status");
            if (prop != null) {
                out.append(ParserUtilities.STATUS_START + prop);
                out.append("\n");
            }
            prop = getProperty("molecularWeight");
            if (prop != null) {
                out.append(ParserUtilities.MW_START + prop);
                out.append("\n");
            }
            prop = getProperty("FullName");
            if (prop != null) {
                out.append(ParserUtilities.FULL_NAME_START + prop);
                out.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * superclass appends all peaks but this version only uses the top 100
     *
     * @param out
     */
    @Override
    protected void appendPeaks(final Appendable out) {
        try {
            ISpectrum highestNPeaks = this;
            if (Constants.USE_HIGHEST_PEAKS)
                highestNPeaks = getHighestNPeaks(Constants.MAX_PEAKS_TO_KEEP);
            for (IPeak peak : highestNPeaks.getPeaks()) {
                String line = String.format("%10.3f", peak.getMz()).trim() + "\t" +
                        String.format("%10.3f", peak.getIntensity()).trim();
                out.append(line);
                out.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

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

    @Override
    protected void appendMSFComment(final Appendable pOut) {
        List<String> strings = new ArrayList<String>(properties.keySet());
        Collections.sort(strings);
        boolean first = true;
        try {
            pOut.append(ParserUtilities.COMMENT_START + " ");
            for (String string : strings) {
                if (!first) {
                    pOut.append(" ");
                    first = false;
                } else {
                    first = false;
                }
                pOut.append(string);
                pOut.append("=");
                String value = properties.get(string);
                if (value.contains(" ")) {
                    value = "\"" + value + "\"";
                }
                pOut.append(value);
            }
            pOut.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * write basic data as TSV
     *
     * @param out
     */
    @Override
    public void appendTSV(final Appendable out) {
        try {
            out.append(getId());
            out.append("\t");
            out.append(Integer.toString(getPrecursorCharge()));
            out.append("\t");
            String mzString = String.format("%10.2f", getPrecursorMz()).trim();
            out.append(mzString);
            out.append("\t");
            out.append(getPeptide());
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

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
