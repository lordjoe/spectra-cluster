package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


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

    @SuppressWarnings("UnusedDeclaration")
    protected static IConsensusSpectrumBuilder getCommonConsensusSpectrumBuilder(ISpectralCluster... copied) {
        //noinspection UnnecessaryLocalVariable
        IConsensusSpectrumBuilder ret = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
//        for (int i = 1; i < copied.length; i++) {
//            final IConsensusSpectrumBuilder spectrumBuilder = copied[i].cloneConsensusSpectrumBuilder();
//            if (!ret.equals(spectrumBuilder)) {
//                final boolean equals = ret.equals(spectrumBuilder); // why not
//                throw new IllegalStateException("AlternativeSpectralClusters MUST have the same ConsensusSpectrumBuilder");
//            }
//        }
        return ret;
    }

    private String id;
    private ISpectrum consensusSpectrum;
    // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolder qualityHolder;
    private final List<ISpectralCluster> constitutingClusters = new ArrayList<ISpectralCluster>();
    @SuppressWarnings("FieldCanBeLocal")
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


    @SuppressWarnings("UnusedDeclaration")
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
     * return a set of all ids
     *
     * @return
     */
    @Override
    public Set<String> getSpectralIds() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    @Override
    public String getSpectralId() {
        StringBuilder sb = new StringBuilder();
        List<String> spectralIds = new ArrayList<String>(getSpectralIds());
        Collections.sort(spectralIds);
        sb.append(spectralIds.get(0));
        for (int i = 1; i < spectralIds.size(); i++) {
            sb.append(",");
            sb.append(spectralIds.get(i));
        }
        return sb.toString();
    }


    /**
     * all internally spectrum
     */
    @Override
    public List<ISpectrum> getHighestQualitySpectra() {

        return getClusteredSpectra();   // todo fix
    }

    //    /**
//     * needed so copy constructors work with the interface
//     *
//     * @return
//     */
//    @Override
//    public IConsensusSpectrumBuilder cloneConsensusSpectrumBuilder() {
//        return consensusSpectrumBuilder.cloneSpectrumBuilder();
//    }
//
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
    public List<String> getPeptides() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public String getMostCommonPeptide() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    /**
     * get peptides with statistics
     *
     * @param dd
     * @return list ordered bu purity
     */
    @Nonnull
    @Override
    public List<ClusterPeptideFraction> getPeptidePurity(final IDecoyDiscriminator dd) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
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
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isStable() {
        return true;
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    @Override
    public boolean isSemiStable() {
        return true;
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @param stable as
     */
    public void setStable(boolean stable) {
        if (!isStable())
            throw new UnsupportedOperationException("AlternativeSpectralClusters are always stable");
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

        //noinspection ConstantIfStatement
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
        } else {
            if (spc1.size() != spc2.size())
                return false;

            //noinspection ForLoopReplaceableByForEach
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
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void append(Appendable out) {

        try {
            out.append("BEGIN CLUSTER");
            out.append(" Id=" + getId());
            //noinspection StringConcatenationInsideStringBufferAppend
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
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * make a one line report
     *
     * @param out
     */
    @Override
    public void appendData(Appendable out) {
        try {
            out.append(getId());
            out.append("\t");

            String mz = String.format("%f8.2", getPrecursorMz());
            out.append(mz);
            out.append("\t");

            out.append(Integer.toString(getPrecursorCharge()));
            out.append("\t");


        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }


    }

    /**
     * write out the data as a .clustering file
     *
     * @param out place to append
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void appendClustering(Appendable out) {
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration
        int indent = 0;

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");


            List<ISpectrum> clusteredSpectra1 = getClusteredSpectra();
            String s = ClusterUtilities.mostCommonPeptides(clusteredSpectra1);
            out.append("sequence=[" + s + "]");
            out.append("\n");

            for (ISpectrum spec : clusteredSpectra1) {
                out.append("SPEC\t");
                out.append(spec.getId());
                out.append("\ttrue\n");

            }
        } catch (IOException e) {
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
    @SuppressWarnings("UnusedDeclaration , StringConcatenationInsideStringBufferAppend")
    public void appendMGF(Appendable out) {

        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            double precursorCharge = getPrecursorCharge();
            double massChargeRatio = getPrecursorMz();

            //noinspection StringConcatenationInsideStringBufferAppend
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
