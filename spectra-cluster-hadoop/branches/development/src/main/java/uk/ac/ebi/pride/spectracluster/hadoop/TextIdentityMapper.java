package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;

import java.io.*;

/**
* uk.ac.ebi.pride.spectracluster.hadoop.TextIdentityMapper
 * Not sure whay I cannot find in 0.2 but uses the key and value unchanged
* User: Steve
* Date: 8/14/13
*/
public class TextIdentityMapper extends AbstractParameterizedMapper<Text> {


    @Override
    public void map(Text key, Text value, Mapper.Context context
    ) throws IOException, InterruptedException {
        //noinspection unchecked
        context.write(key, value);

    }


}
