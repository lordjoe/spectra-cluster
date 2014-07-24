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

/**
 * Created by jg on 14.07.14.
 */
public class ClusterParameterExtractorTest {
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
    public void testParameterExtractor() throws Exception {
        ClusterParameterExtractor parameterExtractor = new ClusterParameterExtractor();

        ArrayList<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(1);
        listeners.add(parameterExtractor);

        reader.readClustersIteratively(listeners);

        String resultString = parameterExtractor.getAnalysisResultString();

        String[] lines = resultString.split("\n");

        Assert.assertEquals(4020, lines.length);
        Assert.assertEquals("305.000\t1.000\t2\t1.000\t1.000", lines[1]);
        Assert.assertEquals("305.002\t1.000\t6\t0.333\t0.333", lines[11]);
    }
}