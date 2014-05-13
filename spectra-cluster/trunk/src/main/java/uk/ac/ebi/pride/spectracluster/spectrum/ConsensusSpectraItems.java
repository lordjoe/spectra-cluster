package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ConcensusSpectraItems
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class ConsensusSpectraItems {
    public static ConsensusSpectraItems[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ConsensusSpectraItems.class;

    private ISpectrum concensus;
    private List<ISpectrum> spectra;

    public ISpectrum getConcensus() {
        return concensus;
    }

    public void setConcensus(ISpectrum concensus) {
        this.concensus = concensus;
    }

    public List<ISpectrum> getSpectra() {
        return spectra;
    }

    public void setSpectra(List<ISpectrum> spectra) {
        this.spectra = spectra;
    }
}
