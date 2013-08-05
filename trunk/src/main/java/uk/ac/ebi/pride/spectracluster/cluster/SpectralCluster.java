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
public class SpectralCluster implements ISpectralCluster, ISpectrumHolder, InternalSpectralCluster, Equivalent<ISpectralCluster> {


    private final String id;
    private boolean dirty;
    private ISpectrum consensusSpectrum;
    // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolder qualityHolder;
    private final List<SpectrumHolderListener> m_SpectrumHolderListeners = new CopyOnWriteArrayList<SpectrumHolderListener>();


    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(ISpectralCluster copied) {
        this.id = copied.getId();
        this.consensusSpectrum = new PeptideSpectrumMatch(copied.getConsensusSpectrum());
        this.dirty = false;
        this.consensusSpectrumBuilder = copied.getConsensusSpectrumBuilder();
        this.qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
        final List<ISpectrum> clusteredSpectra1 = copied.getClusteredSpectra();
        addSpectra(clusteredSpectra1);

    }


    public SpectralCluster(String id) {
        this(id, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;
        this.consensusSpectrum = null;
        this.dirty = false;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
        this.qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
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


    @Override
    public String getId() {
        return id;
    }

    @Override
    public float getPrecursorMz() {
        ISpectrum consensusSpectrum1 = getConsensusSpectrum();
        if (consensusSpectrum1 == null)
            return 0;
        return consensusSpectrum1.getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        return getConsensusSpectrum().getPrecursorCharge();
    }

    @Override
    public List<IPeak> getPeaks() {
        return getConsensusSpectrum().getPeaks();
    }

    @Override
    public int getPeaksCount() {
        return getConsensusSpectrum().getPeaksCount();
    }


    @Override
    public IConsensusSpectrumBuilder getConsensusSpectrumBuilder() {
        return consensusSpectrumBuilder;
    }

    /**
     * all internally spectrum
     */
    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        return qualityHolder.getHighestQualitySpectra();
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
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Override
    public boolean containsMajorPeak(final int mz) {
        return consensusSpectrum.containsMajorPeak(mz);
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Override
    public int[] asMajorPeakMZs() {
        final ISpectrum consensusSpectrum1 = getConsensusSpectrum();
        return consensusSpectrum1.asMajorPeakMZs();
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guaranett clean can be byPassed
     *
     * @return
     */
    @Override
    public List<ISpectrum> internalGetClusteredSpectra() {
        return clusteredSpectra;
    }

    /**
     * convenience method to all a list not individual spectra
     * @param merged !null list to add
     */
    @Override
    public void addSpectra( List<ISpectrum> merged) {
         addSpectra(merged.toArray(new ISpectrum[merged.size()]));
    }


    @Override
    public void addSpectra(ISpectrum... merged) {
        if (merged != null && merged.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToMerge : merged) {
                if (!clusteredSpectra.contains(spectrumToMerge)) {
                    clusteredSpectra.add(spectrumToMerge);
                }
            }
            notifySpectrumHolderListeners(true, merged);   // tell other interested parties  true says this is an add
        }
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        if (removed != null && removed.length > 0) {
            dirty = false;

            for (ISpectrum spectrumToRemove : removed) {
                dirty |= clusteredSpectra.remove(spectrumToRemove);  // set dirty if we actually remove anything
            }
            notifySpectrumHolderListeners(false, removed); // tell other interested parties  false says this is a remove
        }
    }


    protected void guaranteeClean() {
        if (dirty) {
            dirty = false;
            consensusSpectrum = consensusSpectrumBuilder.getConsensusSpectrum();
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
        if (getPrecursorMz() != o.getPrecursorMz()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (getPrecursorCharge() != o.getPrecursorCharge()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
        }

        ISpectrum highestQualitySpectrum1 = getHighestQualitySpectrum();
        ISpectrum highestQualitySpectrum2 = o.getHighestQualitySpectrum();
        if (highestQualitySpectrum1 != highestQualitySpectrum2) {
            if (highestQualitySpectrum1 == null || highestQualitySpectrum2 == null) {
                highestQualitySpectrum1 = getHighestQualitySpectrum();
                throw new IllegalStateException("problem"); // ToDo change
            }

            return highestQualitySpectrum1.getQualityScore() < highestQualitySpectrum2.getQualityScore() ? -1 : 1;
        }

        int hash1 = hashCode();
        int hash2 = o.hashCode();
        if(hash1 != hash2)
            return hash1 < hash2 ? -1 : 0;

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

        List<ISpectrum> spc1 = internalGetClusteredSpectra();
        List<ISpectrum> spc2;
        if (o instanceof InternalSpectralCluster)
            spc2 = ((InternalSpectralCluster) o).internalGetClusteredSpectra();  // no copy or clean needed
        else
            spc2 = o.getClusteredSpectra();

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
     * write out the data as a CGF file
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
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(getClusteredSpectra()) + "]");
            out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(getConsensusSpectrum()));
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
        double precursorMZ = getPrecursorMz();
        String text =
                "charge= " + getPrecursorCharge() + "," +
                        "mz= " + String.format("%10.3f", precursorMZ).trim() + "," +
                        "count= " + clusteredSpectra.size() +
                        ", spectrum = ";
        for (ISpectrum s : clusteredSpectra)
            text += s.getId() + ",";

        text = text.substring(0, text.length() - 1);
        return text;
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


}
