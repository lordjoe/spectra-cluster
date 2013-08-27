package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakPartitioner
 * A partitioner which guarentees that given a key representing a ChargeBinMZKey that
 * all value with a given charge and bin go to the same reducer
 * User: Steve
 * Date: 8/14/13
 */
public class ChargeBinPartitioner extends Partitioner<Text, Text> {


    @Override
    public int getPartition(final Text pText, final Text value, final int numberReducers) {
        String key = pText.toString();
        ChargeBinMZKey realKey = new ChargeBinMZKey(key);

        int hash = realKey.getPartitionHash();
        return hash % numberReducers;
     }
}
