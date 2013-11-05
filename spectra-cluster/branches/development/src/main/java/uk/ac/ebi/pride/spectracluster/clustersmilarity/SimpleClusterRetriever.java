package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterRetriever
 * Suupid in memory implementation if a retriever -
 * good for testing
 *
 * @author Steve Lewis
 * @date 05/11/13
 */
@SuppressWarnings("UnusedDeclaration")
public class SimpleClusterRetriever implements IClusterRetriever {

    private final Map<String, ISpectralCluster> clusterById = new HashMap<String, ISpectralCluster>();

    public SimpleClusterRetriever(Collection<ISpectralCluster> cluaters) {
        for (ISpectralCluster cluater : cluaters) {
            clusterById.put(cluater.getId(), cluater);
        }
    }

    public SimpleClusterRetriever( ISpectralCluster... cluaters) {
         this(Arrays.asList(cluaters));
     }

     @Override
    public ISpectralCluster retrieve(String clusterId) {
        return clusterById.get(clusterId);
    }

    @Override
    public Collection<ISpectralCluster> retrieve(double minMz, double maxMz) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusterById.values()) {
            double mz = cluster.getPrecursorMz();
            if (mz < minMz)
                continue;
            if (mz > maxMz)
                continue;
            holder.add(cluster);
        }

        return holder;
    }
}
