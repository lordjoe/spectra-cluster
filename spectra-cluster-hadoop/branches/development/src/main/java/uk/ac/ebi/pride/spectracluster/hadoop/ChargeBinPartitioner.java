package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargeBinPartitioner
 * A partitioner which guarentees that given a key representing a ChargeBinMZKey that
 * all value with a given charge and bin go to the same reducer
 * User: Steve
 * Date: 8/14/13
 */
public class ChargeBinPartitioner extends   AbstractBinnedAPrioriPartitioner {


    @Override
    public int getPartition(final Text pText, final Text value, final int numberReducers) {


        String key = pText.toString();
        // send all special keys to reducer 0
        if(AbstractParameterizedReducer.isKeySpecial(key))
            return 0;
        ChargeBinMZKey realKey = new ChargeBinMZKey(key);

        if(getPrepartitioning(numberReducers) != null)   {
            int preBin = getPrepartitioning(numberReducers).getBin(realKey.getPrecursorMZ());
            if(preBin > -1)
                return preBin;
        }
        int hash = realKey.getPartitionHash();
        return hash % numberReducers;
     }
}
