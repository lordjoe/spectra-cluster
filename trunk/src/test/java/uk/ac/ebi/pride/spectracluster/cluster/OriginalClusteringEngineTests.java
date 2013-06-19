package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.jmzreader.model.*;
import uk.ac.ebi.pride.tools.mgf_parser.*;
import uk.ac.ebi.pride.tools.mgf_parser.model.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.impl.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class OriginalClusteringEngineTests {

    private List<String> spectrumIds = new ArrayList<String>(Arrays.asList("83931", "1258781", "3722"));
    private List<ISpectrum> originalSpectra;
    private IClusteringEngine clusteringEngine;
    private IClusteringEngine oldClusteringEngine;
    private IClusteringEngine originalClusteringEngine;
    private static List<Spectrum> spectra;   // spectra gathered the old way
    private static List<SpectraCluster> originalSpectraList;   // spectra gathered the old way
    private static final SpectraClustering originalClustering = new FrankEtAlClustering();

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = OriginalClusteringEngineTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        ISpectrum[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
        originalSpectra = Arrays.asList(mgfSpectra);

        oldClusteringEngine = new ClusteringEngine(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        clusteringEngine = Defaults.INSTANCE.getDefaultClusteringEngine();
        originalClusteringEngine = new PrideClusteringEngine();
        for (ISpectrum originalSpectrum : originalSpectra) {
            final ISpectralCluster iSpectralCluster = originalSpectrum.asCluster();
            clusteringEngine.addClusters(iSpectralCluster);
            oldClusteringEngine.addClusters(iSpectralCluster);
            originalClusteringEngine.addClusters(iSpectralCluster);
          }

        // read spectra as the old code does
        if (spectra == null) {
            MgfFile mgfFile = new MgfFile(inputFile);

            Assert.assertNotNull(mgfFile);
            spectra = new ArrayList<Spectrum>(mgfFile.getMs2QueryCount());
            Iterator<Ms2Query> it = mgfFile.getMs2QueryIterator();
            while (it.hasNext()) {
                Ms2Query query = it.next();
                if (query.getPrecursorIntensity() == null)
                    query.setPeptideIntensity(1.0);

                spectra.add(query);
            }
        }
        originalSpectraList = originalClustering.clusterSpectra(spectra);


        while (clusteringEngine.mergeClusters()) ; // find clusters

        while (oldClusteringEngine.mergeClusters()) ; // find clusters

        while (originalClusteringEngine.mergeClusters()) ; // find clusters

    }

    @Test
    public void testClusteringEngine() throws Exception {

        final List<ISpectralCluster> newClusters = clusteringEngine.getClusters();


        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);

        List<ISpectralCluster> originalClusters = originalClusteringEngine.getClusters();
        Collections.sort(originalClusters);

        Assert.assertEquals(originalSpectraList.size(), originalClusters.size());


    }
}
