package org.systemsbiology.xtandem.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.CapitolWordCountRunner
 * Last job in hadoop cascade - this writes an output file
 * <p/>
 * as stand alone from largeSample use the followiing
 * JXTandemOutput2 JXTandemOutput3  -D org.systemsbiology.xtandem.params=tandem.params -D org.systemsbiology.xtandem.hdfs.host=Glados -D org.systemsbiology.xtandem.hdfs.basepath=/user/howdah/JXTandem/data/largeSample
 * User: steven
 * Date: 3/7/11
 */
public class CapitolWordCountRunner extends ConfiguredJobRunner implements IJobRunner {
    public static final String TEST_PROPERTY = "org.systemsbiology.status";

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        /**
         * Called once at the beginning of the task.
         */
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();

            String isBar = conf.get(TEST_PROPERTY);
            Counter counter = context.getCounter("test", "bar");
            if ("foobar".equals(isBar))
                counter.increment(1);
        }

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                String s = itr.nextToken().toUpperCase();
                s = dropNonLetters(s);
                if (s.length() > 0) {
                    word.set(s);
                    context.write(word, one);
                }
            }
        }
    }

    public static String dropNonLetters(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c))
                sb.append(c);
        }

        return sb.toString();
    }

    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(new Text(key.toString()), result);
        }
    }




    public int runJob(Configuration conf, final String[] args) throws Exception {
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
//        if (otherArgs.length != 2) {
//            System.err.println("Usage: wordcount <in> <out>");
//            System.exit(2);
//        }



        Job job = new Job(conf, "word count");
        conf = job.getConfiguration(); // NOTE JOB Copies the configuraton
        job.setJarByClass(CapitolWordCountRunner.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);


        job.setInputFormatClass(FakeWordInputFormat.class);

        // added Slewis
        job.setNumReduceTasks(2); // cheaper on amazon HadoopUtilities.DEFAULT_REDUCE_TASKS);
        //    job.setPartitionerClass(MyPartitioner.class);

        if (otherArgs.length > 1) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        }

        // you must pass the output directory as the last argument
        String athString = otherArgs[otherArgs.length - 1];
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        File out = new File(athString);
//        if (out.exists()) {
//            FileUtilities.expungeDirectory(out);
//            out.delete();
//        }

        Path outputDir = new Path(athString);

        FileSystem fileSystem = outputDir.getFileSystem(conf);
        HadoopUtilities.expunge(outputDir, fileSystem);    // make sure thia does not exist
        FileOutputFormat.setOutputPath(job, outputDir);

        boolean ans = job.waitForCompletion(true);
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        int ret = ans ? 0 : 1;
        return ret;
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
         ToolRunner.run(new CapitolWordCountRunner(), args);
    }
}
