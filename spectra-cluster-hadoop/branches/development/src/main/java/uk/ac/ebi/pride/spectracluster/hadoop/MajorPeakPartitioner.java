package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakPartitioner
 * A partitioner which guarentees that given a key representing a ChargePeakMZ that
 * all value with a given charge and peak go to the same reducer
 * User: Steve
 * Date: 8/14/13
 */
public class MajorPeakPartitioner  extends Partitioner<Text, Text> {


    @Override
    public int getPartition(final Text pText, final Text value, final int numberReducers) {
        String key = pText.toString();
        ChargePeakMZKey realKey = new ChargePeakMZKey(key);

        int hash = realKey.getCharge() * 10000;
        hash += realKey.getPeakMZ();
        return hash % numberReducers;
     }
}
