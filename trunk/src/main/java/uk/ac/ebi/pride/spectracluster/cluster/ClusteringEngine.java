package uk.ac.ebi.pride.spectracluster.cluster;

//import java.io.*;
//import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngine {
//    public static List<ISpectrum> readMGFFile(File inp) {
//       BufferedRandomAccessFile junk = null; // This is not loaded properly
//          try {
//              MgfFile mgfFile = new MgfFile(inp);
//              List<ISpectrum> spectra = new ArrayList<Spectrum>(mgfFile.getMs2QueryCount());
//              Iterator<Ms2Query> it = mgfFile.getMs2QueryIterator();
//              while (it.hasNext()) {
//                  Ms2Query query = it.next();
//                  if (query.getPrecursorIntensity() == null)
//                      query.setPeptideIntensity(1.0);
//
//                  spectra.add(query);
//              }
//              return spectra;
//          } catch (JMzReaderException e) {
//              throw new RuntimeException(e);
//          }
//      }
//
//      public static List<SpectraCluster> doClustering(List<Spectrum> spectra) {
//          SpectraClustering clustering = new FrankEtAlClustering();
//          // set the clustering parameters
//          clustering.setClusteringRounds(2);
//          clustering.setSimilarityThreshold(0.7);
//          List<SpectraCluster> generatedCluster = clustering.clusterSpectra(spectra);
//          return generatedCluster;
//      }
//
//      public static void clusterMGF(String arg) {
//          if (!arg.toLowerCase().endsWith(".mgf"))
//              throw new IllegalArgumentException("we only handle mgf files");
//
//          File inp = new File(arg);
//          if (!inp.exists())
//              throw new IllegalArgumentException("File " + arg + " does not exist");
//
//          List<Spectrum> spectra = readMGFFile(inp);
//
//          List<SpectraCluster> generatedCluster = doClustering(spectra);
//
//          String outFile = arg.substring(0, arg.length() - 4) + ".cgf";
//          ClusterWriter.dumpSpectra(outFile, generatedCluster);
//
//      }
//
//      public static void usage()
//      {
//          System.err.println("MyMgfFile.mgf  MyMgfFile2.mgf ...");
//      }
//
//      public static void main(String[] args) {
//          long start = System.currentTimeMillis();
//          if(args.length == 0)   {
//              usage();
//              return;
//          }
//          for (int i = 0; i < args.length; i++) {
//              String arg = args[i];
//              clusterMGF(arg);
//          }
//          long end = System.currentTimeMillis() ;
//          long del = end - start;
//          int delsec = (int)  del /1000;
//          int delmin = delsec / 60;
//          System.err.println("Finished in " + delmin);
//      }

}
