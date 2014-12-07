package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.io;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;

import java.util.List;

/**
 * Created by jg on 07.12.14.
 */
public interface ISpectrumWriter {
    /**
     * Writes the spectra to the defined output path.
     * @param spectra
     * @param outputFilePath
     * @param cluster
     * @throws Exception
     */
    public void writeClusterSpectra(List<Spectrum> spectra, String outputFilePath, ICluster cluster) throws Exception;
}
