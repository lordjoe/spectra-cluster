package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineMgfTests {


    @Test
    public void testClusteringEngine() throws Exception {
        List<String> spectrumIds = new ArrayList<String>(Arrays.asList("83931", "1258781", "3722"));
        List<IPeptideSpectrumMatch> originalSpectra = ClusteringTestUtilities.readISpectraFromResource();
        IClusteringEngine clusteringEngine = Defaults.INSTANCE.getDefaultClusteringEngine();
        IClusteringEngine oldClusteringEngine = new ClusteringEngine(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());


        for (ISpectrum originalSpectrum : originalSpectra) {
            if (spectrumIds.contains(originalSpectrum.getId())) {
                clusteringEngine.addClusters(originalSpectrum.asCluster());
                oldClusteringEngine.addClusters(originalSpectrum.asCluster());
            }
        }
        for (int i = 0; i <= 2; i++) {
            if (!oldClusteringEngine.mergeClusters()) {
                break;
            }
        }

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);
    }
}
