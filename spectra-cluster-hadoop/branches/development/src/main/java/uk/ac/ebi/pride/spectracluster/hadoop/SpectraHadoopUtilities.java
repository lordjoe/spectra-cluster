package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraHadoopUtilities
 * User: Steve
 * Date: 8/13/13
 * static general purpose routines for handling hadoopy things
 */
public class SpectraHadoopUtilities {

    public static final int MIMIMUM_CLUSTER_LENGTH = 5 * "BEGIN IONS\n".length();

    public static final double NARRROW_BIN_WIDTH = 0.005; // 0.3;
    public static final double NARRROW_BIN_OVERLAP = 0.002; // 0.1;


    public static final IWideBinner NARROW_MZ_BINNER = new SizedWideBinner(
            IPeak.HIGHEST_USABLE_MZ,
            NARRROW_BIN_WIDTH,
            IPeak.LOWEST_USABLE_MZ,
             NARRROW_BIN_OVERLAP);


    public static final double WIDE_BIN_WIDTH = 1.0;
    public static final double WIDE_BIN_OVERLAP = 0.3;

    @SuppressWarnings("UnusedDeclaration")
    public static final IWideBinner WIDE_MZ_BINNER = new SizedWideBinner(
            IPeak.HIGHEST_USABLE_MZ,
            WIDE_BIN_WIDTH,
            IPeak.LOWEST_USABLE_MZ,
                WIDE_BIN_OVERLAP);

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

    /**
     * track how balanced is partitioning
     *
     * @param context !null context
     * @param hash    retucer assuming  ClusterLauncher.DEFAULT_NUMBER_REDUCERS is right
     */
    public static void incrementPartitionCounter(Mapper<? extends Writable, Text, Text, Text>.Context context,String prefix, int hash) {
        String counterName = prefix + String.format("%04d", hash).trim();
        context.getCounter("Partition", counterName).increment(1);
    }

    /**
     * track how balanced is partitioning
     *
     * @param context !null context
     * @param mzKey   !null key
     */
    public static void incrementPartitionCounter(Mapper<? extends Writable, Text, Text, Text>.Context context, ChargePeakMZKey mzKey) {
        int hash = mzKey.getPartitionHash() % ClusterLauncher.DEFAULT_NUMBER_REDUCERS;
        incrementPartitionCounter(context,"Peak", hash);
    }

    /**
      * track how balanced is partitioning
      *
      * @param context !null context
      * @param mzKey   !null key
      */
    public static void incrementPartitionCounter(Mapper<? extends Writable, Text, Text, Text>.Context context, ChargeBinMZKey mzKey) {
        int hash = mzKey.getPartitionHash() % ClusterLauncher.DEFAULT_NUMBER_REDUCERS;
        incrementPartitionCounter(context,"Bin", hash);
    }

    /**
     * build a reader for  a local sequence file
     * @param file !null existing readabl;e non-directory file
     * @param conf  !null Configuration
     * @return  !null reader
     */
    public static SequenceFile.Reader buildSequenceFileReader(File file,Configuration conf)
    {
        String fileName = file.getPath();
        Path filePath = new Path(fileName);
        return buildSequenceFileReader(conf, filePath);

    }

    /**
     * build a reader for  a local sequence file
     * @param conf  !null Configuration
     * @param filePath !null existing path
     * @return  !null reader
      */
    public static  SequenceFile.Reader buildSequenceFileReader(Configuration conf, Path filePath) {
        try {
             FileSystem fs = FileSystem.get(conf);
             return new SequenceFile.Reader(fs, filePath, conf);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
    }

    @SuppressWarnings("UnusedDeclaration")
        protected static final String ATTEMPT = "attempt";
        protected static final char SEPARATOR = '_';

        @SuppressWarnings("UnusedDeclaration")
        public static PrintWriter buildReducerWriter(Reducer.Context ctxt, String baseName) {
            try {
                FileSystem fs = FileSystem.get(ctxt.getConfiguration());
                Path path = getAttempPath(ctxt, fs, baseName);
                final FSDataOutputStream dsOut = fs.create(path);
                //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                PrintWriter out = new PrintWriter(new OutputStreamWriter(dsOut));
                return out;

            }
            catch (IOException e) {
                throw new RuntimeException(e);

            }

        }

        @SuppressWarnings("UnusedDeclaration")
        public static Path getAttempPath(final Reducer.Context ctxt, final FileSystem pFs, String baseName) {
            final TaskAttemptID taskAttemptID = ctxt.getTaskAttemptID();
            String str = taskAttemptID.toString();
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment,MismatchedReadAndWriteOfArray
            String[] parts = str.split(Character.toString(SEPARATOR));


            String fileName = baseName + parts[4] +  ".tmp";
            Path workingDirectory = pFs.getWorkingDirectory();
            return new Path(workingDirectory, fileName);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static void renameAttemptFile(Reducer.Context ctxt, String baseName, String outName) {
            try {
                FileSystem fs = FileSystem.get(ctxt.getConfiguration());
                Path pathstartPath = getAttempPath(ctxt, fs, baseName);
                Path outpath = new Path(fs.getWorkingDirectory(), outName);

                fs.rename(pathstartPath, outpath);

            }
            catch (IOException e) {
                throw new RuntimeException(e);

            }

        }
}