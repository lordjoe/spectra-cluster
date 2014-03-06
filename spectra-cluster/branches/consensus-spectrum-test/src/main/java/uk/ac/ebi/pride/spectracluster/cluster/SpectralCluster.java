package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.Equivalent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectralCluster implements ISpectralCluster, ISpectrumHolder, Equivalent<ISpectralCluster> {


    private final String id;

      // holds a list of the top  SpectralQualityHolder.NUMBER_SPECTRA_FOR_CONSENSUS = 20;
    // quality spectra - these can be use to build a concensus of quality
    // Note all adds and removes are done by registering as a SpectrumHolderListener
    private final SpectralQualityHolderListener qualityHolderListener;

    private final List<ISpectrumHolderListener> spectrumHolderListeners = new CopyOnWriteArrayList<ISpectrumHolderListener>();


    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();

    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(ISpectralCluster copied) {
        this(copied.getId(), Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());

        List<ISpectrum> clusterDefaultsedSpectra1 = copied.getClusteredSpectra();
        addSpectra(clusterDefaultsedSpectra1);
    }


    public SpectralCluster(String id) {
        this(id, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;

        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
        addSpectrumHolderListener(this.consensusSpectrumBuilder);

        this.qualityHolderListener = new SpectralQualityHolderListener();
        addSpectrumHolderListener(qualityHolderListener);
    }


    /**
     * add a change listener
     * final to make sure this is not duplicated at multiple levels
     *
     * @param added non-null change listener
     */
    @Override
    public final void addSpectrumHolderListener(ISpectrumHolderListener added) {
        if (!spectrumHolderListeners.contains(added))
            spectrumHolderListeners.add(added);
    }

    /**
     * remove a change listener
     *
     * @param removed non-null change listener
     */
    @Override
    public final void removeSpectrumHolderListener(ISpectrumHolderListener removed) {
        while (spectrumHolderListeners.contains(removed))
            spectrumHolderListeners.remove(removed);
    }


    /**
     * notify any state change listeners - probably should
     * be protected but is in the interface to form an event cluster
     *
     */
    protected void notifySpectrumHolderListeners(boolean isAdd, ISpectrum... spectra) {
        if (spectrumHolderListeners.isEmpty())
            return;

        for (ISpectrumHolderListener listener : spectrumHolderListeners) {
            if (isAdd)
                listener.update(new SpectrumHolderEvent(this, SpectrumHolderEvent.Type.ADD, spectra));
            else
                listener.update(new SpectrumHolderEvent(this, SpectrumHolderEvent.Type.REMOVE, spectra));
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

    /**
     * all internally spectrum
     */
    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        return qualityHolderListener.getHighestQualitySpectra();
    }


    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    @Override
    public ISpectrum getHighestQualitySpectrum() {
        return qualityHolderListener.getHighestQualitySpectrum();
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
        return true;
    }


    @Override
    public void removeSpectra(ISpectrum... removed) {
        if(!isRemoveSupported())
            throw new UnsupportedOperationException("Remove not supported");

        if (removed != null && removed.length > 0) {

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
        if (getPrecursorMz() != o.getPrecursorMz()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (getPrecursorCharge() != o.getPrecursorCharge()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
        }

        /**
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
         **/

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
