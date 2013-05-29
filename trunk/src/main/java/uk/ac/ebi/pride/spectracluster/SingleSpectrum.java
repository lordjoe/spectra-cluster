package uk.ac.ebi.pride.spectracluster;

//import com.lordjoe.algorithms.*;
//import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
//import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
//import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
//import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * uk.ac.ebi.pride.spectracluster.SingleSpectrumCluster
// *  a cluster with only one spectrum
// * @author Steve Lewis
// * @date 5/10/13
// */
//
//public class SingleSpectrum implements IPeptideSpectrumMatch,Comparable<ISpectrum> ,Equivalent<ISpectrum> {
//
//    private final String id;
//    private final double massChargeRatio;
//    private final int charge;
//    private String peptide;
//    private final List<IPeak> peaks = new ArrayList<IPeak>();
//
//    public SingleSpectrum(String id, double massChargeRatio, int charge, IPeak[] peaks) {
//        this.id = id;
//        this.massChargeRatio = massChargeRatio;
//        this.charge = charge;
//        this.peaks.addAll(Arrays.asList(peaks)) ;
//    }
//
//    /**
//     * spectra may or may not be clusters -
//     * this guarantees that they are
//     * @return
//     */
//    public ISpectralCluster asCluster()
//    {
//        MultiSpectrumCluster cluster = new MultiSpectrumCluster(getId(), getPrecursorCharge()) ;
//        cluster.addSpectra(this);
//        return cluster;
//    }
//    /**
//     * globally unique id
//     *
//     * @return !null id
//     */
//    public String getId() {
//        return id;
//    }
//
//    /**
//       * usfful label - frequently the same as the id
//       * @return   !null title
//       */
//      public String getTitle()
//     {
//         return getId();
//     }
//
//
//    /**
//     * return scored peptide - maybe null
//     * @return
//     */
//    public String getPeptide() {
//        return peptide;
//    }
//
//    public void setPeptide(String peptide) {
//        this.peptide = peptide;
//    }
//
//    /**
//     * get MZ
//     *
//     * @return
//     */
//    public double getPrecursorMz() {
//         return massChargeRatio;
//    }
//
//    /**
//     * get charge - mixed charge
//     *
//     * @return
//     */
//    public int getPrecursorCharge() {
//        return charge;
//    }
//
//    /**
//     * return peaks sorted by MZ
//     *
//     * @return !null array of peaks
//     */
//    public IPeak[] getPeaks() {
//         return peaks.toArray(new IPeak[peaks.size()]);
//    }
//
//
//    /**
//     * may be used in sorting spectra
//     *
//     * @return
//     */
//    public double getQualityMeasure() {
//        if (true) throw new UnsupportedOperationException("Fix This");
//        return 0;
//    }
//
//
//
//    /**
//       * sort by mz - might be useful
//       * @param o
//       * @return
//       */
//     public int compareTo(ISpectrum o) {
//         if(o == this)
//             return 0;
//         if(o.getPrecursorMz() != getPrecursorMz()) {
//             return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
//         }
//        IPeak[] peaks = getPeaks();
//        IPeak[] peaks1 = o.getPeaks();
//        if(peaks.length != peaks1.length) {
//              return peaks.length < peaks1.length ? -1 : 1;
//          }
//
//         return 0;
//     }
//
//    /**
//     * like equals but weaker - says other is equivalent to this
//     *
//     * @param o poiibly null other object
//     * @return true if other is "similar enough to this"
//     */
//    public boolean equivalent(ISpectrum o) {
//        if(o == this)
//             return true;
//         if(o.getPrecursorMz() != getPrecursorMz()) {
//             return false;
//         }
//        IPeak[] peaks = getPeaks();
//        IPeak[] peaks1 = o.getPeaks();
//        if(peaks.length != peaks1.length) {
//              return false;
//          }
//
//        for (int i = 0; i < peaks1.length; i++) {
//            IPeak pk0 = peaks1[i];
//            IPeak pk1 = peaks1[i];
//            if(!pk0.equivalent(pk1))
//                return false;
//        }
//
//         return true;
//    }
//
//    /**
//     * write out the data as an MGF file
//     *
//     * @param out place to append
//     */
//    public void appendMGF(Appendable out) {
//        int indent = 0;
//
//        try {
//            out.append("BEGIN IONS");
//            out.append("\n");
//
//            out.append("TITLE=" + getId());
//            out.append("\n");
//
//            int precursorCharge = getPrecursorCharge();
//            double massChargeRatio = getPrecursorMz();
//
//            out.append("PEPMASS=" + massChargeRatio);
//            out.append("\n");
//
//            out.append("CHARGE=" + precursorCharge);
//            if (precursorCharge > 0)
//                out.append("+");
//            out.append("\n");
//
//            IPeak[] peaks = getPeaks();
//            for (int i = 0; i < peaks.length; i++) {
//                IPeak peak = peaks[i];
//                out.append(peak.toString());
//                out.append("\n");
//            }
//            out.append("END IONS");
//            out.append("\n");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }
//
//
//}
