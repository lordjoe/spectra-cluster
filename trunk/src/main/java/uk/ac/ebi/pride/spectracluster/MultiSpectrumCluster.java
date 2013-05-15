package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.MultiSpectrumCluster
 * a cluster with only one spectrum
 * ??? not really sure we need this but we might to kick things off
 *
 * @author Steve Lewis
 * @date 5/10/13
 */
public class MultiSpectrumCluster implements ISpectralCluster, Comparable<ISpectralCluster>, Equivalent<ISpectralCluster> {
    public static MultiSpectrumCluster[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MultiSpectrumCluster.class;

    private final String m_Id;
    private double m_MassChargeRatio;
    private final int m_Charge;
    private final Map<String, ISpectrum> m_Clusters = new HashMap<String, ISpectrum>();
    private final List<ISpecClusterPeak> m_Peaks = new ArrayList<ISpecClusterPeak>();
    private boolean m_Dirty;

    public MultiSpectrumCluster(String id, int charge) {
        m_Id = id;
        m_Charge = charge;
    }

    public MultiSpectrumCluster(String id, double massChargeRatio, int charge) {
        this(id, charge);
        m_MassChargeRatio = massChargeRatio;
    }

    /**
     * spectra may or may not be clusters -
     * this guarantees that they are
     *
     * @return
     */
    @Override
    public ISpectralCluster asCluster() {
        return this;
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
     *
     * @return !null title
     */
    @Override
    public String getTitle() {
        return getId();
    }


    /**
     * get MZ
     *
     * @return
     */
    @Override
    public double getMassChargeRatio() {
        guaranteeClean();  // build concensus as needed
        return m_MassChargeRatio;
    }

    protected void setMassChargeRatio(double massChargeRatio) {
        m_MassChargeRatio = massChargeRatio;
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
        guaranteeClean();  // build concensus as needed
        return m_Peaks.toArray(ISpecClusterPeak.EMPTY_ARRAY);
    }

    /**
     * all internally clusters
     *
     * @return
     */
    @Override
    public ISpectrum[] getClusteredSpectra() {
        return m_Clusters.values().toArray(ISpectrum.EMPTY_ARRAY);
    }

    /**
     * count of internal clusters
     *
     * @return
     */
    @Override
    public int getClusteredSpectraCount() {
        return m_Clusters.size();
    }

    /**
     * may be used in sorting spectra
     *
     * @return
     */
    @Override
    public double getQualityMeasure() {
        guaranteeClean();  // build concensus as needed
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }


    /**
     * @param merged
     */
    @Override
    public void mergeClusters(ISpectrum... merged) {
        setDirty(true);
        for (int i = 0; i < merged.length; i++) {
            ISpectrum sc = merged[i];
            m_Clusters.put(sc.getId(), sc);
        }

    }

    /**
     * take
     *
     * @param removed
     */
    @Override
    public void removeClusters(ISpectrum... removed) {
        setDirty(true);
        for (int i = 0; i < removed.length; i++) {
            ISpectrum sc = removed[i];
            m_Clusters.remove(sc.getId());
        }

    }

    protected boolean isDirty() {
        return m_Dirty;
    }

    protected void setDirty(boolean dirty) {
        m_Dirty = dirty;
    }

    protected void guaranteeClean() {
        if (isDirty()) {
            setDirty(false);
            // only one cluster - use it
            if (getClusteredSpectraCount() == 1) {
                ISpectrum[] scs = m_Clusters.values().toArray(ISpectrum.EMPTY_ARRAY);
                ISpectrum sc0 = scs[0];
                m_MassChargeRatio = sc0.getMassChargeRatio();
                m_Peaks.clear();
                m_Peaks.addAll(Arrays.asList(sc0.getPeaks()));

            } else {
                // not sure what to do here
                throw new UnsupportedOperationException("Fix This"); // ToDo build concensus
            }
        }
    }

    /**
     * sort by mz - might be useful
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ISpectralCluster o) {
        if (o == this)
            return 0;
        if (o.getMassChargeRatio() != getMassChargeRatio()) {
            return getMassChargeRatio() < o.getMassChargeRatio() ? -1 : 1;
        }
        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
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
    public boolean equivalent(ISpectralCluster o) {
        if (o == this)
            return true;
        if (getCharge() != o.getCharge())
            return false;
        if (getClusteredSpectraCount() == 1) {     //  todo Rui - make the algorithm
            double del = o.getMassChargeRatio() - getMassChargeRatio();
            double abs = Math.abs(del);
            if (abs > ISpecClusterPeak.SMALL_MZ_DIFFERENCE) {
                return false;
            }

        }
        ISpectrum[] spc1 = getClusteredSpectra();
        ISpectrum[] spc2 = o.getClusteredSpectra();
        if (spc1.length != spc2.length) {
            return false;
        }
        if (spc1.length <= 1) {

            ISpecClusterPeak[] peaks = getPeaks();
            ISpecClusterPeak[] peaks1 = o.getPeaks();
            if (peaks.length != peaks1.length) {
                return false;
            }
            for (int i = 0; i < peaks1.length; i++) {
                ISpecClusterPeak pk0 = peaks[i];
                ISpecClusterPeak pk1 = peaks1[i];
                if (!pk0.equivalent(pk1))
                    return false;
            }
            return true; // just one spectrum so check peaks
        } else {


            for (int i = 0; i < spc1.length; i++) {
                Equivalent<ISpectrum> pk0 = (Equivalent<ISpectrum>) spc1[i];
                ISpectrum pk1 = spc2[i];
                boolean test = !pk0.equivalent(pk1);
                if (test)
                    return false;
            }
            return true;
        }
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    @Override
    public void append(Appendable out) {
        int indent = 0;

        try {
            out.append("BEGIN CLUSTER");
            out.append(" Id=" + getId());
            out.append(" Charge=" + getCharge());

            out.append("\n");

            appendSpectra(out);
//            ISpecClusterPeak[] peaks = getPeaks();
//            for (int i = 0; i < peaks.length; i++) {
//                ISpecClusterPeak peak = peaks[i];
//                out.append(peak.toString());
//                out.append("\n");
//            }
            out.append("END CLUSTER");
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * do not add begin and end cluster - useful for rebuilding a mgf
     * @param out
     */
    public void appendSpectra(Appendable out) {
        ISpectrum[] clusteredSpectra = getClusteredSpectra();
        for (int i = 0; i < clusteredSpectra.length; i++) {
            ISpectrum cs = clusteredSpectra[i];
            cs.appendMGF(out);  // single spectgra become mgfs

        }
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
