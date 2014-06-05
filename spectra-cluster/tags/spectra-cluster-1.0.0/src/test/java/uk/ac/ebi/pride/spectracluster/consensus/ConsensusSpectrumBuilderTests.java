package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.filter.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

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

        // do nothing to filter
        Defaults.setDefaultPeakFilter(IPeakFilter.NULL_FILTER);


        IConsensusSpectrumBuilder factory2 = new JohannesConsensusSpectrum();
  //         ConcensusSpectrumBuilderFactory factoryX2 = ConsensusSpectrum.FACTORY;
      //  IConsensusSpectrumBuilder factory2 = Defaults.getDefaultConsensusSpectrumBuilder();
        IConsensusSpectrumBuilder nofilter = ConsensusSpectrum.buildFactory(IPeakFilter.NULL_FILTER).getConsensusSpectrumBuilder();


        List<ICluster> clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        //noinspection UnusedDeclaration
        int numberTested = 0;

        //noinspection UnusedDeclaration,UnusedAssignment
        long start = System.currentTimeMillis();
        List<ISpectrum> fromFactory1 = ClusteringTestUtilities.buildConsessusSpectra(clusters, nofilter);
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
            final double v = Defaults.getDefaultSimilarityChecker().assessSimilarity(newSpec, oldSpec);
            if (!equivalent) {

                equivalent = ClusteringTestUtilities.areSpectraVeryClose(oldSpec, newSpec);// break here do debug failure
                Assert.assertTrue(equivalent);
            }

        }

    }


}
