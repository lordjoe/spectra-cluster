package uk.ac.ebi.pride.tools.fast_spectra_clustering;


import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.junit.*;

import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.mgf_parser.model.Ms2Query;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.SpectraClustering;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.impl.FrankEtAlClustering;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;


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

        System.out.println("Clustering done in " + (stop - start) + " msec");


  //      ClusterWriter.dumpSpectra("FrankEtAl.cgf",generatedCluster);

   //     Assert.assertEquals(142, generatedCluster.size());
        Assert.assertEquals(141, generatedCluster.size());
         for (int i = 0; i < 3; i++) {
            SpectraCluster cluster = generatedCluster.get(i);

            if (i == 0) {
                Assert.assertEquals(402.01793764705883, cluster.getAverageMz(),0.001);
                Assert.assertEquals(8, cluster.getClusterSize());
                //System.out.println("Here1");
                boolean peakFound = false;
                for (Peak p : cluster.getConsensusSpectrum()) {
                    if (p.getMz() == 764.5533508019715) {
                        Assert.assertEquals(0.013422155963675658, p.getIntensity(),0.001);
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

        System.out.println("Clustering done in " + (stop - start) + " msec");

        Assert.assertEquals(168, generatedCluster.size());

        for (int i = 0; i < 6; i++) {
            SpectraCluster cluster = generatedCluster.get(i);

            if (i == 5) {
                Assert.assertEquals(401.67999, cluster.getAverageMz(), 5);
                Assert.assertEquals(1, cluster.getClusterSize());

                boolean peakFound = false;
                for (Peak p : cluster.getConsensusSpectrum()) {
                    if (p.getMz() == 233.92139) {
                        Assert.assertEquals(0.096907934822912, p.getIntensity(),0.001);
                        peakFound = true;
                    }
                }
                Assert.assertTrue(peakFound);
            }
        }
    }

}
