package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectrumInClustererRecombineReducer
 * <p/>
 * Merge spectra with unstable clusters
 */
public class SpectrumInClustererRecombineReducer extends AbstractParameterizedReducer {

    private boolean spectrumInBestCluster;

    @Override protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);

        ISetableParameterHolder application = getApplication();
        spectrumInBestCluster = application.getBooleanParameter(ClusterUtilities.PLACE_SPECTRUM_IN_BEST_CLUSTER,false);
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isSpectrumInBestCluster() {
        return spectrumInBestCluster;
    }

    @Override
    public void reduceNormal(Text key, Iterable<Text> values,
                             Context context) throws IOException, InterruptedException {

        SpectralCluster sc = new SpectralCluster();
        Set<String> processedSpectrunIds = new HashSet<String>();
            // Note this will not be large so memory requirements are ok

         for (Text tv : values) {
            String value = tv.toString();
            LineNumberReader rdr = new LineNumberReader((new StringReader(value)));
            SpectrumInCluster sci2 = ClusterUtilities.readSpectrumInCluster(rdr);
            IPeptideSpectrumMatch spectrum = sci2.getSpectrum();
            String id = spectrum.getId();
            if (!sci2.isRemoveFromCluster()) {
                sc.addSpectra(spectrum);
            }
            else {
                // handle spectra kicked out
                if (!processedSpectrunIds.contains(id)) {
                    ISpectralCluster cluster = spectrum.asCluster();
                      writeOneVettedCluster(context, cluster);
                }
                else {
                    System.out.println("duplicate id " + id);
                }

            }
            processedSpectrunIds.add(id);
        }
        if (sc.getClusteredSpectraCount() == 0)
            return;
        writeOneVettedCluster(context, sc);

    }


    /**
     * this version of writeCluster does all the real work
     *
     * @param context
     * @param cluster
     * @throws IOException
     * @throws InterruptedException
     */
    protected void writeOneVettedCluster(@Nonnull final Context context, @Nonnull final ISpectralCluster cluster) throws IOException, InterruptedException {
        if (cluster.getClusteredSpectraCount() == 0)
            return; // empty dont bother

        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), cluster.getPrecursorMz());

        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            writeKeyValue(key.toString(), string, context);

        }
    }


}