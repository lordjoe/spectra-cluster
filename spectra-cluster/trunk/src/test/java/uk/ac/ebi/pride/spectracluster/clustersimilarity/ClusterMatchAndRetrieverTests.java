package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IClusterRetriever;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterRetriever;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.ClusterMatchAndRetrieverTests
 *
 * @author Steve Lewis
 * @date 05/11/13
 */
public class ClusterMatchAndRetrieverTests {

    protected IClusterRetriever buildRetriever() {
        List<IPeptideSpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        return new SimpleClusterRetriever(originalSpectralClusters);
    }


    @Test
    public void testRetriever() throws Exception {
        List<IPeptideSpectralCluster> start = ClusteringTestUtilities.readSpectraClustersFromResource();
        IClusterRetriever retriever = buildRetriever();

        // make sure we can retrieve all clusters with retrieve
        for (IPeptideSpectralCluster sc : start) {
            final String id = sc.getSpectralId();
            IPeptideSpectralCluster retrieve = retriever.retrieve(id);
            Assert.assertTrue(retrieve.equivalent(sc));
        }

        // make sure we can retrieve all clusters with retrieve (minMZ,MaxMZ)
        Collection<IPeptideSpectralCluster> retrieve = retriever.retrieve(0, 10000);
        List<IPeptideSpectralCluster> retrieved = new ArrayList<IPeptideSpectralCluster>(retrieve);
        Assert.assertEquals(retrieved.size(), start.size());

        Collections.sort(retrieved);
        Collections.sort(start);

        for (int i = 0; i < retrieved.size(); i++) {
            Assert.assertTrue(retrieved.get(i).equivalent(start.get(i)));
        }

        Collection<IPeptideSpectralCluster> ret400 = retriever.retrieve(400, 401);
        for (IPeptideSpectralCluster sc : ret400) {
            double mz = sc.getPrecursorMz();
            Assert.assertTrue(mz >= 400);
            Assert.assertTrue(mz <= 401);
        }
    }
}
