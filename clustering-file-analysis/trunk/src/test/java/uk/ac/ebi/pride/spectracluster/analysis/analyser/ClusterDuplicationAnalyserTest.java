package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.analysis.TestUtilities;
import uk.ac.ebi.pride.spectracluster.analysis.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jg on 15.07.14.
 */
public class ClusterDuplicationAnalyserTest {
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
        ClusterDuplicationAnalyser analyzer = new ClusterDuplicationAnalyser();

        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
        listeners.add(analyzer);

        reader.readClustersIteratively(listeners);

        String result = analyzer.getAnalysisResultString();

        Assert.assertEquals("count\tfrequency\n" +
                "1\t888\n" +
                "2\t785\n" +
                "3\t1159\n" +
                "4\t924\n" +
                "5\t1638\n" +
                "6\t1956\n", result);

        Assert.assertEquals(5, (int) analyzer.getSpecIdCounts().get("PXD000637;PRIDE_Exp_Complete_Ac_33209.xml;spectrum=1410"));
    }
}
