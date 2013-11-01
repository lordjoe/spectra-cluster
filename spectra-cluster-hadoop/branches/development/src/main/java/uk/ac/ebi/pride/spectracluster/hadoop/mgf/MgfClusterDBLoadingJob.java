package uk.ac.ebi.pride.spectracluster.hadoop.mgf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.systemsbiology.hadoop.ConfiguredJobRunner;
import org.systemsbiology.hadoop.HadoopUtilities;
import org.systemsbiology.hadoop.IJobRunner;
import org.systemsbiology.hadoop.MGFInputFormat;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterDBLoadingJob extends ConfiguredJobRunner implements IJobRunner {

    public static final String DATABASE_NAME_PROPERTY = "database.name";
    public static final String JOB_NAME = "MGF cluster db loader";

    @Override
    public int runJob(Configuration conf, String[] args) throws Exception {
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        conf.set(DATABASE_NAME_PROPERTY, otherArgs[2]);

        Job job = new Job(conf, JOB_NAME);
        job.setJarByClass(getClass());

        job.setInputFormatClass(MGFInputFormat.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        Path outputDir = new Path(otherArgs[1]);

        FileOutputFormat.setOutputPath(job, outputDir);
        HadoopUtilities.expunge(outputDir, outputDir.getFileSystem(conf));

        job.setMapperClass(MgfClusterDBLoadingMapper.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(NullWritable.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.printf("Usage: %s [input path] [output path] [database name]", getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Configuration conf = getConf();

        return runJob(conf, args);
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new MgfClusterDBLoadingJob(), args);
        System.exit(exitCode);
    }
}
