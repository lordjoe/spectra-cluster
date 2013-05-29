package com.lordjoe.hadoopsimulator;

import java.io.*;
import java.util.*;

import com.lordjoe.hadoop.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import uk.ac.ebi.pride.spectracluster.hadoop.*;

/**
 * com.lordjoe.hadoopsimulator.HadoopWrapper
 * Wrapper class to allow an ITextMapper and ITextReducer to run on the cluster
 *
 * @author Steve Lewis
 * @date 23/05/13
 */
public class HadoopWrapper extends Configured implements Tool {

    private static ITextMapper m_Mapper;
    private static ITextReducer m_Reducer;

    private final String m_Name;
    private Class<? extends InputFormat> m_InputFormatClass = LineTextInputFormat.class;
    private final Properties m_AddedProps;

    public HadoopWrapper(String name, ITextMapper mapper, ITextReducer reducer) {
        this(name, mapper, reducer, new Properties());
    }

    public HadoopWrapper(String name, ITextMapper mapper, ITextReducer reducer, Properties p) {
        m_Name = name;
        m_Mapper = mapper;
        m_Reducer = reducer;
        m_AddedProps = p;
    }

    public String getName() {
        return m_Name;
    }

    public Class<? extends InputFormat> getInputFormatClass() {
        return m_InputFormatClass;
    }

    public void setInputFormatClass(Class<? extends InputFormat> inputFormatClass) {
        m_InputFormatClass = inputFormatClass;
    }

    public static ITextMapper getMapper() {
        return m_Mapper;
    }

    public static ITextReducer getReducer() {
        return m_Reducer;
    }

    /**
     * copy the properties from the config to a Properties to be used by non-Hadoop aware code
     *
     * @param context
     * @param conFigProps
     */
    public static void fillInProperties(TaskAttemptContext context, Properties conFigProps) {
        Configuration conf = context.getConfiguration();
        Iterator it = conf.iterator();
        while (it.hasNext()) {

            Map.Entry next = (Map.Entry) it.next();
            String key = next.getKey().toString();
            String value = next.getValue().toString();
            conFigProps.setProperty(key, value);
        }
    }


    public static class WrappedMapper extends Mapper<Text, Text, Text, Text> {

        private final Text keyText = new Text();
        private final Text valueText = new Text();
        private final Properties conFigProps = new Properties();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            fillInProperties(context, conFigProps); // read properties out of the configuration

        }

        @Override
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            String keyStr = key.toString();
            String valueStr = value.toString();
            ITextMapper mapper = getMapper();
            TextKeyValue[] map = mapper.map(keyStr, valueStr, conFigProps);
            for (int i = 0; i < map.length; i++) {
                TextKeyValue tk = map[i];
                keyText.set(tk.getKey());
                valueText.set(tk.getValue());
                context.write(keyText, valueText);
            }
        }
    }


    public static class WrappedReducer extends Reducer<Text, Text, Text, Text> {
        private final Text keyText = new Text();
        private final Text valueText = new Text();
        private final Properties conFigProps = new Properties();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            fillInProperties(context, conFigProps);
        }


        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            String keyStr = key.toString();
            List<String> holder = new ArrayList<String>();
            for (Text val : values) {
                holder.add(val.toString());
            }
            ITextReducer reducer = getReducer();
            TextKeyValue[] reduce = reducer.reduce(keyStr, holder, conFigProps);
            for (int i = 0; i < reduce.length; i++) {
                TextKeyValue tk = reduce[i];
                keyText.set(tk.getKey());
                valueText.set(tk.getValue());
                context.write(keyText, valueText);
            }
        }
    }

    /**
     * kill a directory and all contents
     *
     * @param src
     * @param fs
     * @return
     */
    public static boolean expunge(Path src, FileSystem fs) {


        try {
            if (!fs.exists(src))
                return true;
            // break these out
            if (fs.getFileStatus(src).isDir()) {
                boolean doneOK = fs.delete(src, true);
                doneOK = !fs.exists(src);
                return doneOK;
            }
            if (fs.isFile(src)) {
                boolean doneOK = fs.delete(src, false);
                return doneOK;
            }
            throw new IllegalStateException("should be file of directory if it exists");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public int runJob(Configuration conf, String[] args) throws Exception {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.err.println(arg);
        }
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();


        Job job = new Job(conf, getName());
        conf = job.getConfiguration(); // NOTE JOB Copies the configuraton

        job.setJarByClass(HadoopWrapper.class);
        job.setMapperClass(WrappedMapper.class);
        job.setReducerClass(WrappedReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(LineTextInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);


        if (otherArgs.length > 1) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        }

        // you must pass the output directory as the last argument
        String athString = otherArgs[otherArgs.length - 1];
        File out = new File(athString);
//        if (out.exists()) {
//            FileUtilities.expungeDirectory(out);
//            out.delete();
//        }

        Path outputDir = new Path(athString);

        FileSystem fileSystem = outputDir.getFileSystem(conf);
        expunge(outputDir, fileSystem);    // make sure thia does not exist
        FileOutputFormat.setOutputPath(job, outputDir);


        boolean ans = job.waitForCompletion(true);
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
            conf = new Configuration();
        // copy added properties into conf importantly this might set a file system
        for (String key : m_AddedProps.stringPropertyNames()) {
            conf.set(key, m_AddedProps.getProperty(key));
        }
        return runJob(conf, args);
    }


    public static final String HADOOP_MACHINE = "hadoop-master-03.ebi.ac.uk";
    public static final int HADOOP_PORT = 54310;

    private static void usage() {
        System.out.println("usage inputfile1 <inputfile2> <inputfile3> ... outputdirectory");
    }

    /**
     * Sample of use
     * args might be /user/slewis/hadoop/test/books/pg135.txt /user/slewis/hadoop/test/output1
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        String title = "Word Count";
        ITextMapper mapper = new WordCount.WordMapper();
        ITextReducer reducer = new WordCount.WordCountReducer();

        Properties added = new Properties();
        // This line runs the job on the cluster - omitting it runs the job locallty
        added.setProperty("fs.default.name", "hdfs://" + HADOOP_MACHINE + ":" + HADOOP_PORT);

        HadoopWrapper wrapper = new HadoopWrapper(title, mapper, reducer, added);

        wrapper.run(args);
    }
}
