package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.algorithms.CountedString;
import com.lordjoe.algorithms.Equivalent;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.SpectrumIDComparator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Rui Wang
 * @version $Id$
 *          * NOTE extend WatchedClass to look for possible memory leaks
 */
public class SpectralCluster /* extends WatchedClass */ implements ISpectralCluster, ISpectrumHolder, InternalSpectralCluster, Equivalent<ISpectralCluster> {


    private String id;
    // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolder qualityHolder;
    private final List<SpectrumHolderListener> m_SpectrumHolderListeners = new CopyOnWriteArrayList<SpectrumHolderListener>();
    private final Set<String> spectraIds = new HashSet<String>();
    private final List<ClusterPeptideFraction> byPurity = new ArrayList<ClusterPeptideFraction>();


    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(ISpectralCluster copied) {
        this.id = copied.getId();
        this.consensusSpectrumBuilder = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
        addSpectrumHolderListener(this.consensusSpectrumBuilder);

        this.qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
        final List<ISpectrum> clusteredSpectra1 = copied.getClusteredSpectra();
        addSpectra(clusteredSpectra1);

    }

    /**
     * use this when the cluster is not stable
     */
    public SpectralCluster() {
        this(null, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id) {
        this(id, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        super();
        this.id = id;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
        addSpectrumHolderListener(this.consensusSpectrumBuilder);
        this.qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
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
     * return a set of all ids
     *
     * @return
     */
    @Override
    public Set<String> getSpectralIds() {
        if (this.spectraIds.isEmpty()) {
            List<ISpectrum> clusteredSpectra1 = getClusteredSpectra();
            for (ISpectrum iSpectrum : clusteredSpectra1) {
                spectraIds.add(iSpectrum.getId());
            }
        }
        return Collections.unmodifiableSet(spectraIds);
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

    /**
     * if possible use the highest
     *
     * @return
     */
    @Override
    public String getId() {
        // in unstable clusters use id of the highest quality spectrum
        if (id == null) {
            id = getSpectralId();
            //   ISpectrum highestQualitySpectrum = getHighestQualitySpectrum();
            //    if (highestQualitySpectrum != null)
            //       return highestQualitySpectrum.getId();
        }
        return id;
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

    public void setId(String id) {
        if (this.id != null) {
            throw new IllegalStateException("Cluster id should only be set once");
        }

        this.id = id;
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
    public List<String> getPeptides() {
        String[] peptides = ClusterUtilities.getMostCommonPeptides(this);

        return new ArrayList<String>(Arrays.asList(peptides));
    }

    @Override
    public String getMostCommonPeptide() {
        List<String> peptideStrings = getPeptides();
        if (!peptideStrings.isEmpty()) {
            return peptideStrings.get(0);
        }
        return null;
    }


    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    public
    @Nonnull
    List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd) {
        buildPeptidePurities(dd);
        return byPurity;

    }


    protected void buildPeptidePurities(IDecoyDiscriminator dd) {
        if (byPurity.size() > 0)
            return;
        byPurity.clear();
        double numberSpectra = getClusteredSpectraCount();
        CountedString[] items = CountedString.getCountedStrings(getPeptides());
        for (int i = 0; i < items.length; i++) {
            CountedString item = items[i];
            String value = item.getValue();
            String[] peptides = value.split(";");
            boolean decoy = false;
            for (int j = 0; j < peptides.length; j++) {
                String peptide = peptides[j];
                if (peptide != null && dd != null)
                    decoy |= dd.isDecoy(peptide);
                else
                    decoy = false; // break here
            }

            ClusterPeptideFraction e = new ClusterPeptideFraction(peptides[0], item.getCount() / numberSpectra, decoy);
            byPurity.add(e);
        }
    }


    @Override
    public List<IPeak> getPeaks() {
        return getConsensusSpectrum().getPeaks();
    }

    @Override
    public int getPeaksCount() {
        return getConsensusSpectrum().getPeaksCount();
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
        return internalGetConcensusSpectrum();
    }

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guarantee clean can be byPassed
     *
     * @return exactly the current concensus spectrum
     */
    @Override
    public ISpectrum internalGetConcensusSpectrum() {
        return consensusSpectrumBuilder.getConsensusSpectrum();
    }


    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Override
    public boolean containsMajorPeak(final int mz) {
        return internalGetConcensusSpectrum().containsMajorPeak(mz);
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
     *
     * @param merged !null list to add
     */
    @Override
    public void addSpectra(List<ISpectrum> merged) {
        addSpectra(merged.toArray(new ISpectrum[merged.size()]));
    }


    @Override
    public void addSpectra(ISpectrum... merged) {
        if (merged != null && merged.length > 0) {
            boolean spectrumAdded = false;
            for (ISpectrum spectrumToMerge : merged) {
                spectraIds.add(spectrumToMerge.getId());
                if (!clusteredSpectra.contains(spectrumToMerge)) {
                    spectrumAdded = true;
                    clusteredSpectra.add(spectrumToMerge);
                }
            }
            if (spectrumAdded)
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
        // return !isStable();
        return true;
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isStable() {
        return ClusterUtilities.isClusterStable(this);
    }

    /**
     * if true the cluster is semi stable and will not allow removal
     *
     * @return
     */
    @Override
    public boolean isSemiStable() {
        return ClusterUtilities.isClusterSemiStable(this);
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        if (!isRemoveSupported())
            throw new UnsupportedOperationException("Remove not supported");

        if (removed != null && removed.length > 0) {
            for (ISpectrum spectrumToMerge : removed) {
                spectraIds.add(spectrumToMerge.getId());
                clusteredSpectra.remove(spectrumToMerge);
            }

            notifySpectrumHolderListeners(false, removed); // tell other interested parties  false says this is a remove
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
        try {
            int ret = CompareTo.compare(getPrecursorMz(), o.getPrecursorMz());
            if (ret != 0)
                return ret;
            if (getPrecursorCharge() != o.getPrecursorCharge()) {
                return getPrecursorCharge() < o.getPrecursorCharge() ? -1 : 1;
            }
            if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
                return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
            }

            ISpectrum highestQualitySpectrum1 = getHighestQualitySpectrum();
            ISpectrum highestQualitySpectrum2 = o.getHighestQualitySpectrum();
            if (highestQualitySpectrum1 != highestQualitySpectrum2) {
                if (highestQualitySpectrum1 == null || highestQualitySpectrum2 == null) {
                    //noinspection UnusedAssignment
                    highestQualitySpectrum1 = getHighestQualitySpectrum();
                    throw new IllegalStateException("problem"); // ToDo change
                }

                return highestQualitySpectrum1.getQualityScore() < highestQualitySpectrum2.getQualityScore() ? -1 : 1;
            }
        } catch (IllegalStateException e) {
            //  give up use hash code
        }

        int hash1 = hashCode();
        int hash2 = o.hashCode();
        if (hash1 != hash2)
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
        } else {
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
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void append(Appendable out) {

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
        } catch (IOException e) {
            throw new RuntimeException(e);

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

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            List<ISpectrum> clusteredSpectra1 = getClusteredSpectra();
            String s = ClusterUtilities.mostCommonPeptides(clusteredSpectra1);
            out.append("sequence=[" + s + "]");
            out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(getConsensusSpectrum()));
            out.append("\n");

            List<ISpectrum> spectra = clusteredSpectra1;
            Collections.sort(spectra, SpectrumIDComparator.INSTANCE);   // sort by id
            for (ISpectrum spec : spectra) {
                StringBuilder sb = new StringBuilder();
                sb.append("SPEC\t");
                String id1 = spec.getId();
                while (id1.startsWith("="))
                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
                sb.append(id1);
                sb.append("\ttrue\n");  // changed to look at output
                String csq = sb.toString();
                out.append(csq);

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
        double precursorMZ = getPrecursorMz();
        String text =
                "charge= " + getPrecursorCharge() + "," +
                        "mz= " + String.format("%10.3f", precursorMZ).trim() + "," +
                        "count= " + clusteredSpectra.size() +
                        "peptide= " + ClusterUtilities.mostCommonPeptides(this) +
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
    @SuppressWarnings({"UnusedDeclaration", "StringConcatenationInsideStringBufferAppend"})
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
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
