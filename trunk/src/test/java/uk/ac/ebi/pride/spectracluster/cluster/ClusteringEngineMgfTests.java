package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProductOld;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
