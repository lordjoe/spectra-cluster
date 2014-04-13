package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectrumInClusterReducer
 *
 * Merge spectra with unstable clusters
 */
public class SpectrumInClusterReducer extends AbstractParameterizedReducer {

    private boolean spectrumInBestCluster;

    @Override protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);

        ISetableParameterHolder application = getApplication();
        spectrumInBestCluster = application.getBooleanParameter(ClusterUtilities.PLACE_SPECTRUM_IN_BEST_CLUSTER);
    }


    @Override
    public void reduceNormal(Text key, Iterable<Text> values,
                             Context context) throws IOException, InterruptedException {

        String spectrumId = key.toString();
        int numberProcessed = 0;

        // grab all spectraInClusters - these are guaranteed to be small
        List<SpectrumInCluster> holder = new ArrayList<SpectrumInCluster>();


        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();

            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            SpectrumInCluster sci2 = ClusterUtilities.readSpectrumInCluster(rdr);
            holder.add(sci2);
        }

        if (holder.size() == 0)
            return;
        SpectrumInCluster.handleClusters(holder);
        for (SpectrumInCluster spectrumInCluster : holder) {
            writeSpectrumInCluster(spectrumInCluster,context);
        }

    }

     protected void writeSpectrumInCluster( SpectrumInCluster  cluster, Context context) {
         throw new UnsupportedOperationException("Fix This"); // ToDo

     }

}
