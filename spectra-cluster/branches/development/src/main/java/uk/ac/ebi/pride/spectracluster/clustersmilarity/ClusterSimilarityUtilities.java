package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
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
        } else if (file.getName().endsWith(".clustering")){
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
        } else if (file.getName().endsWith(".mgf")){
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                IPeptideSpectrumMatch[] spectra = ParserUtilities.readMGFScans(lineNumberReader);
                for (IPeptideSpectrumMatch readSpectrum : spectra) {

                    spectrumRetriever.addSpectrum(spectra);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * return all clusters with more than one spectrum
     * @param clusters !null list of clusters
     * @return  !null list of clusters
     */
    public static List<ISpectralCluster>  nonSingleClusters( List<ISpectralCluster> clusters)     {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
          for (ISpectralCluster cluster : clusters) {
            if(cluster.getClusteredSpectraCount() > 1)
                holder.add(cluster);
        }
        return holder;
    }

    /**
      * return all clusters with only  one spectrum
      * @param clusters !null list of clusters
      * @return  !null list of clusters
      */
     public static List<ISpectralCluster>  singleClusters( List<ISpectralCluster> clusters)     {
         List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
           for (ISpectralCluster cluster : clusters) {
             if(cluster.getClusteredSpectraCount() == 1)
                 holder.add(cluster);
         }
         return holder;
     }


    public static void main(String[] args) {
//        IClusterSet originalClusterSet = buildFromClusteringFile(new File(args[0]), null);
//
//        int numberClusters = originalClusterSet.getClusterCount();
//
//        IClusterSet clusterSet = buildFromClusteringFile(new File(args[1]), null);
//
//        int numberClusters2 = clusterSet.getClusterCount();
//
//        ClusterStatistics originalClusterStatistics = new ClusterStatistics();
//
//        originalClusterSet.visitClusters(originalClusterStatistics);
//
//        System.out.println("Original");
//        System.out.println(originalClusterStatistics.generateReport());
//
//        ClusterStatistics clusterStatistics = new ClusterStatistics();
//
//        System.out.println("New");
//        clusterSet.visitClusters(clusterStatistics);
//
//        System.out.println(clusterStatistics.generateReport());

        SimpleSpectrumRetriever spectrumRetriever = new SimpleSpectrumRetriever();
        buildFromMgfFile(new File(args[0]), spectrumRetriever);
        System.out.println(spectrumRetriever.getSpectra().size());
    }

}
