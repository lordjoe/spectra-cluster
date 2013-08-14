package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.*;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparatorTests {

    private List<ISpectrum> peptideSpectrumMatches;
    private ClusterComparator clusterComparator;
    private OriginalClusterComparator originalClusterComparator;

    @Before
    public void setUp() throws Exception {
        clusterComparator = new ClusterComparator();
        originalClusterComparator = new OriginalClusterComparator(new OriginalSignalToNoiseChecker());

         peptideSpectrumMatches = ClusteringTestUtilities.readConsensusSpectralItems();
    }

    @Test
    public void testClusterComparator() throws Exception {
        final ISpectralCluster cluster1 = peptideSpectrumMatches.get(0).asCluster();
        int result = clusterComparator.compare(cluster1, peptideSpectrumMatches.get(1).asCluster());
        int originalResult = originalClusterComparator.compare(cluster1, peptideSpectrumMatches.get(1).asCluster());
        Assert.assertEquals(result, originalResult);
    }
}
