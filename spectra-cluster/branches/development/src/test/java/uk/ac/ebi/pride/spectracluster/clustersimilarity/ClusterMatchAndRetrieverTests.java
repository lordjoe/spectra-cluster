package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.ClusterMatchAndRetrieverTests
 *
 * @author Steve Lewis
 * @date 05/11/13
 */
public class ClusterMatchAndRetrieverTests {

    protected IClusterRetriever buildRetriever()
    {
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        return new SimpleClusterRetriever(  originalSpectralClusters);
    }


    @Test
    public void testRetriever() throws Exception {
        List<ISpectralCluster> start = ClusteringTestUtilities.readSpectraClustersFromResource();
        IClusterRetriever retriever = buildRetriever() ;

        // make sure we can retrieve all clusters with retrieve
        for (ISpectralCluster sc : start) {
            ISpectralCluster retrieve = retriever.retrieve(sc.getId());
            Assert.assertTrue( retrieve.equivalent(sc));
        }

     // make sure we can retrieve all clusters with retrieve (minMZ,MaxMZ)
        Collection<ISpectralCluster> retrieve = retriever.retrieve(0, 10000);
        List<ISpectralCluster> retrieved = new ArrayList<ISpectralCluster>(retrieve) ;
        Assert.assertEquals(retrieved.size(),start.size());

        Collections.sort(retrieved);
        Collections.sort(start);

        for (int i = 0; i < retrieved.size(); i++) {
            Assert.assertTrue(retrieved.get(i).equivalent(start.get(i)));
          }

        Collection<ISpectralCluster> ret400 = retriever.retrieve(400, 401);
        for (ISpectralCluster sc : ret400) {
             double mz = sc.getPrecursorMz();
            Assert.assertTrue( mz >= 400);
            Assert.assertTrue( mz <= 401);
        }
    }
}
