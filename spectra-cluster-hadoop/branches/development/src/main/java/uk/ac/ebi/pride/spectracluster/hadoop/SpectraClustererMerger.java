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
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraPeakClustererPass1
 * This uses a key based in charge,peakmz,PrecursorMZ
 * inout is MGF text
 */
public class SpectraClustererMerger extends ConfiguredJobRunner implements IJobRunner {

    @SuppressWarnings("UnusedDeclaration")
    public static final int MAX_TEST_PROTEINS = 2000;


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
            Job job = new Job(conf, "Spectrum Peak Merger");
            setJob(job);

            conf = job.getConfiguration(); // NOTE JOB Copies the configuraton

            // make default settings
            XTandemHadoopUtilities.setDefaultConfigurationArguments(conf);

            // sincs reducers are writing to hdfs we do NOT want speculative execution
            // conf.set("mapred.reduce.tasks.speculative.execution", "false");


            String params = conf.get(XTandemHadoopUtilities.PARAMS_KEY);
            if (params == null)
                conf.set(XTandemHadoopUtilities.PARAMS_KEY, otherArgs[0]);
            job.setJarByClass(SpectraClustererMerger.class);

            job.setInputFormatClass(SequenceFileInputFormat.class);

            // sequence files are faster but harder to debug
            job.setOutputFormatClass(SequenceFileOutputFormat.class);

            //job.setMapperClass(SpecialChargeMZNarrowBinMapper.class);    //  ToDo put back
            job.setMapperClass(ChargeMZNarrowBinMapper.class);
            job.setReducerClass(SpectrumMergeReducer.class);  //  ToDo put back
            //  job.setReducerClass(ClusterConsolidator.FileWriteReducer.class);
            job.setPartitionerClass(ChargeBinPartitioner.class);    //  ToDo put back


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

            // Todo this whole section if for debugging
            Path parentPath = outputDir.getParent();
            Path outPath = new Path(parentPath, "ConsolidatedOutputDebug");    // todo take out debug
            fileSystem.mkdirs(outPath);
            conf.set(ClusterConsolidator.CONSOLIDATOR_PATH_PROPERTY, outPath.toString());
            // Todo rnd whole section if for debugging


            boolean ans = job.waitForCompletion(true);
            int ret = ans ? 0 : 1;
            if (ans)
                XTandemHadoopUtilities.saveCounters(fileSystem, XTandemHadoopUtilities.buildCounterFileName(this, conf), job);
            else
                throw new IllegalStateException("Job Failed");


            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } catch (ClassNotFoundException e) {
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
        ToolRunner.run(new SpectraClustererMerger(), args);
    }
}