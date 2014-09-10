package uk.ac.ebi.pride.spectracluster.psmassessmentextractor;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceReader;
import uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment.PsmAssessmentExtractor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jg on 10.09.14.
 */
public class PsmAssessmentTest {
    IClusterSourceReader reader;

    @Before
    public void setUp() throws URISyntaxException {
        URI testfileUri = PsmAssessmentTest.class.getClassLoader().getResource("testfile.clustering").toURI();
        reader = new ClusteringFileReader(new File(testfileUri));
    }

    @Test
    public void testPsmAssessments() throws Exception {
        PsmAssessmentExtractor assessmentExtractor = new PsmAssessmentExtractor();
        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>();
        listeners.add(assessmentExtractor);

        reader.readClustersIteratively(listeners);

        Map<String, Integer> psmAssessments = assessmentExtractor.getPsmAssessments();

        Assert.assertEquals(7234, psmAssessments.size());

        // count the number of assessments
        int n1 = 0;
        int n2 = 0;
        int n3 = 0;

        for (Integer assessment : psmAssessments.values()) {
            if (assessment == 1)
                n1++;
            else if (assessment == 2)
                n2++;
            else if (assessment == 3)
                n3++;
            else
                throw new Exception("Illegal PSM assessment encountered: " + assessment);
        }

        Assert.assertEquals(4335, n1);
        Assert.assertEquals(1188, n2);
        Assert.assertEquals(1711, n3);

        // check individual assessments
        Assert.assertEquals(2, (int) psmAssessments.get("PRD000493;PRIDE_Exp_Complete_Ac_18184.xml;spectrum=122816"));
        Assert.assertEquals(1, (int) psmAssessments.get("PXD000090;PRIDE_Exp_Complete_Ac_27995.xml;spectrum=2095"));
        Assert.assertEquals(3, (int) psmAssessments.get("PRD000439;PRIDE_Exp_Complete_Ac_17638.xml;spectrum=3845"));
        Assert.assertEquals(1, (int) psmAssessments.get("PRD000439;PRIDE_Exp_Complete_Ac_17609.xml;spectrum=5628"));
    }
}
