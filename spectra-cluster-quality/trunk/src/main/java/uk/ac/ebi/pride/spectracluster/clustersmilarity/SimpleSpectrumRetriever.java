package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SimpleSpectrumRetriever implements IMutableSpectrumRetriever {

    private final Map<String, ISpectrum> spectraById = new HashMap<String, ISpectrum>();
    private final Map<String, List<ISpectrum>> spectraByPeptide = new HashMap<String, List<ISpectrum>>();

    public SimpleSpectrumRetriever() {
    }

    public SimpleSpectrumRetriever(ISpectrum... spectra) {
        this(Arrays.asList(spectra));
    }

    public SimpleSpectrumRetriever(Collection<ISpectrum> spectra) {
        for (ISpectrum spectrum : spectra) {
            addSpectra(spectrum);
        }
    }

    public List<ISpectrum> getSpectra() {
        return new ArrayList<ISpectrum>(spectraById.values());
    }

    @Override
    public void addSpectra(ISpectrum... spectra) {
        for (ISpectrum spectrum : spectra) {
            spectraById.put(spectrum.getId(), spectrum);
            addSpectrumByPeptide(spectrum);
        }
    }

    private void addSpectrumByPeptide(ISpectrum spectrum) {
        final String peptidesProp = spectrum.getProperty(ISpectrum.IDENTIFIED_PEPTIDE_KEY);
        if(peptidesProp == null)
            return;
        String[] peptides = peptidesProp.split(";");
        for (String peptide : peptides) {
            List<ISpectrum> spectrumMatches = spectraByPeptide.get(peptide);
            if (spectrumMatches == null) {
                spectrumMatches = new ArrayList<ISpectrum>();
                spectraByPeptide.put(peptide, spectrumMatches);
            }

            spectrumMatches.add(spectrum);
        }
    }

    @Override
    public ISpectrum retrieve(String spectrumId) {
        ISpectrum iPeptideSpectrumMatch = spectraById.get(spectrumId);
        if (iPeptideSpectrumMatch != null)
            return iPeptideSpectrumMatch;
        //  System.out.println("Cannot find " + spectrumId);
        return null;
    }

    public List<ISpectrum> retrieveByPeptide(String peptide) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();

        List<ISpectrum> spectrumMatches = spectraByPeptide.get(peptide);
        if (spectrumMatches != null) {
            holder.addAll(spectrumMatches);
        }

        return holder;
    }


    @Override
    public List<ISpectrum> retrieveAll() {
        List<ISpectrum> holder = new ArrayList<ISpectrum>(spectraById.values());
        Collections.sort(holder);
        return holder;
    }


    public int getSpectraCount() {
        return spectraById.size();
    }
}
