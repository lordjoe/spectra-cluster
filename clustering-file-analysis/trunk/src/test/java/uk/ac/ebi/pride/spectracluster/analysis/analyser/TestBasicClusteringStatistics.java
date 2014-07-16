package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.analysis.TestUtilities;
import uk.ac.ebi.pride.spectracluster.analysis.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceReader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
                "Number of clusters: 4019 (619 with 1 spec)\n" +
                "Average maximum ratio: 0.756\n" +
                "Average cluster size: 8.572\n" +
                "Minimum size: 1\n" +
                "Maximum size: 202\n" +
                "Minimum ratio: 0.046\n" +
                "Maximum ratio: 1.000\n" +
                "Stable clusters: 217\n",
               simpleStat);
    }
}
