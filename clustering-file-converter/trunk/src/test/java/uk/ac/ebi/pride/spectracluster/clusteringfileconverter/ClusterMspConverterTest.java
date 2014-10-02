package uk.ac.ebi.pride.spectracluster.clusteringfileconverter;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters.ClusterMspConverter;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jg on 09.08.14.
 */
public class ClusterMspConverterTest {
    ClusteringFileReader reader;
    List<ICluster> clusters;
    ClusterMspConverter converter;

    @Before
    public void setUp() throws Exception {
        URI testFileUri = ClusterMspConverterTest.class.getClassLoader().getResource("testfile.clustering").toURI();
        reader = new ClusteringFileReader(new File(testFileUri));
        clusters = reader.readAllClusters();
        converter = new ClusterMspConverter();
    }

    @Test
    public void testConversion() {
        String mspCluster10 = converter.convertCluster(clusters.get(10));

        Assert.assertEquals("Name: KNYGK/0\n" +
                "Comment: Spec=Consensus Mods=0 Parent=305.01 Nreps=1 Naa=5 MaxRatio=1.0 PrecursorMzRange=0.0\n" +
                "Num peaks: 33\n" +
                "93.084 210.05\n" +
                "95.164 190.43\n" +
                "110.259 1323.93\n" +
                "129.109 543.6\n" +
                "171.111 459.92\n" +
                "175.276 702.12\n" +
                "191.088 686.29\n" +
                "259.323 1191.14\n" +
                "262.339 996.98\n" +
                "263.435 899.24\n" +
                "269.336 892.12\n" +
                "270.288 803.25\n" +
                "319.355 829.02\n" +
                "320.215 833.09\n" +
                "359.311 860.71\n" +
                "370.553 2184.54\n" +
                "391.858 2625.43\n" +
                "401.437 450.39\n" +
                "406.148 2459.2\n" +
                "406.774 488.67\n" +
                "421.269 795.16\n" +
                "425.916 495.13\n" +
                "502.375 101.72\n" +
                "508.51 91.03\n" +
                "525.282 107.37\n" +
                "554.575 140.59\n" +
                "589.398 93.76\n" +
                "601.327 71.06\n" +
                "602.224 67.64\n" +
                "603.364 271.1\n" +
                "611.135 54.08\n" +
                "630.366 94.61\n" +
                "737.475 51.48\n", mspCluster10);
    }

    @Test
    public void testCompleteConversion() throws Exception {
        File tmpFile = File.createTempFile("ConversionTest", ".msp");

        converter.setOutputPath(tmpFile.getPath());

        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
        listeners.add(converter);

        reader.readClustersIteratively(listeners);
        converter.close();
    }

    @Test
    public void testExternalFileConversion() throws Exception {
        File externalFile = new File("/tmp/ClusteringBin0421.clustering");

        if (!externalFile.exists()  || !externalFile.isFile())
            return;

        ClusteringFileReader externalReader = new ClusteringFileReader(externalFile);


        converter.setOutputPath(externalFile.getPath() + ".msp");

        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
        listeners.add(converter);

        externalReader.readClustersIteratively(listeners);
        converter.close();
    }
}
