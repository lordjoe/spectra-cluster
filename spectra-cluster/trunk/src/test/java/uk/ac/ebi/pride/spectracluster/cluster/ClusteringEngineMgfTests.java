package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.engine.ClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngineFactory;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProductOld;
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
        IClusteringEngineFactory factory = ClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
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

        List<ISpectralCluster> oldClusters = (List<ISpectralCluster>) oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);
    }
}
