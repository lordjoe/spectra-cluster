package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.io.*;
import java.util.*;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>
 * todo: implement quality measure
*
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch implements IPeptideSpectrumMatch {

    private String id;
    private String peptide;
    private double precursorCharge;
    private double precursorMz;
    private double totalIntensity;
    /**
     * for calculate similarity between spectra, all peaks should be sorted by intensity
     */
    private final List<IPeak> peaks = new ArrayList<IPeak>();
    // Dot products always get the highest peaks of a specific intensity -
    // this caches thoes and returns a list sorted by MZ
    private final Map<Integer,List<IPeak>> highestPeaks = new HashMap<Integer, List<IPeak>>();
     private double qualityMeasure;
    private boolean m_Dirty;

    public PeptideSpectrumMatch(ISpectrum spectrum) {
        this.id = spectrum.getId();
        if (spectrum instanceof IPeptideSpectrumMatch) {
            this.peptide = ((IPeptideSpectrumMatch) spectrum).getPeptide();
        } else {
            this.peptide = null;
        }
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.precursorMz = spectrum.getPrecursorMz();

        for (IPeak peak : spectrum.getPeaks()) {
            this.peaks.add(new Peak(peak.getMz(), peak.getIntensity(), peak.getCount()));
        }
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
        this.qualityMeasure = -1;
        setPeaks(peaks);
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

    public double getTotalIntensity() {
        return totalIntensity;
    }

    protected boolean isDirty() {
        return m_Dirty;
    }

    protected void setDirty(boolean dirty) {
        m_Dirty = dirty;
    }

    protected void guaranteeClean() {
        if (isDirty()) {
            highestPeaks.clear(); // highest peaks may have changed
            Collections.sort(this.peaks, PeakMzComparator.getINSTANCE());
            totalIntensity = 0;
            for (IPeak peak : peaks) {
                totalIntensity += peak.getIntensity();
            }
            qualityMeasure = buildQualityMeasure();
            setDirty(false);
          }
    }

    protected  double buildQualityMeasure() {
       // throw new UnsupportedOperationException("Fix This"); // ToDo
        return 0;
    }


    public List<IPeak> getPeaks() {
        guaranteeClean();
        return new ArrayList<IPeak>(peaks);
    }

    public void setPeaks(Collection<IPeak> peaks) {
        this.peaks.clear();
        if (peaks != null) {
            this.peaks.addAll(peaks);
        }
        setDirty(true);
    }

    public double getQualityMeasure() {
        guaranteeClean();
        return qualityMeasure;
    }

    public void setQualityMeasure(double qualityMeasure) {
        this.qualityMeasure = qualityMeasure;
    }

    /**
     * make a cluster contaiming a single spectrum - this
     *
     * @return
     */
    public ISpectralCluster asCluster() {
        guaranteeClean();
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
    public List<IPeak> getHighestNPeaks(int numberRequested) {
        guaranteeClean();
        List<IPeak> ret = highestPeaks.get(numberRequested);
        if(ret == null)    {
            ret = buildHighestPeaks(numberRequested) ;
            Collections.sort(ret,PeakMzComparator.INSTANCE);   // sort by mz
            // remember the result and if less than requested remember for all
            // requests above or equal to the size
            for (int i = numberRequested; i >= ret.size(); i--) {
                highestPeaks.put(i,ret);
              }
        }
        return ret;
    }

    /**
     * return a list of the highest peaks sorted by intensity
     * @param numberRequested   number peaks requested
     * @return  !null array of size <= numberRequested;
     */
    protected List<IPeak> buildHighestPeaks(int numberRequested) {
        List<IPeak> byIntensity = new ArrayList<IPeak>(peaks);
        Collections.sort(byIntensity,PeakIntensityComparator.INSTANCE); // sort by intensity
        List<IPeak> holder = new ArrayList<IPeak>();
        for (IPeak iPeak : byIntensity) {
            holder.add(iPeak);
            if(holder.size() >= numberRequested)
                return holder;
        }
        return holder; // we did not get enough
    }

    @Override
    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendMGF(Appendable out) {
        guaranteeClean();
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
        guaranteeClean();
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
        guaranteeClean();
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return getPrecursorCharge() < o.getPrecursorCharge() ? -1 : 1;
        if (getPrecursorMz() != o.getPrecursorMz())
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;

        return getId().compareTo(o.getId());


    }
}
