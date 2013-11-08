package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakPartitioner
 * A partitioner which guarentees that given a key representing a ChargeBinMZKey that
 * all value with a given charge and bin go to the same reducer
 * User: Steve
 * Date: 8/14/13
 */
public class StableChargeBinKeyPartitioner extends Partitioner<Text, Text> {


    @Override
    public int getPartition(final Text pText, final Text value, final int numberReducers) {
        String key = pText.toString();
        // send all special keys to reducer 0
        if(AbstractParameterizedReducer.isKeySpecial(key))
            return 0;
        StableChargeBinMZKey realKey;
        if(key.contains(StableChargeBinMZKey.SORT_PREFIX))
            realKey = new StableChargeBinMZKey(key);
        else {
            realKey = new UnStableChargeBinMZKey(key);
        }

        int hash = realKey.getPartitionHash();
        return hash % numberReducers;
     }
}
