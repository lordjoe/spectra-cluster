package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.math.*;
import java.util.*;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>  This class is effectively immutable - some measures are computed lazily but
 * it cannot be manipulated from the outside
  *
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch extends PeaksSpectrum implements IPeptideSpectrumMatch, ISpectrumQuality {


    private final String peptide;
    // Dot products always get the highest peaks of a specific intensity -
    // this caches thoes and returns a list sorted by MZ
    private final Map<Integer, IPeaksSpectrum> highestPeaks = new HashMap<Integer, IPeaksSpectrum>();
    private double qualityMeasure = BAD_QUALITY_MEASURE;
    private BigInteger majorBits;

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public PeptideSpectrumMatch(ISpectrum spectrum) {
        super(spectrum, spectrum.getPeaks());
        if (spectrum instanceof IPeptideSpectrumMatch) {
            peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
        }
        else {
            peptide = null;
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
        }
        else {
            peptide = null;
        }
        makeAdvancedCalculations();
    }

    public PeptideSpectrumMatch(String id,
                                String peptideX,
                                int precursorCharge,
                                float precursorMz,
                                List<IPeak> peaks) {
        super(id, precursorCharge, precursorMz, peaks);
        this.peptide = peptideX;
        makeAdvancedCalculations();

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
        final IPeaksSpectrum highestNPeaks = getHighestNPeaks(MAJOR_PEAK_NUMBER);
        final List<IPeak> iPeaks = ((PeaksSpectrum) highestNPeaks).internalGetPeaks();
        for (IPeak pk : iPeaks) {
            ret = ret.setBit((int) pk.getMz());
        }
        return ret;
    }

    protected void makeAdvancedCalculations() {
        highestPeaks.clear(); // highest peaks may have changed
        qualityMeasure = BAD_QUALITY_MEASURE;  // be laze here
    }


    public String getPeptide() {
        return peptide;
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
        if (qualityMeasure == BAD_QUALITY_MEASURE) {
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
        SpectralCluster ret = new SpectralCluster(getId());
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
    public IPeaksSpectrum getHighestNPeaks(int numberRequested) {
        //  guaranteeClean();
        IPeaksSpectrum ret = highestPeaks.get(numberRequested);
        if (ret == null) {
            ret = buildHighestPeaks(numberRequested);
            int numberPeaks = ret.getPeaksCount();
            // remember the result and if less than requested remember for all
            // requests above or equal to the size
            for (int i = numberRequested; i >= numberPeaks; i--) {
                highestPeaks.put(i, ret);
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
        String csq = "TITLE=" + getId();
        final String peptide1 = getPeptide();
        if (peptide1 != null && peptide1.length() > 0)
            csq = "TITLE==id=" + getId() + ",sequence=" + peptide1;
        out.append(csq);
    }


}
