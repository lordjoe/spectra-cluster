package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import javax.annotation.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class FractionInClustersOfSizeStatistics implements IClusterStatistics<FractionInClustersOfSize> {
    private final FractionInClustersOfSize spectrumToClusters = new FractionInClustersOfSize();
    private final SimpleSpectrumRetriever spectra;

    public FractionInClustersOfSizeStatistics(final SimpleSpectrumRetriever pSpectra) {
        spectra = pSpectra;
    }

    @Override
    public FractionInClustersOfSize getStatistics() {
        return spectrumToClusters;
    }

    @Override
    public String generateDefaultReport() {
        StringBuilder sb = new StringBuilder();
        int spectraCount = spectra.getSpectraCount();
        sb.append("Total Spectra ").append(spectraCount).append("\n");

//        sb.append("Number Empty Clusters ").append(spectrumToClusters.getNumberOfEmptyCluster()).append("\n");
        int numberClustered = spectrumToClusters.getNumberInClusterOfSize(2);
        sb.append("Fraction Clustered Spectra ").append(String.format("%10.3f", ((double) numberClustered) / spectraCount).trim()).append("\n");
        numberClustered = spectrumToClusters.getNumberInClusterOfSize(8);
        sb.append("Fraction Medium Clustered Spectra ").append(String.format("%10.3f", ((double) numberClustered) / spectraCount).trim()).append("\n");
        numberClustered = spectrumToClusters.getNumberInClusterOfSize(30);
        sb.append("Fraction Stable Clustered Spectra ").append(String.format("%10.3f", ((double) numberClustered) / spectraCount).trim()).append("\n");

        return sb.toString();
    }

    @Override
    public void visit(@Nonnull ISpectralCluster pT) {
        spectrumToClusters.addCluster(pT);
    }
}
