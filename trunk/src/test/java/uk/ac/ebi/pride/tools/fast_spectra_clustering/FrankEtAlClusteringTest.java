package uk.ac.ebi.pride.tools.fast_spectra_clustering;


import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.tools.jmzreader.model.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.impl.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.Peak;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;

import java.util.*;


public class FrankEtAlClusteringTest {



    @Test
    public void testClustering1() {
        SpectraClustering clustering = new FrankEtAlClustering();
        // set the clustering parameters
        clustering.setClusteringRounds(2);
        clustering.setSimilarityThreshold(0.7);

        List<Spectrum> spectra = ClusteringTestUtilities.readSpectrumsFromResource( );
        // do the clustering
        long start = System.currentTimeMillis();
        List<SpectraCluster> generatedCluster = clustering.clusterSpectra(spectra);
        long stop = System.currentTimeMillis();

        //System.out.println("Clustering done in " + (stop - start) + " msec");

         // NOTE Values are changed we sort differently
  //      ClusterWriter.dumpSpectra("FrankEtAl.cgf",generatedCluster);

   //     Assert.assertEquals(142, generatedCluster.size());
        Assert.assertEquals(141, generatedCluster.size());
        for (int i = 0; i < 3; i++) {
            SpectraCluster cluster = generatedCluster.get(i);


            if (i == 0) {
                Assert.assertEquals(400.438 , cluster.getAverageMz(),0.001);
                Assert.assertEquals(5, cluster.getClusterSize());
                //System.out.println("Here1");
                boolean peakFound = false;
                for (Peak p : cluster.getConsensusSpectrum()) {
                    if (Math.abs(p.getMz() - 686.52796) < IPeak.SMALL_MZ_DIFFERENCE) {
                        Assert.assertEquals(2.1374678, p.getIntensity(),IPeak.SMALL_INTENSITY_DIFFERENCE);
                        peakFound = true;
                    }
                }
                Assert.assertTrue(peakFound);
            }
        }
    }

    @Test
    public void testClustering2() {
        SpectraClustering clustering = new FrankEtAlClustering();
        // set the clustering parameters
        clustering.setClusteringRounds(2);
        clustering.setSimilarityThreshold(0.8);

        List<Spectrum> spectra = ClusteringTestUtilities.readSpectrumsFromResource( );
        // do the clustering
        long start = System.currentTimeMillis();
        List<SpectraCluster> generatedCluster = clustering.clusterSpectra(spectra);
        long stop = System.currentTimeMillis();

        // System.out.println("Clustering done in " + (stop - start) + " msec");

         // NOTE Values modifies to work
        // we do sort differently
        Assert.assertEquals(167, generatedCluster.size());
       // Assert.assertEquals(168, generatedCluster.size());

        for (int i = 0; i < 6; i++) {
            SpectraCluster cluster = generatedCluster.get(i);

            if (i == 5) {
                Assert.assertEquals(400.62, cluster.getAverageMz(), IPeak.SMALL_MZ_DIFFERENCE);
                Assert.assertEquals(1, cluster.getClusterSize());

                boolean peakFound = false;
                for (Peak p : cluster.getConsensusSpectrum()) {
                    if (Math.abs(p.getMz() -284.06317) < IPeak.SMALL_MZ_DIFFERENCE) {
                        Assert.assertEquals(6.644530262887737, p.getIntensity(),IPeak.SMALL_INTENSITY_DIFFERENCE);
                        peakFound = true;
                    }
                }
                Assert.assertTrue(peakFound);
            }
        }
    }

}
