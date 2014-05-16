package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumToClustersStatistics implements IClusterStatistics<SpectrumToClusters> {
    private final SpectrumToClusters spectrumToClusters = new SpectrumToClusters();
    private final ISpectrumRetriever totalSpectra;

    public SpectrumToClustersStatistics(final ISpectrumRetriever pTotalSpectra) {
        totalSpectra = pTotalSpectra;
    }

    @Override
    public SpectrumToClusters getStatistics() {
        return spectrumToClusters;
    }

    @Override
    public String generateDefaultReport() {
        StringBuilder sb = new StringBuilder();

        List<ISpectrum> usedSpectra = spectrumToClusters.getUsedSpectra(totalSpectra);
        List<ISpectrum> unusedSpectra = spectrumToClusters.getUnusedSpectra(totalSpectra);
        sb.append("Number Unsed Spectra ").append(unusedSpectra.size()).append("\n");
        sb.append("Number Used Spectra ").append(usedSpectra.size()).append("\n");

//        sb.append("Number Empty Clusters ").append(spectrumToClusters.getNumberOfEmptyCluster()).append("\n");
        sb.append("Number Duplicated Spectra ").append(spectrumToClusters.getNumberOfDuplicatedSpectra()).append("\n");
        sb.append("Total Number Spectra ").append(spectrumToClusters.getNumberOfSpectra()).append("\n");
        sb.append("Total Number Unique Spectra ").append(spectrumToClusters.getUniqueSpectra()).append("\n");

        return sb.toString();
    }

    @Override
    public void visit(@Nonnull IPeptideSpectralCluster pT) {
        spectrumToClusters.addCluster(pT);
    }
}
