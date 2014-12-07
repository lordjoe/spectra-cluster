package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.io.ISpectrumWriter;
import uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.io.MgfSpectrumWriter;
import uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.spectrum_fetcher.SpectrumFileFetcher;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.spectra.ArchiveSpectraRetriever;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jg on 07.12.14.
 */
public class ClusteringFilesProcessor implements IClusterProcessor, IClusterSourceListener {
    private final String[] clusteringFilenames;
    private final int minSize;
    private final int maxSize;
    private final float minRatio;
    private final float maxRatio;

    private boolean disableSpectraFetcher = false;
    private String outputPath = ".";
    private boolean ignoreIncompleteClusters = true;

    private ArchiveSpectraRetriever spectraRetriever = new ArchiveSpectraRetriever();
    private SpectrumFileFetcher spectrumFileFetcher;
    private ISpectrumWriter spectrumWriter = new MgfSpectrumWriter();

    public ClusteringFilesProcessor(String[] clusteringFilenames, int minSize, int maxSize, float minRatio, float maxRatio) {
        this.clusteringFilenames = clusteringFilenames;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
    }

    @Override
    public void processClusters() throws Exception {
        if (spectrumFileFetcher == null) {
            spectrumFileFetcher = new SpectrumFileFetcher();
        }

        for (String clusteringFilename : clusteringFilenames) {
            System.out.println("Processing " + clusteringFilename + "...");
            ClusteringFileReader clusteringFileReader = new ClusteringFileReader(new File(clusteringFilename));

            List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
            listeners.add(this);
            clusteringFileReader.readClustersIteratively(listeners);
        }
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        if (newCluster.getSpecCount() < minSize)
            return;
        if (newCluster.getSpecCount() > maxSize)
            return;
        if (newCluster.getMaxRatio() < minRatio)
            return;
        if (newCluster.getMaxRatio() > maxRatio)
            return;

        try {
            System.out.print("  Fetching spectra for cluster " + newCluster.getId() + " ...");
            List<Spectrum> spectra = fetchSpectraForCluster(newCluster, spectraRetriever, disableSpectraFetcher);;
            System.out.println("OK.");

            // write the spectra to a file
            String outputFilePath = outputPath + newCluster.getId() + ".mgf";
            spectrumWriter.writeClusterSpectra(spectra, outputFilePath, newCluster);
            System.out.println("    Spectra written to " + outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Spectrum> fetchSpectraForCluster(ICluster cluster, ArchiveSpectraRetriever spectraRetriever, boolean useOnlyFileFetcher) throws Exception {
        List<Spectrum> spectra = new ArrayList<Spectrum>();
        SpectrumFileFetcher spectrumFileFetcher = new SpectrumFileFetcher();

        for (ISpectrumReference specRef : cluster.getSpectrumReferences()) {
            List<Spectrum> spectrum = null;

            if (!useOnlyFileFetcher) {
                spectrum = spectraRetriever.findById(specRef.getSpectrumId());
            }

            if (spectrum == null || spectrum.size() < 1) {
                spectrum = spectrumFileFetcher.getSpectrumAsList(specRef.getSpectrumId());
            }

            if (spectrum.size() < 1 && ignoreIncompleteClusters) {
                throw new Exception("Failed to retrieve spectrum " + specRef.getSpectrumId());
            }
            else if (spectrum.size() < 1) {
                System.out.println("Error: Failed to retrieve spectrum " + specRef.getSpectrumId());
            }

            spectra.addAll(spectrum);
        }

        return spectra;
    }

    @Override
    public void setDisableSpectraFetcher(boolean disableSpectraFetcher) {
        this.disableSpectraFetcher = disableSpectraFetcher;
    }

    @Override
    public boolean isDisableSpectraFetcher() {
        return disableSpectraFetcher;
    }

    @Override
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public void setIgnoreIncompleteClusters(boolean ignoreIncompleteCluster) {
        this.ignoreIncompleteClusters = ignoreIncompleteCluster;
    }

    @Override
    public boolean isIgnoreIncompleteClusters() {
        return ignoreIncompleteClusters;
    }
}
