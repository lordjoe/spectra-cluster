package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.io.CGFClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ClusterIdentityMergeReducer
 * Reducer merges multiple copies of stable and semistable clusters
 * designed to run after Stable Cluster mapping
 * User: Steve
 * Date: 8/14/13
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusterIdentityMergeReducer extends AbstractParameterizedReducer {

    private IClusterStabilityAssessor clusterStabilityAssessor = new CountBasedClusterStabilityAssessor();

    @Override
    protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        ConfigurableProperties.configureAnalysisParameters(getApplication());
        HadoopDefaults.configureAnalysisParameters(getApplication());
    }

    @Override
    protected void reduceNormal(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String id = key.toString();
        IPeptideSpectralCluster mainCluster = null;
        for (Text value : values) {
            String text = value.toString();
            LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
            IPeptideSpectralCluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
            if (clusters.length == 0)
                continue;
            if (clusters.length > 1)
                throw new IllegalStateException("we should never get more than one");
            IPeptideSpectralCluster thisCluster = clusters[0];
            if (mainCluster == null) {
                mainCluster = thisCluster;
            } else {
                final List<ISpectrum> clusteredSpectra = thisCluster.getClusteredSpectra();
                mainCluster.addSpectra(clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]));
            }
        }

        //noinspection ConstantConditions
        if(!clusterStabilityAssessor.isSemiStable(mainCluster)) {
            throw new IllegalStateException("ClusterIdentity ust operate on stable and semistable clusters");
        }
        StringBuilder sb = new StringBuilder();
        final CGFClusterAppender clusterAppender = new CGFClusterAppender(new MGFSpectrumAppender());
        clusterAppender.appendCluster(sb, mainCluster);
        writeKeyValue(id, sb.toString(), context);
    }


}
