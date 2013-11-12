package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SimpleSpectrumRetriever implements IMutableSpectrumRetriever {

    private final Map<String, IPeptideSpectrumMatch> spectraById = new HashMap<String, IPeptideSpectrumMatch>();
    private final Map<String, List<IPeptideSpectrumMatch>> spectraByPeptide = new HashMap<String, List<IPeptideSpectrumMatch>>();

    public SimpleSpectrumRetriever() {
    }

    public SimpleSpectrumRetriever(IPeptideSpectrumMatch... spectra) {
        this(Arrays.asList(spectra));
    }

    public SimpleSpectrumRetriever(Collection<IPeptideSpectrumMatch> spectra) {
        for (IPeptideSpectrumMatch spectrum : spectra) {
            addSpectrum(spectrum);
        }
    }

    public List<IPeptideSpectrumMatch> getSpectra() {
        return new ArrayList<IPeptideSpectrumMatch>(spectraById.values());
    }

    @Override
    public void addSpectrum(IPeptideSpectrumMatch... spectra) {
        for (IPeptideSpectrumMatch spectrum : spectra) {
            spectraById.put(spectrum.getId(), spectrum);
            addSpectrumByPeptide(spectrum);
        }
    }

    private void addSpectrumByPeptide(IPeptideSpectrumMatch spectrum) {
        String peptide = spectrum.getPeptide();
        List<IPeptideSpectrumMatch> spectrumMatches = spectraByPeptide.get(peptide);
        if (spectrumMatches == null) {
            spectrumMatches = new ArrayList<IPeptideSpectrumMatch>();
            spectraByPeptide.put(peptide, spectrumMatches);
        }

        spectrumMatches.add(spectrum);
    }

    @Override
    public IPeptideSpectrumMatch retrieve(String spectrumId) {
        return spectraById.get(spectrumId);
    }

    public List<IPeptideSpectrumMatch> retrieveByPeptide(String peptide) {
        List<IPeptideSpectrumMatch> holder = new ArrayList<IPeptideSpectrumMatch>();

        List<IPeptideSpectrumMatch> spectrumMatches = spectraByPeptide.get(peptide);
        if (spectrumMatches != null) {
            holder.addAll(spectrumMatches);
        }

        return holder;
    }
}
