package uk.ac.ebi.pride.spectracluster.hadoop;


import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ClusterConsolidator
 * This sends ALL output to a single reducer to make a CGF file -
 * primarily for use in early stage debugging
 */
public class ClusterConsolidator extends ConfiguredJobRunner implements IJobRunner {

    private static String outputFileName = "ConsolidatorOutput.cgf";

    /**
     * retucer to w
     */
    public static class FileWriteReducer extends Reducer<Text, Text, Text, Text> {

        private PrintWriter outputWriter;

        @Override
        protected void setup(final Context context) throws IOException, InterruptedException {
            super.setup(context);
            outputWriter = new PrintWriter(new FileWriter(outputFileName));
        }

        @Override
        protected void cleanup(final Context context) throws IOException, InterruptedException {
            super.cleanup(context);
            if(outputWriter != null)
                outputWriter.close();
        }

        /**
         * write the values to a real file -
         *   NOT useful in a cluster but very helpful in debugging
         * @param key
         * @param values
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        protected void reduce(final Text key, final Iterable<Text> values, final Context context) throws IOException, InterruptedException {
            //noinspection LoopStatementThatDoesntLoop
            for (Text val : values) {
                String valStr = val.toString();
                outputWriter.println(valStr);

            }

        }
    }

    protected static void usage() {
        System.out.println("Usage <input file or directory> <output directory>");
    }


    public int runJob(Configuration conf, final String[] args) throws Exception {
        try {
            if (args.length < 2)
                throw new IllegalStateException("needs a file name and an output directory");
            String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

            // GenericOptionsParser stops after the first non-argument
            otherArgs = XTandemHadoopUtilities.handleGenericInputs(conf, otherArgs);


//        if (otherArgs.length != 2) {
//            System.err.println("Usage: wordcount <in> <out>");
//            System.exit(2);
//        }
            Job job = new Job(conf, "Cluster Consolidator");
            setJob(job);

            conf = job.getConfiguration(); // NOTE JOB Copies the configuraton

            // make default settings
            XTandemHadoopUtilities.setDefaultConfigurationArguments(conf);

            // sincs reducers are writing to hdfs we do NOT want speculative execution
            // conf.set("mapred.reduce.tasks.speculative.execution", "false");


            String params = conf.get(XTandemHadoopUtilities.PARAMS_KEY);
            if (params == null)
                conf.set(XTandemHadoopUtilities.PARAMS_KEY, otherArgs[0]);
            job.setJarByClass(ClusterConsolidator.class);

            job.setInputFormatClass(SequenceFileInputFormat.class);

              // there is no output
            job.setOutputFormatClass(TextOutputFormat.class);

            job.setMapperClass(TextIdentityMapper.class);
            job.setReducerClass(FileWriteReducer.class);

            job.setNumReduceTasks(1);  // this is important

            // We always do this
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
        if (args.length < 2) {
            usage();
            return;
        }
        ToolRunner.run(new ClusterConsolidator(), args);
    }
}