package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class AlternativeSpectralClusters implements ISpectralCluster, InternalSpectralCluster, Equivalent<ISpectralCluster> {

    protected static String concensusId(ISpectralCluster... copied) {
        StringBuilder sb = new StringBuilder();
        for (ISpectralCluster sc : copied) {
            if (sb.length() > 0)
                sb.append(":");
            sb.append(sc.getId());
        }

        return sb.toString();
    }

    protected static ISpectrum getHighestQualitySpectrum(ISpectralCluster... copied) {
        ISpectrum ret = copied[0].getHighestQualitySpectrum();
        for (int i = 1; i < copied.length; i++) {
            if (!ret.equals(copied[i].getHighestQualitySpectrum()))
                throw new IllegalStateException("AlternativeSpectralClusters MUST have the same highest quality spectrum");
        }

        return ret;
    }

    protected static IConsensusSpectrumBuilder getCommonConsensusSpectrumBuilder(ISpectralCluster... copied) {
        IConsensusSpectrumBuilder ret = copied[0].cloneConsensusSpectrumBuilder();
        for (int i = 1; i < copied.length; i++) {
            final IConsensusSpectrumBuilder spectrumBuilder = copied[i].cloneConsensusSpectrumBuilder();
            if (!ret.equals(spectrumBuilder)) {
                final boolean equals = ret.equals(spectrumBuilder); // why not
                throw new IllegalStateException("AlternativeSpectralClusters MUST have the same ConsensusSpectrumBuilder");
            }
        }
        return ret;
    }

    private final String id;
    private ISpectrum consensusSpectrum;
    // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolder qualityHolder;
    private final List<ISpectralCluster> constitutingClusters = new ArrayList<ISpectralCluster>();
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;
    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private final List<SpectrumHolderListener> m_SpectrumHolderListeners = new CopyOnWriteArrayList<SpectrumHolderListener>();
    private boolean locked;

    public AlternativeSpectralClusters(ISpectralCluster... copied) {
        this.id = concensusId(copied);
        constitutingClusters.addAll(Arrays.asList(copied));
        qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
        this.consensusSpectrumBuilder = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
        Set<ISpectrum> holder = new HashSet<ISpectrum>();
        for (ISpectralCluster sc : copied) {
            holder.addAll(sc.getClusteredSpectra());
        }
        addSpectra(new ArrayList<ISpectrum>(holder));
        Collections.sort(clusteredSpectra);
        this.consensusSpectrum = consensusSpectrumBuilder.getConsensusSpectrum();
        locked = true; // now we are immutable
    }


    /**
     * add a change listener
     * final to make sure this is not duplicated at multiple levels
     *
     * @param added non-null change listener
     */
    @Override
    public final void addSpectrumHolderListener(SpectrumHolderListener added) {
        if (!m_SpectrumHolderListeners.contains(added))
            m_SpectrumHolderListeners.add(added);
    }

    /**
     * remove a change listener
     *
     * @param removed non-null change listener
     */
    @Override
    public final void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        while (m_SpectrumHolderListeners.contains(removed))
            m_SpectrumHolderListeners.remove(removed);
    }


    /**
     * notify any state change listeners - probably should
     * be protected but is in the interface to form an event cluster
     *
     * @param oldState
     * @param newState
     * @param commanded
     */
    protected void notifySpectrumHolderListeners(boolean isAdd, ISpectrum... spectra) {
        if (m_SpectrumHolderListeners.isEmpty())
            return;
        for (SpectrumHolderListener listener : m_SpectrumHolderListeners) {
            if (isAdd)
                listener.onSpectraAdd(this, spectra);
            else
                listener.onSpectraRemove(this, spectra);
        }
    }


    public List<ISpectralCluster> getConstitutingClusters() {
        return Collections.unmodifiableList(constitutingClusters);
    }

    @Override
    public String getId() {
        return id;
    }

    protected void guaranteeClean() {
        // do nothing but keep code more compatable with SpectraCluster
    }


    /**
     * all internally spectrum
     */
    @Override
    public List<ISpectrum> getHighestQualitySpectra() {

        return getClusteredSpectra();   // todo fix
    }

    /**
     * needed so copy constructors work with the interface
     *
     * @return
     */
    @Override
    public IConsensusSpectrumBuilder cloneConsensusSpectrumBuilder() {
        return consensusSpectrumBuilder.cloneSpectrumBuilder();
    }

    @Override
    public float getPrecursorMz() {
        guaranteeClean();
        ISpectrum consensusSpectrum1 = getConsensusSpectrum();
        if (consensusSpectrum1 == null)
            return 0;
        return consensusSpectrum1.getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        guaranteeClean();
        return getConsensusSpectrum().getPrecursorCharge();
    }

    @Override
    public List<IPeak> getPeaks() {
        guaranteeClean();
        return getConsensusSpectrum().getPeaks();
    }


    @Override
    public int getPeaksCount() {
        guaranteeClean();
        return getConsensusSpectrum().getPeaksCount();
    }


    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    @Override
    public ISpectrum getHighestQualitySpectrum() {
        return qualityHolder.getHighestQualitySpectrum();
    }


    @Override
    public ISpectrum getConsensusSpectrum() {
        guaranteeClean();
        return consensusSpectrum;
    }

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guarantee clean can be byPassed
     *
     * @return exactly the current concensus spectrum
     */
    @Override
    public ISpectrum internalGetConcensusSpectrum() {
        return consensusSpectrum;
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Override
    public int[] asMajorPeakMZs() {
        return getConsensusSpectrum().asMajorPeakMZs();
    }


    @Override
    public List<ISpectrum> getClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    /**
     * @return
     */
    @Override
    public List<ISpectrum> internalGetClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }


    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }


    /**
     * convenience method to all a list not individual spectra
     *
     * @param merged !null list to add
     */
    @Override
    public void addSpectra(List<ISpectrum> merged) {
        addSpectra(merged.toArray(new ISpectrum[merged.size()]));
    }


    @Override
    public void addSpectra(ISpectrum... merged) {
        if (locked)
            throw new IllegalStateException("Cannot change AlternativeSpectralClusters");
        if (merged != null && merged.length > 0) {

            for (ISpectrum spectrumToMerge : merged) {
                if (!clusteredSpectra.contains(spectrumToMerge)) {
                    clusteredSpectra.add(spectrumToMerge);
                }
            }
            notifySpectrumHolderListeners(true, merged);   // tell other interested parties  true says this is an add
        }
    }


    /**
     * stable clusters do not support remove others do
     *
     * @return as above
     */
    @Override
    public boolean isRemoveSupported() {
        return false;
    }



    @Override
    public void removeSpectra(ISpectrum... removed) {
        throw new UnsupportedOperationException("Cannot change AlternativeSpectralClusters");
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
        if (getPrecursorMz() != o.getPrecursorMz()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
        }

        if (getHighestQualitySpectrum() != o.getHighestQualitySpectrum()) {
            return getHighestQualitySpectrum().getQualityScore() < o.getHighestQualitySpectrum().getQualityScore() ? -1 : 1;
        }

        if (true)
            throw new UnsupportedOperationException("Fix This"); // This should never happen
        return 0;
    }


    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o poiibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(ISpectralCluster o) {
        if (o == this)
            return true;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return false;
        double del = o.getPrecursorMz() - getPrecursorMz();
        double abs = Math.abs(del);
        if (abs > IPeak.SMALL_MZ_DIFFERENCE) {
            return false;
        }
        List<ISpectrum> spc1 = getClusteredSpectra();
        List<ISpectrum> spc2 = o.getClusteredSpectra();
        if (spc1.size() != spc2.size()) {
            return false;
        }
        if (spc1.size() <= 1) {

            List<IPeak> peaks = getPeaks();
            List<IPeak> peaks1 = o.getPeaks();
            if (peaks.size() != peaks1.size()) {
                return false;
            }
            for (int i = 0; i < peaks1.size(); i++) {
                IPeak pk0 = peaks.get(i);
                IPeak pk1 = peaks1.get(i);
                if (!pk0.equivalent(pk1))
                    return false;
            }
            return true; // just one spectrum so check peaks
        }
        else {
            if (spc1.size() != spc2.size())
                return false;


            for (int i = 0; i < spc1.size(); i++) {
                ISpectrum pk1 = spc1.get(i);
                ISpectrum pk2 = spc2.get(i);
                boolean test = !pk2.equivalent(pk1);
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
            out.append(" Charge=" + getPrecursorCharge());

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
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * write out the data as a .clustering file
     *
     * @param out place to append
     */
    @Override
    public void appendClustering(Appendable out) {
        int indent = 0;

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");


            out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(getClusteredSpectra()) + "]");
            out.append("\n");

            for (ISpectrum spec : getClusteredSpectra()) {
                out.append("SPEC\t");
                out.append(spec.getId());
                out.append("\ttrue\n");

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * do not add begin and end cluster - useful for rebuilding a mgf
     *
     * @param out
     */
    @Override
    public void appendSpectra(Appendable out) {
        List<ISpectrum> clusteredSpectra = getClusteredSpectra();
        for (ISpectrum cs : clusteredSpectra) {
            cs.appendMGF(out);  // single spectgra become mgfs

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ISpectralCluster constitutingCluster : constitutingClusters) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(constitutingCluster);
        }
        return sb.toString();

    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendMGF(Appendable out) {

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
        }
        catch (IOException e) {
            throw new RuntimeException(e);

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
        for (ISpectralCluster constitutingCluster : constitutingClusters) {
            if (constitutingCluster.containsMajorPeak(mz))
                return true;
        }
        return false;
    }
}
