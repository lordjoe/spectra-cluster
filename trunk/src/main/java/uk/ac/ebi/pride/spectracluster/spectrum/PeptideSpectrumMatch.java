package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>  This class is effectively immutable - some measures are computed lazily but
 * it cannot be manipulated from the outside
 * todo: implement quality measure
 *
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch implements IPeptideSpectrumMatch {

    /**
     * who knows why Johannes does this but we can as well
     * todo generalize
     *
     * @param p1
     * @return
     */
    public static double johannesIntensityConverted(IPeak p1) {
        double intensity = p1.getIntensity();
        if (intensity == 0)
            return 0;
        double intensity2 = 1 + Math.log(intensity);
        return intensity2;

    }


    private final String id;
    private final String peptide;
    private final double precursorCharge;
    private final double precursorMz;
    private double totalIntensity;
    private double sumSquareIntensity;
    /**
     * for calculate similarity between spectra, all peaks should be sorted by intensity
     */
    private final List<IPeak> peaks = new ArrayList<IPeak>();
    // Dot products always get the highest peaks of a specific intensity -
    // this caches thoes and returns a list sorted by MZ
    private final Map<Integer, ISpectrum> highestPeaks = new HashMap<Integer, ISpectrum>();
    private double qualityMeasure = BAD_QUALITY_MEASURE;
    private boolean m_Dirty;

    /**
     * simple copy constructor
     * @param spectrum
     */
    public PeptideSpectrumMatch(ISpectrum spectrum) {
        this(spectrum, spectrum.getPeaks());
    }

    /**
     * copy with different peaks
     * @param spectrum  base used for charge, mz
     * @param peaks new peaks
     */
    public PeptideSpectrumMatch(ISpectrum spectrum, List<IPeak> inpeaks) {
        this.id = spectrum.getId();
        if (spectrum instanceof IPeptideSpectrumMatch) {
            this.peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
        } else {
            this.peptide = null;
        }
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.precursorMz = spectrum.getPrecursorMz();

        peaks.clear();
        Collections.sort(inpeaks);
        peaks.addAll(inpeaks);
        makeCalculations();
    }

    public PeptideSpectrumMatch(String id,
                                String peptide,
                                double precursorCharge,
                                double precursorMz,
                                List<IPeak> peaks) {
        this.id = id;
        this.peptide = peptide;
        this.precursorCharge = precursorCharge;
        this.precursorMz = precursorMz;
        this.qualityMeasure = BAD_QUALITY_MEASURE;
        this.peaks.clear();
        Collections.sort(peaks);
        this.peaks.addAll(peaks);
        makeCalculations();

    }

    public String getId() {
        return id;
    }

    public String getPeptide() {
        return peptide;
    }

    public double getPrecursorMz() {
        return precursorMz;
    }

    public double getPrecursorCharge() {
        return precursorCharge;
    }

    @Override
    public double getTotalIntensity() {
    //    guaranteeClean();
        return totalIntensity;
    }

    /**
     * return the sum  Square of all intensities
     */
    @Override
    public double getSumSquareIntensity() {
      //  guaranteeClean();
        return sumSquareIntensity;
    }


    protected void makeCalculations() {
        highestPeaks.clear(); // highest peaks may have changed
        Collections.sort(this.peaks, PeakMzComparator.getInstance());
        totalIntensity = 0;
        sumSquareIntensity = 0;
        for (IPeak peak : peaks) {
            double intensity = peak.getIntensity();
            totalIntensity += intensity;
            // johannes uses this in his dotProduct // todo generalize for other algorithms
            double ji = johannesIntensityConverted(peak);
            sumSquareIntensity += ji * ji;
        }
        qualityMeasure = BAD_QUALITY_MEASURE;  // be laze here
    }


    protected double buildQualityMeasure() {
        return Defaults.INSTANCE.getDefaultQualityScorer().calculateQualityScore(this);

    }


    public List<IPeak> getPeaks() {
       // guaranteeClean();
        return new ArrayList<IPeak>(peaks);
    }

    /**
     * return number of peaks
     *
     * @return count
     */
    @Override
    public int getPeaksCount() {
     //   guaranteeClean();
        return peaks.size();
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
    public ISpectrum getHighestNPeaks(int numberRequested) {
      //  guaranteeClean();
        ISpectrum ret = highestPeaks.get(numberRequested);
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
        List<IPeak> byIntensity = new ArrayList<IPeak>(peaks);
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

    @Override
    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendMGF(Appendable out) {
    //    guaranteeClean();
        int indent = 0;

        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            double precursorCharge = getPrecursorCharge();
            double massChargeRatio = getPrecursorMz();

            out.append("PEPMASS=" + massChargeRatio);
            out.append("\n");

            out.append("CHARGE=" + precursorCharge);
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            for (IPeak peak : getPeaks()) {
                out.append(peak.toString());
                out.append("\n");
            }
            out.append("END IONS");
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o poiibly null other object
     * @return true if other is "similar enough to this"
     */
    public boolean equivalent(ISpectrum o) {
        if (o == this)
            return true;
     //   guaranteeClean();
        if (o.getPrecursorMz() != getPrecursorMz()) {
            return false;
        }
        IPeak[] peaks = getPeaks().toArray(new IPeak[0]);
        IPeak[] peaks1 = o.getPeaks().toArray(new IPeak[0]);
        if (peaks.length != peaks1.length) {
            return false;
        }

        for (int i = 0; i < peaks1.length; i++) {
            IPeak pk0 = peaks1[i];
            IPeak pk1 = peaks1[i];
            if (!pk0.equivalent(pk1))
                return false;
        }

        return true;
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
    //    guaranteeClean();
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return getPrecursorCharge() < o.getPrecursorCharge() ? -1 : 1;
        if (getPrecursorMz() != o.getPrecursorMz())
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;

        return getId().compareTo(o.getId());


    }
}
