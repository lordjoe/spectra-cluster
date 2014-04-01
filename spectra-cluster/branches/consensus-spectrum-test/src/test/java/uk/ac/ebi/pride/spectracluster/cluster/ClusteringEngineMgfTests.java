package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineMgfTests {


    @Test
    public void testClusteringEngine() throws Exception {
//        List<String> spectrumIds = new ArrayList<String>(Arrays.asList("86434", "6777", "5", "291", "13480", "17877", "117146"));
        List<String> spectrumIds = new ArrayList<String>(Arrays.asList("6777", "291", "13480"));
        List<IPeptideSpectrumMatch> originalSpectra = ClusteringTestUtilities.readISpectraFromResource();
  //      IClusteringEngine clusteringEngine = Defaults.INSTANCE.getDefaultClusteringEngine();
        IClusteringEngine oldClusteringEngine = new ClusteringEngine(new FrankEtAlDotProduct(), Defaults.INSTANCE.getDefaultSpectrumComparator());

        for (ISpectrum originalSpectrum : originalSpectra) {
            if (spectrumIds.contains(originalSpectrum.getId())) {
//                clusteringEngine.addClusters(originalSpectrum.asCluster());
                oldClusteringEngine.addClusters(originalSpectrum.asCluster());
            }
        }

        for (int i = 0; i <= 2; i++) {
            if (!oldClusteringEngine.processClusters()) {
                break;
            }
        }

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);
    }
}