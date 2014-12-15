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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jg on 07.12.14.
 */
public class ClusteringFilesProcessor implements IClusterProcessor, IClusterSourceListener {
    private final String[] clusteringFilenames;
    private final int minSize;
    private final int maxSize;
    private final float minRatio;
    private final float maxRatio;
    private final String clusterIdFilePath;

    private boolean disableSpectraFetcher = false;
    private String outputPath = ".";
    private boolean ignoreIncompleteClusters = true;
    private boolean ignoreExisting = false;

    private ArchiveSpectraRetriever spectraRetriever = new ArchiveSpectraRetriever();
    private SpectrumFileFetcher spectrumFileFetcher;
    private ISpectrumWriter spectrumWriter = new MgfSpectrumWriter();

    Set<String> clusterIds = new HashSet<String>();

    public ClusteringFilesProcessor(String[] clusteringFilenames, int minSize, int maxSize, float minRatio, float maxRatio, String clusterIdFilePath) {
        this.clusteringFilenames = clusteringFilenames;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
        this.clusterIdFilePath = clusterIdFilePath;
    }

    @Override
    public void processClusters() throws Exception {
        if (spectrumFileFetcher == null) {
            spectrumFileFetcher = new SpectrumFileFetcher();
        }

        if (clusterIdFilePath != null) {
            loadClusterIdsFromFile(clusterIdFilePath);
        }

        for (String clusteringFilename : clusteringFilenames) {
            System.out.println("Processing " + clusteringFilename + "...");
            ClusteringFileReader clusteringFileReader = new ClusteringFileReader(new File(clusteringFilename));

            List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
            listeners.add(this);
            clusteringFileReader.readClustersIteratively(listeners);
        }
    }

    private void loadClusterIdsFromFile(String clusterIdFilePath) throws Exception {
        File clusterIdFile = new File(clusterIdFilePath);

        if (!clusterIdFile.exists() || !clusterIdFile.canRead()) {
            throw new Exception("Cannot read from " + clusterIdFilePath);
        }

        clusterIds.clear();

        BufferedReader br = new BufferedReader(new FileReader(clusterIdFile));

        String line;
        while ((line = br.readLine()) != null) {
            clusterIds.add(line.trim());
        }
        br.close();
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
        // if cluster ids were provided only export these
        if (clusterIds.size() > 0 && !clusterIds.contains(newCluster.getId()))
            return;

        // ignore the cluster if the option is set
        String outputFilePath = outputPath + newCluster.getId() + ".mgf";
        if (ignoreExisting) {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists())
                return;
        }

        try {
            System.out.print("  Fetching spectra for cluster " + newCluster.getId() + " ...");
            List<Spectrum> spectra = fetchSpectraForCluster(newCluster, spectraRetriever, disableSpectraFetcher);;
            System.out.println("OK.");

            // write the spectra to a file
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

    @Override
    public void setIgnoreExisting(boolean ignoreExisting) {
        this.ignoreExisting = ignoreExisting;
    }

    @Override
    public boolean isIgnoreExisting() {
        return ignoreExisting;
    }
}
