package uk.ac.ebi.pride.spectracluster.hadoop;


import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
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


    /**
     * maps ueing a ChargePeakMZKey key - use MajorPeakPartitioner to see all instances with a major peak
     * go to the same reducer - NOTE every spectrum is emitted 3 times
     */
    public static class MajorPeakMapper extends AbstractParameterizedMapper<Writable> {



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
            if(match == null )    {
                System.err.println("No Match fount in text\n" + text);
                return;
            }
            int precursorCharge = match.getPrecursorCharge();
            double precursorMZ = match.getPrecursorMz();

            Text onlyKey = getOnlyKey();
            Text onlyValue = getOnlyValue();
            for (int peakMz : match.asMajorPeakMZs()) {
                ChargePeakMZKey mzKey = new ChargePeakMZKey(precursorCharge, peakMz, precursorMZ);

           //     SpectraHadoopUtilities.incrementPartitionCounter(context,mzKey);   // debug partitioning

                final String keyStr = mzKey.toString();
                onlyKey.set(keyStr);
                onlyValue.set(text);   // send on the MGF
                context.write(onlyKey, onlyValue);
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
            Job job = new Job(conf, "Spectrum Peak Clusterer");
            setJob(job);

            conf = job.getConfiguration(); // NOTE JOB Copies the configuraton

            // make default settings
            XTandemHadoopUtilities.setDefaultConfigurationArguments(conf);

            // sincs reducers are writing to hdfs we do NOT want speculative execution
            // conf.set("mapred.reduce.tasks.speculative.execution", "false");


            String params = conf.get(XTandemHadoopUtilities.PARAMS_KEY);
            if (params == null)
                conf.set(XTandemHadoopUtilities.PARAMS_KEY, otherArgs[0]);
            job.setJarByClass(SpectraPeakClustererPass1.class);

            job.setInputFormatClass(MGFInputFormat.class);

            // sequence files are faster but harder to debug
            job.setOutputFormatClass(SequenceFileOutputFormat.class);

            job.setMapperClass(MajorPeakMapper.class);
            job.setReducerClass(MajorPeakReducer.class);
            // make sure all instances of a peak/charge hit the same reducer
            job.setPartitionerClass(MajorPeakPartitioner.class);

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
        ToolRunner.run(new SpectraPeakClustererPass1(), args);
    }
}