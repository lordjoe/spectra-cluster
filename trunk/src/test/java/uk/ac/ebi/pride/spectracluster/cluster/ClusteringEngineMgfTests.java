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

    private List<String> spectrumIds = new ArrayList<String>(Arrays.asList("83931", "1258781", "3722"));
    private List<ISpectrum> originalSpectra;
    private IClusteringEngine clusteringEngine;
    private IClusteringEngine oldClusteringEngine;

    @Before
    public void setUp() throws Exception {
         originalSpectra = ClusteringTestUtilities.readISpectraFromResource();

        oldClusteringEngine = new ClusteringEngine(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        clusteringEngine = Defaults.INSTANCE.getDefaultClusteringEngine();
        for (ISpectrum originalSpectrum : originalSpectra) {
            if (spectrumIds.contains(originalSpectrum.getId())) {
                clusteringEngine.addClusters(originalSpectrum.asCluster());
                oldClusteringEngine.addClusters(originalSpectrum.asCluster());
            }
        }
    }

    @Test
    public void testClusteringEngine() throws Exception {
        for (int i = 0; i <= 2; i++) {
            if (!oldClusteringEngine.mergeClusters()) {
                break;
            }
        }

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);
    }
}
