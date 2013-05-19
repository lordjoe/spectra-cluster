package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.ConcensusSpectraItems
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class ConcensusSpectraItems {
    public static ConcensusSpectraItems[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ConcensusSpectraItems.class;

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
