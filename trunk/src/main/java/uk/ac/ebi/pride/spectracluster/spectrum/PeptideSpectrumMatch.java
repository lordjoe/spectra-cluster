package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 * <p/>
 * todo: implement quality measure
 * todo: implement equals and hashcode
 *
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch implements IPeptideSpectrumMatch {
    private String id;
    private String peptide;
    private double precursorCharge;
    private double precursorMz;
    private double precursorIntensity;
    /**
     * for calculate similarity between spectra, all peaks should be sorted by intensity
     */
    private final List<IPeak> peaks = new ArrayList<IPeak>();
    private double qualityMeasure;

    public PeptideSpectrumMatch(String id,
                                String peptide,
                                double precursorCharge,
                                double precursorMz,
                                double precursorIntensity,
                                Collection<IPeak> peaks) {
        this.id = id;
        this.peptide = peptide;
        this.precursorCharge = precursorCharge;
        this.precursorMz = precursorMz;
        this.precursorIntensity = precursorIntensity;
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

    public double getPrecursorIntensity() {
        return precursorIntensity;
    }

    public double getPrecursorCharge() {
        return precursorCharge;
    }

    public List<IPeak> getPeaks() {
        return new ArrayList<IPeak>(peaks);
    }

    public void setPeaks(Collection<IPeak> peaks) {
        this.peaks.clear();
        if (peaks != null) {
            this.peaks.addAll(peaks);
            Collections.sort(this.peaks, PeakIntensityComparator.getInstance());
        }
    }

    public double getQualityMeasure() {
        return qualityMeasure;
    }

    public void setQualityMeasure(double qualityMeasure) {
        this.qualityMeasure = qualityMeasure;
    }

    /**
     * make a cluster contaiming a single spectrum - this
     * @return
     */
    public ISpectralCluster asCluster() {
        SpectralCluster ret = new SpectralCluster(getId());
        ret.addSpectra(this);
        return ret;
    }

    @Override
     /**
      * write out the data as an MGF file
      *
      * @param out place to append
      */
     public void appendMGF(Appendable out) {
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

               for ( IPeak peak : getPeaks()) {
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
            if(o == this)
                 return true;
             if(o.getPrecursorMz() != getPrecursorMz()) {
                 return false;
             }
            IPeak[] peaks = getPeaks().toArray(new IPeak[0]);
            IPeak[] peaks1 = o.getPeaks().toArray(new IPeak[0]);
            if(peaks.length != peaks1.length) {
                  return false;
              }

            for (int i = 0; i < peaks1.length; i++) {
                IPeak pk0 = peaks1[i];
                IPeak pk1 = peaks1[i];
                if(!pk0.equivalent(pk1))
                    return false;
            }

             return true;
        }


}
