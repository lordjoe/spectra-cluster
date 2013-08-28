package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.ArrayList;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.OriginalSpectralCluster
 * User: jg
 * Date: 7/30/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class OriginalSpectralCluster implements ISpectralCluster {
    private final IConsensusSpectrumBuilder consensusSpectrum = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
    private List<ISpectrum> spectra = new ArrayList<ISpectrum>();

    public OriginalSpectralCluster(ISpectralCluster cluster) {
        spectra.addAll(cluster.getClusteredSpectra());
        consensusSpectrum.addSpectra(cluster.getClusteredSpectra().toArray(new ISpectrum[cluster.getClusteredSpectra().size()]));
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public float getPrecursorMz() {
        return consensusSpectrum.getConsensusSpectrum().getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        return consensusSpectrum.getConsensusSpectrum().getPrecursorCharge();
    }

    @Override
    public ISpectrum getConsensusSpectrum() {
        return consensusSpectrum.getConsensusSpectrum();
    }

    @Override
    public ISpectrum getHighestQualitySpectrum() {
        // TODO change to proper function

        if (spectra.size() < 1)
            return null;

        return spectra.get(0);
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
    public List<ISpectrum> getHighestQualitySpectra() {
        return null;
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        return new ArrayList<ISpectrum>(spectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return consensusSpectrum.getSpectraCount();
    }

    @Override
    public boolean containsMajorPeak(int mz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSpectra(List<ISpectrum> added) {
        addSpectra(added.toArray(new ISpectrum[added.size()]));
    }

    @Override
    /**
     * Sort according to m/z.
     */
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

        return 0;
    }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(ISpectralCluster other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] asMajorPeakMZs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IPeak> getPeaks() {
        return new ArrayList<IPeak>(consensusSpectrum.getConsensusSpectrum().getPeaks());
    }

    @Override
    public int getPeaksCount() {
        return consensusSpectrum.getConsensusSpectrum().getPeaks().size();
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        // spectra can deliberately added more than once
        consensusSpectrum.addSpectra(merged);
        //noinspection ManualArrayToCollectionCopy
        for (ISpectrum spectrum : merged) {
            spectra.add(spectrum);
        }
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        consensusSpectrum.removeSpectra(removed);
        for (ISpectrum spectrum : removed)
            spectra.remove(spectrum);
    }

    @Override
    public void addSpectrumHolderListener(ISpectrumHolderListener added) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSpectrumHolderListener(ISpectrumHolderListener removed) {
        throw new UnsupportedOperationException();
    }
}
