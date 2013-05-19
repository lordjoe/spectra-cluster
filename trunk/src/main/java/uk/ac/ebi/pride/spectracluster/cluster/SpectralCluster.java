package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectralCluster implements ISpectralCluster {

    private final String id;
    private ISpectrum consensusSpectrum;
    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private boolean dirty;
    private final ConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(String id, ConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;
        this.consensusSpectrum = null;
        this.dirty = false;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
    }

    public String getId() {
        return id;
    }

    public ISpectrum getConsensusSpectrum() {
        guaranteeClean();
        return consensusSpectrum;
    }

    public Collection<ISpectrum> getClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }

    public void addSpectra(ISpectrum... merged) {
        if (merged != null && merged.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToMerge : merged) {
                if (spectrumToMerge instanceof ISpectralCluster) {
                    Collection<ISpectrum> nestedSpectra = ((ISpectralCluster) spectrumToMerge).getClusteredSpectra();
                    for (ISpectrum nestedSpectrum : nestedSpectra) {
                        if (!clusteredSpectra.contains(spectrumToMerge)) {
                            clusteredSpectra.add(nestedSpectrum);
                        }
                    }

                } else if (!clusteredSpectra.contains(spectrumToMerge)){
                    clusteredSpectra.add(spectrumToMerge);
                }
            }
        }
    }

    public void removeSpectra(ISpectrum... removed) {
        if (removed != null && removed.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToRemove : removed) {
                if (spectrumToRemove instanceof ISpectralCluster) {
                    Collection<ISpectrum> nestedSpectra = ((ISpectralCluster) spectrumToRemove).getClusteredSpectra();
                    clusteredSpectra.removeAll(nestedSpectra);
                } else {
                    clusteredSpectra.remove(spectrumToRemove);
                }
            }
        }
    }

    private void guaranteeClean() {
        if (dirty) {
            consensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(clusteredSpectra);
            dirty = false;
        }
    }
}
