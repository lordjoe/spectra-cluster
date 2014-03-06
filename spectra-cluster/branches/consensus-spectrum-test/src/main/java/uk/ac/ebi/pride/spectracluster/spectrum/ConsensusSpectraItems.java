package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.List;

/**
 * uk.ac.ebi.uk.ac.ebi.pride.spectracluster.spectrum.ConcensusSpectraItems
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class ConsensusSpectraItems {

    private ISpectrum consensus;
    private List<ISpectrum> spectra;

    public ISpectrum getConsensus() {
        return consensus;
    }

    public void setConsensus(ISpectrum consensus) {
        this.consensus = consensus;
    }

    public List<ISpectrum> getSpectra() {
        return spectra;
    }

    public void setSpectra(List<ISpectrum> spectra) {
        this.spectra = spectra;
    }
}
