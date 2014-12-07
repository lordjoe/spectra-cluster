package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cluster_fetcher.ClusterFetcher;
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
import java.util.*;

/**
 * Created by jg on 07.12.14.
 */
public class ClusterFilelistProcessor implements IClusterProcessor {
    private final String clusterListPath;
    private boolean disableSpectraFetcher = false;
    private String outputPath = ".";
    private boolean ignoreIncompleteClusters = true;

    public ClusterFilelistProcessor(String clusterFilePath) {
        this.clusterListPath = clusterFilePath;
    }

    @Override
    public void processClusters() throws Exception {
        Map<String, Set<String>> clusterIdsPerFile = processClusterList(clusterListPath);
        ArchiveSpectraRetriever spectraRetriever = new ArchiveSpectraRetriever();
        ISpectrumWriter spectrumWriter = new MgfSpectrumWriter();

        for (String clusterFilePath : clusterIdsPerFile.keySet()) {
            // get the clusters from the file
            System.out.println("Processing clusters from " + clusterFilePath + "...");
            List<ICluster> extractedClusters = extractClustersFromFile(clusterFilePath, clusterIdsPerFile.get(clusterFilePath));

            if (!ignoreIncompleteClusters && extractedClusters.size() != clusterIdsPerFile.get(clusterFilePath).size()) {
                throw new Exception("Failed to extract all defined clusters from " + clusterFilePath);
            }

            System.out.println("  Extracted " + extractedClusters.size() + "/" + clusterIdsPerFile.get(clusterFilePath).size() + " clusters.");

            // get the spectra for each cluster
            for (ICluster cluster : extractedClusters) {
                System.out.print("  Fetching spectra for cluster " + cluster.getId() + " ...");
                List<Spectrum> spectra = null;
                try {
                    spectra = fetchSpectraForCluster(cluster, spectraRetriever, disableSpectraFetcher);
                }
                catch (Exception e) {
                    if (ignoreIncompleteClusters) {
                        continue;
                    }
                    else {
                        throw e;
                    }
                }
                System.out.println("OK.");

                // write the spectra to a file
                String outputFilePath = outputPath + cluster.getId() + ".mgf";
                spectrumWriter.writeClusterSpectra(spectra, outputFilePath, cluster);
                System.out.println("    Spectra written to " + outputFilePath);
            }
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

    private List<ICluster> extractClustersFromFile(String clusterFilePath, Collection<String> clusterIds) throws Exception {
        // extract the clusters from the file using a ClusterFetcher
        ClusteringFileReader fileReader = new ClusteringFileReader(new File(clusterFilePath));
        ClusterFetcher clusterFetcher = new ClusterFetcher(clusterIds);

        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>();
        listeners.add(clusterFetcher);

        // read the file iteratively to avoid performance problems
        fileReader.readClustersIteratively(listeners);

        List<ICluster> extractedClusters = clusterFetcher.getExtractedClusters();

        return extractedClusters;
    }

    /**
     * Reads the file containing the list of clusters to extract from the .clustering files.
     * @param clusterListPath
     * @return
     */
    private Map<String, Set<String>> processClusterList(String clusterListPath) throws Exception {
        File clusteringListFile = new File(clusterListPath);

        if (!clusteringListFile.exists() || !clusteringListFile.canRead()) {
            throw new Exception("Cannot find " + clusterListPath);
        }

        BufferedReader br = new BufferedReader(new FileReader(clusteringListFile));

        Map<String, Set<String>> clusterIdsPerFile = new HashMap<String, Set<String>>();
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            // ignore empty lines
            if (line.length() < 1)
                continue;

            String[] fields = line.split("\t");
            if (fields.length < 2)
                throw new Exception("Illegal line encountered in " + clusterListPath + ": " + line);

            String filename = fields[0];
            String clusterId = fields[1];

            if (!clusterIdsPerFile.containsKey(filename)) {
                clusterIdsPerFile.put(filename, new HashSet<String>());
            }

            clusterIdsPerFile.get(filename).add(clusterId);
        }

        return clusterIdsPerFile;
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
