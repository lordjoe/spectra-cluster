package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ClusterIdentityMapper
 * Map only stable and semi-stable clusters
 * User: Steve
 * Date: 8/14/13
 */
public class ClusterIdentityMapper extends AbstractParameterizedMapper<Text> {


    @Override
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String label = key.toString();
        String text = value.toString();
        if (label == null || text == null)
            return;
        if (label.length() == 0 || text.length() == 0)
            return;


        LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
        IPeptideSpectrumCluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
        switch (clusters.length) {
            case 0:
                return;
            case 1:
                handleCluster(clusters[0],text,context);
                return;
            default:
                throw new UnsupportedOperationException("Fix This"); // ToDo
        }
    }

    /**
     * write stable and semiStable clusters
     * @param cluster
     * @param text
     * @param context
     */
    protected void handleCluster(IPeptideSpectrumCluster cluster,String text, Context context) {
        if(cluster.isStable())   {
            String id = ClusterUtilities.getStableClusterId();
            writeKeyValue(id,text,context);
            return;
        }
        if(cluster.isSemiStable())   {
             String id = ClusterUtilities.getSemiStableClusterId();
             writeKeyValue(id,text,context);
             return;
         }
        // ignore unstable clusters
     }


}
