package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumToClustersStatistics implements IClusterStatistics<SpectrumToClusters>{
    private final SpectrumToClusters spectrumToClusters = new SpectrumToClusters();

    @Override
    public SpectrumToClusters getStatistics() {
        return spectrumToClusters;
    }

    @Override
    public String generateDefaultReport() {
        StringBuilder sb = new StringBuilder();

//        sb.append("Number Empty Clusters ").append(spectrumToClusters.getNumberOfEmptyCluster()).append("\n");
        sb.append("Number Duplicated Spectra ").append(spectrumToClusters.getNumberOfDuplicatedSpectra()).append("\n");
        sb.append("Total Number Spectra ").append(spectrumToClusters.getNumberOfSpectra()).append("\n");
        sb.append("Total Number Unique Spectra ").append(spectrumToClusters.getUniqueSpectra()).append("\n");

        return sb.toString();
    }

    @Override
    public void visit(@Nonnull ISpectralCluster pT) {
        spectrumToClusters.addCluster(pT);
    }
}
