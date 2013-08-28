package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.quality.OriginalSignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.comparator.ClusterComparator;

import java.util.List;

import static junit.framework.Assert.assertEquals;

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
        assertEquals(result, originalResult);
    }
}
