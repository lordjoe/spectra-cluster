package uk.ac.ebi.pride.spectracluster.hadoop;

import uk.ac.ebi.pride.spectracluster.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraHadoopUtilities
 * User: Steve
 * Date: 8/13/13
 * static general purpose routines for handling hadoopy things
 */
public class SpectraHadoopUtilities {

    public static final int MIMIMUM_CLUSTER_LENGTH = 5 * "BEGIN IONS\n".length();

    /**
     * convert am int into an mz for easy comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static String mzToKey(double mz) {
        int peak = ClusterUtilities.mzToInt(mz);
        //noinspection UnnecessaryLocalVariable
        String str = String.format("%010d", peak);
        return str;
    }
    /**
      * convert am int into an mz for east comparison
      *
      * @param key input
      * @return MZ_RESOLUTION * mz as int
      */
     public static double keyToMZ(String key) {
         //noinspection UnnecessaryLocalVariable
          double ret = Integer.parseInt(key); // (double)MZ_RESOLUTION;
          return ret / ClusterUtilities.MZ_RESOLUTION;
     }


//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, String paramsFile, String added) {
////        String paramsFile = buildOutputFileName(context, data);
////        if (added != null)
////            paramsFile += added;
//        OutputStream out = buildOutputStream(context, paramsFile, added);
//        PrintWriter ret = new PrintWriter(out);
//        return ret;
//    }
//
//
//    public static OutputStream buildOutputStream(TaskInputOutputContext context,
//                                                 HadoopTandemMain data, String added) {
//        final Configuration configuration = context.getConfiguration();
//        String paramsFile = buildOutputFileName(context, data);
//        String hpl = paramsFile.toLowerCase();
//        if (hpl.endsWith(".hydra")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".hydra".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (hpl.endsWith(".mzxml")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".mzXML".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (hpl.endsWith(".mzml")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".mzml".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (added != null)
//            paramsFile += added;
//        //      if (host != null || !"null".equals(host)) {
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//        XTandemMain.addPreLoadOpener(opener);
//        // note we are reading from hdsf
//        safeWrite(context, "Output File", paramsFile);
//        HDFSAccessor accesor = opener.getAccesor();
//        // use a counter to see what we do
//        context.getCounter("outputfile",paramsFile).increment(1);
//        Path path = new Path(paramsFile);
//        OutputStream os = accesor.openFileForWrite(path);
//
//        context.getCounter("outputfile","total_files").increment(1);
//        return os;
//    }

}
