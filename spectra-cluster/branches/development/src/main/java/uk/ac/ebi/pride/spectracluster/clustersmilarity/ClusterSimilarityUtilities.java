package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityUtilities
 * User: Steve
 * Date: 6/27/13
 */
public class ClusterSimilarityUtilities {

    public static IClusterSet buildFromClusteringFile(File file, ISpectrumRetriever spectrumRetriever) {
        SimpleClusterSet simpleClusterSet = new SimpleClusterSet();

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                IClusterSet clusterSet = buildFromClusteringFile(file1, spectrumRetriever);
                simpleClusterSet.addClusters(clusterSet.getClusters());
            }
        } else if (file.getName().endsWith(".clustering")) {
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                ISpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(lineNumberReader, spectrumRetriever);
                simpleClusterSet.addClusters(Arrays.asList(clusters));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return simpleClusterSet;
    }

    public static void buildFromMgfFile(File file, IMutableSpectrumRetriever spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                buildFromMgfFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".mgf")) {
            try {
                System.out.println(file.getName());
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                IPeptideSpectrumMatch[] spectra = ParserUtilities.readMGFScans(lineNumberReader);
                spectrumRetriever.addSpectra(spectra);
                lineNumberReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void buildFromTSVFile(File file, IMutableSpectrumRetriever spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                buildFromTSVFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".tsv")) {
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                String line;
                while((line = lineNumberReader.readLine()) != null) {
                    IPeptideSpectrumMatch peptideSpectrumMatch = constructPeptideSpectrumMatch(line);
                    spectrumRetriever.addSpectra(peptideSpectrumMatch);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static IPeptideSpectrumMatch constructPeptideSpectrumMatch(String line) {
        String[] parts = line.split("\t");
        String spectrumId = parts[0];
        int precursorCharge = Integer.parseInt(parts[1]);
        float precursorMz = Float.parseFloat(parts[2]);
        String peptide = parts[3];

        return new PeptideSpectrumMatch(spectrumId, peptide, precursorCharge, precursorMz, Collections.EMPTY_LIST);
    }

    /**
     * return all clusters with more than one spectrum
     *
     * @param clusters !null list of clusters
     * @return !null list of clusters
     */
    public static List<ISpectralCluster> nonSingleClusters(List<ISpectralCluster> clusters) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            if (cluster.getClusteredSpectraCount() > 1)
                holder.add(cluster);
        }
        return holder;
    }

    /**
     * return all clusters with only  one spectrum
     *
     * @param clusters !null list of clusters
     * @return !null list of clusters
     */
    public static List<ISpectralCluster> singleClusters(List<ISpectralCluster> clusters) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            if (cluster.getClusteredSpectraCount() == 1)
                holder.add(cluster);
        }
        return holder;
    }

    public static void loadSpectrumTSVFile(String[] args) {
        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();
        buildFromTSVFile(new File(args[0]), simpleSpectrumRetriever);
        System.out.println(simpleSpectrumRetriever.getSpectra().size());
    }

    public static void main(String[] args) {
        SimpleSpectrumRetriever spectrumRetriever = new SimpleSpectrumRetriever();
        buildFromMgfFile(new File(args[0]), spectrumRetriever);
        System.out.println(spectrumRetriever.getSpectra().size());
    }

}
