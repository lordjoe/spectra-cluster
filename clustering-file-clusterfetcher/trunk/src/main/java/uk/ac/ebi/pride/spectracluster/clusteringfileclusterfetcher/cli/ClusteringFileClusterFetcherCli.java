package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cluster_fetcher.ClusterFetcher;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.spectra.ArchiveSpectraRetriever;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;

import java.io.*;
import java.util.*;

/**
 * Created by jg on 02.12.14.
 */
public class ClusteringFileClusterFetcherCli {
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(),
                    args);

            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printHelp();
                return;
            }

            String clusterListPath = commandLine.getOptionValue(CliOptions.OPTIONS.CLUSTER_FILE.getValue(), "");

            if (clusterListPath.equals("")) {
                throw new Exception("Missing required parameter '" + CliOptions.OPTIONS.CLUSTER_FILE.getValue() + "'");
            }

            String outputPath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue(), "");
            if (outputPath.equals("")) {
                throw new Exception("Missing required paramter '" + CliOptions.OPTIONS.OUTPUT_PATH.getValue() + "'");
            }


            Map<String, Set<String>> clusterIdsPerFile = processClusterList(clusterListPath);
            ArchiveSpectraRetriever spectraRetriever = new ArchiveSpectraRetriever();

            for (String clusterFilePath : clusterIdsPerFile.keySet()) {
                // get the cluters from the file
                System.out.println("Processing clusters from " + clusterFilePath + "...");
                List<ICluster> extractedClusters = extractClustersFromFile(clusterFilePath, clusterIdsPerFile.get(clusterFilePath));

                System.out.println("  Extracted " + extractedClusters.size() + "clusters.");

                // get the spectra for each cluster
                for (ICluster cluster : extractedClusters) {
                    System.out.print("  Fetching spectra for cluster " + cluster.getId() + "...");
                    List<Spectrum> spectra = fetchSpectraForCluster(cluster, spectraRetriever);
                    System.out.println("OK.");

                    // write the spectra to a file
                    String outputFilePath = outputPath + cluster.getId() + ".mgf";
                    writeSpectraToFile(spectra, outputFilePath);
                    System.out.println("    Spectra written to " + outputFilePath);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeSpectraToFile(List<Spectrum> spectra, String outputFilePath) throws Exception {
        FileWriter fw = new FileWriter(outputFilePath);

        for (Spectrum s : spectra) {
            double[] mzValues = s.getPeaksMz();
            double[] intensityValues = s.getPeaksIntensities();

            if (mzValues.length != intensityValues.length) {
                throw new Exception("Spectrum " + s.getId() + " contains a different number of m/z and intensity values.");
            }


            fw.write("BEGIN IONS\n");
            fw.write("TITLE=" + s.getId() + "\n");
            fw.write("PEPMASS=" + s.getPrecursorMz() + "\n");
            fw.write("CHARGE=" + s.getPrecursorCharge() + "\n");
            // write the peaks
            for (int i = 0; i < mzValues.length; i++) {
                fw.write(mzValues[i] + " " + intensityValues[i] + "\n");
            }

            fw.write("END IONS\n");
        }

        fw.close();
    }

    private static List<Spectrum> fetchSpectraForCluster(ICluster cluster, ArchiveSpectraRetriever spectraRetriever) {
        List<Spectrum> spectra = new ArrayList<Spectrum>();

        for (ISpectrumReference specRef : cluster.getSpectrumReferences()) {
            spectra.addAll(spectraRetriever.findById(specRef.getSpectrumId()));
        }

        return spectra;
    }

    private static List<ICluster> extractClustersFromFile(String clusterFilePath, Collection<String> clusterIds) throws Exception {
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
    private static Map<String, Set<String>> processClusterList(String clusterListPath) throws Exception {
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

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar {VERSION}.jar",
                        "Writes the spectra of a specified cluster to an MGF file.",
                        CliOptions.getOptions(), "\n\n", true);
    }
}
