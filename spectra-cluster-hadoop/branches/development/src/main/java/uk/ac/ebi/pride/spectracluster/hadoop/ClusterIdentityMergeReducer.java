package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ClusterIdentityMergeReducer
 * Reducer merges multiple copies of stable and semistable clusters
 * designed to run after Stable Cluster mapping
 * User: Steve
 * Date: 8/14/13
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusterIdentityMergeReducer extends AbstractParameterizedReducer {


    @Override
    protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        Defaults.configureAnalysisParameters(getApplication());

    }

    @Override
    protected void reduceNormal(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String id = key.toString();
        ISpectralCluster mainCluster = null;
        for (Text value : values) {
            String text = value.toString();
            LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
            ISpectralCluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
            if (clusters.length == 0)
                continue;
            if (clusters.length > 1)
                throw new IllegalStateException("we should never get more than one");
            ISpectralCluster thisCluster = clusters[0];
            if (mainCluster == null) {
                mainCluster = thisCluster;
            } else {
                mainCluster.addSpectra(thisCluster.getClusteredSpectra());
            }
        }

        //noinspection ConstantConditions
        if(!mainCluster.isSemiStable()) {
            throw new IllegalStateException("ClusterIdentity ust operate on stable and semistable clusters");
        }
        StringBuilder sb = new StringBuilder();
        mainCluster.append(sb);
        writeKeyValue(id, sb.toString(), context);
    }


}
