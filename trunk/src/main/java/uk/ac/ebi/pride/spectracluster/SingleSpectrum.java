package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.SingleSpectrumCluster
 *  a cluster with only one spectrum
 * @author Steve Lewis
 * @date 5/10/13
 */

public class SingleSpectrum implements IPeptideSpectrumMatch,Comparable<ISpectrum> ,Equivalent<ISpectrum> {
    public static SingleSpectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SingleSpectrum.class;

    private final String m_Id;
    private final double m_MassChargeRatio;
    private final int m_Charge;
    private String m_Peptide;
    private final List<ISpecClusterPeak> m_Peaks  = new ArrayList<ISpecClusterPeak>();

    public SingleSpectrum(String id, double massChargeRatio, int charge, ISpecClusterPeak[] peaks) {
        m_Id = id;
        m_MassChargeRatio = massChargeRatio;
        m_Charge = charge;
        m_Peaks.addAll(Arrays.asList(peaks)) ;
    }

    /**
     * spectra may or may not be clusters -
     * this guarantees that they are
     * @return
     */
    @Override
    public ISpectralCluster asCluster()
    {
        MultiSpectrumCluster cluster = new MultiSpectrumCluster(getId(),getCharge()) ;
        cluster.mergeClusters(this);
        return cluster;
    }
    /**
     * globally unique id
     *
     * @return !null id
     */
    @Override
    public String getId() {
        return m_Id;
    }

    /**
       * usfful label - frequently the same as the id
       * @return   !null title
       */
     @Override
      public String getTitle()
     {
         return getId();
     }


    /**
     * return scored peptide - maybe null
     * @return
     */
    @Override
    public String getPeptide() {
        return m_Peptide;
    }

    public void setPeptide(String peptide) {
        m_Peptide = peptide;
    }

    /**
     * get MZ
     *
     * @return
     */
    @Override
    public double getMassChargeRatio() {
         return m_MassChargeRatio;
    }

    /**
     * get charge - mixed charge
     *
     * @return
     */
    @Override
    public int getCharge() {
        return m_Charge;
    }

    /**
     * return peaks sorted by MZ
     *
     * @return !null array of peaks
     */
    @Override
    public ISpecClusterPeak[] getPeaks() {
         return m_Peaks.toArray(ISpecClusterPeak.EMPTY_ARRAY);
    }


    /**
     * may be used in sorting spectra
     *
     * @return
     */
    @Override
    public double getQualityMeasure() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }



    /**
       * sort by mz - might be useful
       * @param o
       * @return
       */
    @Override
     public int compareTo(ISpectrum o) {
         if(o == this)
             return 0;
         if(o.getMassChargeRatio() != getMassChargeRatio()) {
             return getMassChargeRatio() < o.getMassChargeRatio() ? -1 : 1;
         }
        ISpecClusterPeak[] peaks = getPeaks();
        ISpecClusterPeak[] peaks1 = o.getPeaks();
        if(peaks.length != peaks1.length) {
              return peaks.length < peaks1.length ? -1 : 1;
          }

         return 0;
     }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param other poiibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(ISpectrum o) {
        if(o == this)
             return true;
         if(o.getMassChargeRatio() != getMassChargeRatio()) {
             return false;
         }
        ISpecClusterPeak[] peaks = getPeaks();
        ISpecClusterPeak[] peaks1 = o.getPeaks();
        if(peaks.length != peaks1.length) {
              return false;
          }

        for (int i = 0; i < peaks1.length; i++) {
            ISpecClusterPeak pk0 = peaks1[i];
            ISpecClusterPeak pk1 = peaks1[i];
            if(!pk0.equivalent(pk1))
                return false;
        }

         return true;
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    @Override
    public void appendMGF(Appendable out) {
        int indent = 0;

        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            int precursorCharge = getCharge();
            double massChargeRatio = getMassChargeRatio();

            out.append("PEPMASS=" + massChargeRatio);
            out.append("\n");

            out.append("CHARGE=" + precursorCharge);
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            ISpecClusterPeak[] peaks = getPeaks();
            for (int i = 0; i < peaks.length; i++) {
                ISpecClusterPeak peak = peaks[i];
                out.append(peak.toString());
                out.append("\n");
            }
            out.append("END IONS");
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
