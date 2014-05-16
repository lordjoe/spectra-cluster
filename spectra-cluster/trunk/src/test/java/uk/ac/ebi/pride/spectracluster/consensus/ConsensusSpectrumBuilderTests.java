package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrumBuilderTests
 *
 * @author Steve Lewis
 *         This tests the difference between  IConsensusSpectrumBuilder implementations
 */
public class ConsensusSpectrumBuilderTests {


    @Test
    public void testConsensusSpectrum() throws Exception {
        ConcensusSpectrumBuilderFactory factory1 = JohannesConsensusSpectrum.FACTORY;
        //     ConcensusSpectrumBuilderFactory factory2 = ConsensusSpectrum.FACTORY;
        ConcensusSpectrumBuilderFactory factory2 = ConsensusSpectrumNew.FACTORY;


        List<IPeptideSpectralCluster> clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        //noinspection UnusedDeclaration
        int numberTested = 0;

        //noinspection UnusedDeclaration,UnusedAssignment
        long start = System.currentTimeMillis();
        List<ISpectrum> fromFactory1 = ClusteringTestUtilities.buildConsessusSpectra(clusters, factory1);
        //noinspection UnusedDeclaration,UnusedAssignment
        long end = System.currentTimeMillis();
        //noinspection UnusedDeclaration
        //  double delSec1 = (end - start);  // how long did that take

        start = System.currentTimeMillis();
        List<ISpectrum> fromFactory2 = ClusteringTestUtilities.buildConsessusSpectra(clusters, factory2);
        end = System.currentTimeMillis();
        //noinspection UnusedDeclaration
        double delSec2 = (end - start);  // how long did that take


        Assert.assertEquals(fromFactory1.size(), fromFactory2.size());
        for (int i = 0; i < fromFactory1.size(); i++) {
            final ISpectrum oldSpec = fromFactory1.get(i);
            final ISpectrum newSpec = fromFactory2.get(i);
            boolean equivalent = ClusteringTestUtilities.areSpectraVeryClose(oldSpec, newSpec);
            if (!equivalent) {
                equivalent = ClusteringTestUtilities.areSpectraVeryClose(oldSpec, newSpec);// break here do debug failure
                Assert.assertTrue(equivalent);
            }

        }

    }


}
