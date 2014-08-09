package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.analysis.TestUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jg on 12.07.14.
 */
public class TestBasicClusteringStatistics {
    private File testFile;
    private IClusterSourceReader reader;

    @Before
    public void setUp() {
        try {
            testFile = TestUtilities.getTestfile();
            reader = new ClusteringFileReader(testFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAnalysis() throws Exception {
        BasicClusteringStatistics basicStat = new BasicClusteringStatistics();

        List<IClusterSourceListener> analysers = new ArrayList<IClusterSourceListener>(1);
        analysers.add(basicStat);

        reader.readClustersIteratively(analysers);

        String simpleStat = basicStat.getAnalysisResultString();

        Assert.assertEquals(
                "Number of clusters: 960 (256 with 1 spec)\n" +
                        "Average maximum ratio: 0.760\n" +
                        "Average cluster size: 12.173\n" +
                        "Minimum size: 1\n" +
                        "Maximum size: 397\n" +
                        "Minimum ratio: 0.050\n" +
                        "Maximum ratio: 1.000\n" +
                        "Stable clusters: 56\n",
               simpleStat);
    }
}
