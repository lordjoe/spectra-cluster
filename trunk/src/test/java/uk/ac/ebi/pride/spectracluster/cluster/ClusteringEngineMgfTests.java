package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

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
        IClusteringEngineFactory factory = ClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator()) ;
        IClusteringEngine oldClusteringEngine = factory.getClusteringEngine();

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
