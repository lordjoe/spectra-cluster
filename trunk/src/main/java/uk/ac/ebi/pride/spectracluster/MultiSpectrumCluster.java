package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.*;
import java.util.*;
//
///**
// * uk.ac.ebi.pride.spectracluster.MultiSpectrumCluster
// * a cluster with only one spectrum
// * ??? not really sure we need this but we might to kick things off
// *
// * @author Steve Lewis
// * @date 5/10/13
// */
//public class MultiSpectrumCluster implements ISpectralCluster, Comparable<ISpectralCluster>, Equivalent<ISpectralCluster> {
//
//    private final String id;
//    private double massChargeRatio;
//    private final int charge;
//    private final Map<String, ISpectrum> clusters = new HashMap<String, ISpectrum>();
//    private final List<IPeak> peaks = new ArrayList<IPeak>();
//    private boolean dirty;
//
//    public MultiSpectrumCluster(String id, int charge) {
//        this.id = id;
//        this.charge = charge;
//    }
//
//    public MultiSpectrumCluster(String id, double massChargeRatio, int charge) {
//        this(id, charge);
//        this.massChargeRatio = massChargeRatio;
//    }
//
//    /**
//     * spectra may or may not be clusters -
//     * this guarantees that they are
//     *
//     * @return
//     */
//    public ISpectralCluster asCluster() {
//        return this;
//    }
//
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
//     * usfful label - frequently the same as the id
//     *
//     * @return !null title
//     */
//    public String getTitle() {
//        return getId();
//    }
//
//
//    /**
//     * get MZ
//     *
//     * @return
//     */
//    public double getMassChargeRatio() {
//        guaranteeClean();  // build concensus as needed
//        return massChargeRatio;
//    }
//
//    protected void setMassChargeRatio(double massChargeRatio) {
//        this.massChargeRatio = massChargeRatio;
//    }
//
//    /**
//     * get charge - mixed charge
//     *
//     * @return
//     */
//    public int getCharge() {
//        return charge;
//    }
//
//    /**
//     * return peaks sorted by MZ
//     *
//     * @return !null array of peaks
//     */
//    public IPeak[] getPeaks() {
//        guaranteeClean();  // build concensus as needed
//        return peaks.toArray(new IPeak[peaks.size()]);
//    }
//
//    /**
//     * all internally clusters
//     *
//     * @return
//     */
//    public ISpectrum[] getClusteredSpectra() {
//        Collection<ISpectrum> spectra = clusters.values();
//        return spectra.toArray(new ISpectrum[spectra.size()]);
//    }
//
//    /**
//     * count of internal clusters
//     *
//     * @return
//     */
//    public int getClusteredSpectraCount() {
//        return clusters.size();
//    }
//
//    /**
//     * may be used in sorting spectra
//     *
//     * @return
//     */
//    public double getQualityMeasure() {
//        guaranteeClean();  // build concensus as needed
//        if (true) throw new UnsupportedOperationException("Fix This");
//        return 0;
//    }
//
//
//    /**
//     * @param merged
//     */
//    public void addSpectra(ISpectrum... merged) {
//        setDirty(true);
//        for (ISpectrum sc : merged) {
//            clusters.put(sc.getId(), sc);
//        }
//
//    }
//
//    /**
//     * take
//     *
//     * @param removed
//     */
//    public void removeSpectra(ISpectrum... removed) {
//        setDirty(true);
//        for (ISpectrum sc : removed) {
//            clusters.remove(sc.getId());
//        }
//
//    }
//
//    protected boolean isDirty() {
//        return dirty;
//    }
//
//    protected void setDirty(boolean dirty) {
//        this.dirty = dirty;
//    }
//
//    protected void guaranteeClean() {
//        if (isDirty()) {
//            setDirty(false);
//            // only one cluster - use it
//            if (getClusteredSpectraCount() == 1) {
//                ISpectrum[] scs = getClusteredSpectra();
//                ISpectrum sc0 = scs[0];
//                massChargeRatio = sc0.getPrecursorMz();
//                peaks.clear();
//                peaks.addAll(Arrays.asList(sc0.getPeaks()));
//
//            } else {
//                // not sure what to do here
//                throw new UnsupportedOperationException("Fix This"); // ToDo build concensus
//            }
//        }
//    }
//
//    /**
//     * sort by mz - might be useful
//     *
//     * @param o
//     * @return
//     */
//    public int compareTo(ISpectralCluster o) {
//        if (o == this)
//            return 0;
//        if (o.getMassChargeRatio() != getMassChargeRatio()) {
//            return getMassChargeRatio() < o.getMassChargeRatio() ? -1 : 1;
//        }
//        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
//            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
//        }
//
//        return 0;
//    }
//
//
//    /**
//     * like equals but weaker - says other is equivalent to this
//     *
//     * @param o poiibly null other object
//     * @return true if other is "similar enough to this"
//     */
//    public boolean equivalent(ISpectralCluster o) {
//        if (o == this)
//            return true;
//        if (getCharge() != o.getCharge())
//            return false;
//        if (getClusteredSpectraCount() == 1) {     //  todo Rui - make the algorithm
//            double del = o.getMassChargeRatio() - getMassChargeRatio();
//            double abs = Math.abs(del);
//            if (abs > IPeak.SMALL_MZ_DIFFERENCE) {
//                return false;
//            }
//
//        }
//        ISpectrum[] spc1 = getClusteredSpectra();
//        ISpectrum[] spc2 = o.getClusteredSpectra();
//        if (spc1.length != spc2.length) {
//            return false;
//        }
//        if (spc1.length <= 1) {
//
//            IPeak[] peaks = getPeaks();
//            IPeak[] peaks1 = o.getPeaks();
//            if (peaks.length != peaks1.length) {
//                return false;
//            }
//            for (int i = 0; i < peaks1.length; i++) {
//                IPeak pk0 = peaks[i];
//                IPeak pk1 = peaks1[i];
//                if (!pk0.equivalent(pk1))
//                    return false;
//            }
//            return true; // just one spectrum so check peaks
//        } else {
//
//
//            for (int i = 0; i < spc1.length; i++) {
//                Equivalent<ISpectrum> pk0 = (Equivalent<ISpectrum>) spc1[i];
//                ISpectrum pk1 = spc2[i];
//                boolean test = !pk0.equivalent(pk1);
//                if (test)
//                    return false;
//            }
//            return true;
//        }
//    }
//
//    /**
//     * write out the data as an MGF file
//     *
//     * @param out place to append
//     */
//    public void append(Appendable out) {
//        int indent = 0;
//
//        try {
//            out.append("BEGIN CLUSTER");
//            out.append(" Id=" + getId());
//            out.append(" Charge=" + getCharge());
//
//            out.append("\n");
//
//            appendSpectra(out);
////            ISpecClusterPeak[] peaks = getPeaks();
////            for (int i = 0; i < peaks.length; i++) {
////                ISpecClusterPeak peak = peaks[i];
////                out.append(peak.toString());
////                out.append("\n");
////            }
//            out.append("END CLUSTER");
//            out.append("\n");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }
//
//    /**
//     * do not add begin and end cluster - useful for rebuilding a mgf
//     *
//     * @param out
//     */
//    public void appendSpectra(Appendable out) {
//        ISpectrum[] clusteredSpectra = getClusteredSpectra();
//        for (int i = 0; i < clusteredSpectra.length; i++) {
//            ISpectrum cs = clusteredSpectra[i];
//            cs.appendMGF(out);  // single spectgra become mgfs
//
//        }
//    }
//
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
//            int precursorCharge = getCharge();
//            double massChargeRatio = getMassChargeRatio();
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
