package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.normalizer.IntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;

import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ExtendedFrankEtAlConsensusSpectrumBuilder extends FrankEtAlConsensusSpectrumBuilder {

    public ExtendedFrankEtAlConsensusSpectrumBuilder(IntensityNormalizer intensityNormalizer) {
        super(intensityNormalizer);
    }

    @Override
    public PeptideSpectrumMatch createConsensusSpectrum(Collection<ISpectrum> spectra, List<IPeak> filteredSpectrum) {
        return super.createConsensusSpectrum(spectra, filteredSpectrum);
    }

    @Override
    public List<IPeak> filterSpectrum(List<IPeak> mergedConsensusSpectrum) {
        return super.filterSpectrum(mergedConsensusSpectrum);
    }

    @Override
    public void adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        super.adaptPeakIntensities(mergedConsensusSpectrum, numberOfSpectra);
    }

    @Override
    public List<IPeak> addAllPeaks(Collection<ISpectrum> spectra) {
        return super.addAllPeaks(spectra);
    }

    @Override
    public Collection<IPeak> originalAddAllPeaks(Collection<ISpectrum> spectra) {
        return super.originalAddAllPeaks(spectra);
    }

    @Override
    public List<IPeak> mergeIdenticalPeaks(List<IPeak> consensusSpectrum) {
        return super.mergeIdenticalPeaks(consensusSpectrum);
    }
}
