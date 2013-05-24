package uk.ac.ebi.pride.spectracluster.hadoop;


import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.LineTextInputFormat
 * Jusrt like LineInputFormat except the key returned is Text so
 * we cab be compatable with all other TextMappers and TextReducers
 *
 * @author Steve Lewis
 * @date 24/05/13
 */
public class LineTextInputFormat extends FileInputFormat<Text, Text> {

    public LineTextInputFormat() {

    }


    @Override
    public RecordReader<Text, Text> createRecordReader(InputSplit split,
                                                       TaskAttemptContext context) {
        return new LineTextReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return true;
    }

    /**
       * single value with the name as a key
     * Value is the entire file
     * Key is the file name
     */
    public class LineTextReader extends RecordReader<Text, Text> {

        private CompressionCodecFactory compressionCodecs = null;
        private long m_Start;
        private long m_End;
        private long current;
        private LineReader m_Input;
        FSDataInputStream m_RealFile;
        private Text key = null;
        private Text value = null;
        private Text buffer = new Text();

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            m_Start = split.getStart();
            m_End = m_Start + split.getLength();
            Path path = split.getPath();
            final Path file = path;
            compressionCodecs = new CompressionCodecFactory(job);
            boolean skipFirstLine = false;
            final CompressionCodec codec = compressionCodecs.getCodec(file);

            // open the file and seek to the m_Start of the split
            FileSystem fs = file.getFileSystem(job);
            // open the file and seek to the m_Start of the split
            m_RealFile = fs.open(path);
            if (codec != null) {
                CompressionInputStream inputStream = codec.createInputStream(m_RealFile);
                m_Input = new LineReader(inputStream);
                m_End = Long.MAX_VALUE;
            } else {
                if (m_Start != 0) {
                    skipFirstLine = true;
                    --m_Start;
                    m_RealFile.seek(m_Start);
                }
                m_Input = new LineReader(m_RealFile);
            }
            // not at the beginning so go to first line
            if (skipFirstLine) {  // skip first line and re-establish "m_Start".
                m_Start += m_Input.readLine(buffer);
            }

            current = m_Start;
            if (key == null) {
                key = new Text();
            }
            key.set(path.getName());
            if (value == null) {
                value = new Text();
            }

            current = 0;
        }

        /**
         * look for a <scan tag then read until it closes
         *
         * @return true if there is data
         * @throws java.io.IOException
         */
        public boolean nextKeyValue() throws IOException {
            int newSize = 0;
            while (current < m_Start) {
                newSize = m_Input.readLine(buffer);
                // we are done
                if (newSize == 0) {
                    key = null;
                    value = null;
                    return false;
                }
                current = m_RealFile.getPos();
            }
            newSize = m_Input.readLine(buffer);

            if (newSize == 0) {
                key = null;
                value = null;
                return false;

            }
            value.set(buffer.toString());
            return true;
        }

        @Override
        public Text getCurrentKey() {
            return key;
        }

        @Override
        public Text getCurrentValue() {
            return value;
        }


        /**
         * Get the progress within the split
         */
        public float getProgress() {
            long totalBytes = m_End - m_Start;
            long totalHandled = current - m_Start;
            return ((float) totalHandled) / totalBytes;
        }


        public synchronized void close() throws IOException {
            if (m_Input != null) {
                m_Input.close();
            }
        }
    }
}
