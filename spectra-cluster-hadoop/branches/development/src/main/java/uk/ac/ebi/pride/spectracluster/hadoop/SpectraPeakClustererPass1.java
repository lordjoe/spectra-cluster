package uk.ac.ebi.pride.spectracluster.hadoop;


import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.hadoop.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraPeakClustererPass1
 * This uses a key based in charge,peakmz,PrecursorMZ
 * inout is MGF text
 */
public class SpectraPeakClustererPass1 extends ConfiguredJobRunner implements IJobRunner {

    @SuppressWarnings("UnusedDeclaration")
    public static final int MAX_TEST_PROTEINS = 2000;
    public static final int REPORT_INTERVAL_PROTEINS = 10000;


    public static class MajorPeakMapper extends AbstractTandemMapper<Writable> {
        private int m_Proteins;


        @Override
        public void map(Writable key, Text value, Context context
        ) throws IOException, InterruptedException {

            String label = key.toString();
            String text = value.toString();
            if (label == null || text == null)
                return;
            if (label.length() == 0 || text.length() == 0)
                return;
            // ready to read test as one MGF
            LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
            final IPeptideSpectrumMatch match = ParserUtilities.readMGFScan(rdr);
            int precursorCharge = match.getPrecursorCharge();
            double precursorMZ = match.getPrecursorMz();

            Text onlyKey = getOnlyKey();
            Text onlyValue = getOnlyValue();
            for (int peakMz : match.asMajorPeakMZs()) {
                ChargePeakMZKey mzKey = new ChargePeakMZKey(precursorCharge, (double) peakMz, precursorMZ);
                final String keyStr = mzKey.toString();
                onlyKey.set(keyStr);
                onlyValue.set(text);   // send on the MGF
                context.write(onlyKey, onlyValue);
            }

        }


    }


    /**
     * Form clusters from
     */
    public static class MajorPeakReducer extends AbstractTandemReducer {

        private int majorPeak;
        private int currentCharge;

        /**
         * just like reduce but some special handlers may run first or instead of
         *
         * @param key
         * @param values
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduceNormal(Text key, Iterable<Text> values,
                                 Context context) throws IOException, InterruptedException {

            String keyStr = key.toString();
            ChargePeakMZKey mzKey = new ChargePeakMZKey(keyStr);




            for (Text val : values) {
                String valStr = val.toString();


                LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
                final IPeptideSpectrumMatch match = ParserUtilities.readMGFScan(rdr);

                if (true)
                    throw new UnsupportedOperationException("Fix This"); // ToDo


            }
        }


        /**
         * Called once at the end of the task.
         */
        @Override
        protected void cleanup(final Context context) throws IOException, InterruptedException {
            writeParseParameters(context);
            super.cleanup(context);

        }

        /**
         * remember we built the database
         *
         * @param context
         * @throws java.io.IOException
         */

        protected void writeParseParameters(final Context context) throws IOException {
            Configuration cfg = context.getConfiguration();
            HadoopTandemMain application = getApplication();
            TaskAttemptID tid = context.getTaskAttemptID();
            //noinspection UnusedDeclaration
            String taskStr = tid.getTaskID().toString();
            String paramsFile = application.getDatabaseName() + ".params";
            Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);

            FileSystem fs = FileSystem.get(cfg);

            if (!fs.exists(dd)) {
                try {
                    FastaHadoopLoader ldr = new FastaHadoopLoader(application);
                    String x = ldr.asXMLString();
                    FSDataOutputStream fsout = fs.create(dd);
                    PrintWriter out = new PrintWriter(fsout);
                    out.println(x);
                    out.close();
                }
                catch (IOException e) {
                    try {
                        fs.delete(dd, false);
                    }
                    catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                    throw new RuntimeException(e);
                }

            }

        }

        protected PrintWriter getOutputWriter(final Context context, final int pMass) throws IOException {
            Configuration cfg = context.getConfiguration();
            Path outPath = XTandemHadoopUtilities.buildPathFromMass(pMass, getApplication());
            FileSystem fs = FileSystem.get(cfg);
            FSDataOutputStream fsout = fs.create(outPath);
            return new PrintWriter(fsout);
        }
    }

    public int runJob(Configuration conf, final String[] args) throws Exception {
        try {
            if (args.length == 0)
                throw new IllegalStateException("needs a file name");
            String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

            // GenericOptionsParser stops after the first non-argument
            otherArgs = XTandemHadoopUtilities.handleGenericInputs(conf, otherArgs);


//        if (otherArgs.length != 2) {
//            System.err.println("Usage: wordcount <in> <out>");
//            System.exit(2);
//        }
            Job job = new Job(conf, "Spectrum Peak Clusterer");
            setJob(job);

            conf = job.getConfiguration(); // NOTE JOB Copies the configuraton

            // make default settings
            XTandemHadoopUtilities.setDefaultConfigurationArguments(conf);

            // sincs reducers are writing to hdfs we do NOT want speculative execution
            conf.set("mapred.reduce.tasks.speculative.execution", "false");


            String params = conf.get(XTandemHadoopUtilities.PARAMS_KEY);
            if (params == null)
                conf.set(XTandemHadoopUtilities.PARAMS_KEY, otherArgs[0]);
            job.setJarByClass(SpectraPeakClustererPass1.class);

            if (JXTandemLauncher.isSequenceFilesUsed()) {
                job.setInputFormatClass(SequenceFileInputFormat.class);
            }
            else {
                job.setInputFormatClass(TextInputFormat.class);
            }

            job.setOutputFormatClass(TextOutputFormat.class);

            job.setMapperClass(MajorPeakMapper.class);
            job.setReducerClass(MajorPeakReducer.class);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);


            // Do not set reduce tasks - ue whatever cores are available
            // this does not work just set a number for now
            XTandemHadoopUtilities.setRecommendedMaxReducers(job);


            if (otherArgs.length > 1) {
                String otherArg = otherArgs[0];
                XTandemHadoopUtilities.setInputPath(job, otherArg);
                System.err.println("Input path mass finder " + otherArg);
            }

            // you must pass the output directory as the last argument
            String athString = otherArgs[otherArgs.length - 1];
            //           File out = new File(athString);
//        if (out.exists()) {
//            FileUtilities.expungeDirectory(out);
//            out.delete();
//        }

            Path outputDir = new Path(athString);

            FileSystem fileSystem = outputDir.getFileSystem(conf);
            XTandemHadoopUtilities.expunge(outputDir, fileSystem);    // make sure thia does not exist
            FileOutputFormat.setOutputPath(job, outputDir);
            System.err.println("Output path mass finder " + outputDir);


            boolean ans = job.waitForCompletion(true);
            int ret = ans ? 0 : 1;
            if (ans)
                XTandemHadoopUtilities.saveCounters(fileSystem, XTandemHadoopUtilities.buildCounterFileName(this, conf), job);
            else
                throw new IllegalStateException("Job Failed");


            //    if (numberMapped != numberReduced)
            //       throw new IllegalStateException("problem"); // ToDo change

            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Execute the command with the given arguments.
     *
     * @param args command specific arguments.
     * @return exit code.
     * @throws Exception
     */
    @Override
    public int run(final String[] args) throws Exception {
        Configuration conf = getConf();
        if (conf == null)
            conf = HDFSAccessor.getSharedConfiguration();
        return runJob(conf, args);
    }

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new SpectraPeakClustererPass1(), args);
    }
}