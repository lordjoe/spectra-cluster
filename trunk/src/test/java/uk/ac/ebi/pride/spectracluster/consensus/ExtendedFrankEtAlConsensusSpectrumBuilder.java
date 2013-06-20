package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.normalizer.IntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.spectrum.*;


import java.util.Collection;
import java.util.List;

/**
 * TODo drop why is this here
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
    public List<IPeak> adaptPeakIntensities(List<IPeak> mergedConsensusSpectrum, double numberOfSpectra) {
        return super.adaptPeakIntensities(mergedConsensusSpectrum, numberOfSpectra);
    }

    @Override
    public List<IPeak> addAllPeaks(Collection<ISpectrum> spectra) {
        return super.addAllPeaks(spectra);
    }

    @Override
    public List<IPeak> mergeIdenticalPeaks(List<IPeak> consensusSpectrum) {
        return super.mergeIdenticalPeaks(consensusSpectrum);
    }
}
