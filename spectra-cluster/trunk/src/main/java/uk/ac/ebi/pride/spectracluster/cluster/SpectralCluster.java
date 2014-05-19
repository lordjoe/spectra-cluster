package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.CompareTo;
import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Constants;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Default implementation of ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class SpectralCluster implements ICluster {

    private String id;
    // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolder qualityHolder;
    private final List<SpectrumHolderListener> spectrumHolderListeners = new CopyOnWriteArrayList<SpectrumHolderListener>();
    private final Set<String> spectraIds = new HashSet<String>();

    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(ICluster copied) {
        this(copied.getId(), ConsensusSpectrum.FACTORY.getConsensusSpectrumBuilder());

        final List<ISpectrum> clusteredSpectra1 = copied.getClusteredSpectra();
        addSpectra(clusteredSpectra1.toArray(new ISpectrum[clusteredSpectra1.size()]));
    }

    /**
     * use this when the cluster is not stable
     */
    public SpectralCluster() {
        this(null, ConsensusSpectrum.FACTORY.getConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id) {
        this(id, ConsensusSpectrum.FACTORY.getConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
        addSpectrumHolderListener(this.consensusSpectrumBuilder);
        this.qualityHolder = new SpectralQualityHolder();
        addSpectrumHolderListener(qualityHolder);
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
        if (!spectrumHolderListeners.contains(added))
            spectrumHolderListeners.add(added);
    }

    /**
     * remove a change listener
     *
     * @param removed non-null change listener
     */
    @Override
    public final void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        while (spectrumHolderListeners.contains(removed))
            spectrumHolderListeners.remove(removed);
    }


    /**
     * notify any state change listeners - probably should
     * be protected but is in the interface to form an event cluster
     */
    protected void notifySpectrumHolderListeners(boolean isAdd, ISpectrum... spectra) {
        if (spectrumHolderListeners.isEmpty())
            return;
        for (SpectrumHolderListener listener : spectrumHolderListeners) {
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
        return consensusSpectrumBuilder.getConsensusSpectrum();
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
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
    public int compareTo(ICluster o) {
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
    public boolean equivalent(ICluster o) {
        if (o == this)
            return true;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return false;
        double del = o.getPrecursorMz() - getPrecursorMz();
        double abs = Math.abs(del);
        if (abs > Constants.SMALL_MZ_DIFFERENCE) {
            return false;
        }

        List<ISpectrum> spc1 = clusteredSpectra;
        List<ISpectrum> spc2 = o.getClusteredSpectra();

        if (spc1.size() != spc2.size()) {
            return false;
        }
        if (spc1.size() <= 1) {

            List<IPeak> peaks = getConsensusSpectrum().getPeaks();
            List<IPeak> peaks1 = o.getConsensusSpectrum().getPeaks();
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
}
