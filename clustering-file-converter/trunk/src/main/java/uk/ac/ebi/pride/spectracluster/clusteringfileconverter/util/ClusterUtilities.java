package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.util;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;

/**
 * Created by jg on 09.08.14.
 */
public class ClusterUtilities {
    private ClusterUtilities() {

    }

    public static int calculateClusterCharge(ICluster cluster) {
        // calculate the average charge
        int sumCharge = 0;

        for (ISpectrumReference specRef : cluster.getSpectrumReferences()) {
            sumCharge += specRef.getCharge();
        }

        float avCharge = (float) sumCharge / (float) cluster.getSpectrumReferences().size();
        int avChargeRounded = (int) (avCharge + 0.5);

        return avChargeRounded;
    }
}
