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
public class SimpleClusterRetriever implements IClusterRetriever {

    private final Map<String, ISpectralCluster> clusterById = new HashMap<String, ISpectralCluster>();
    private final List<ISpectralCluster> sortedClusters = new ArrayList<ISpectralCluster>();

    public SimpleClusterRetriever(Collection<ISpectralCluster> cluaters) {
        for (ISpectralCluster cluater : cluaters) {
            addCluster(cluater);
        }
    }

    public int getClusterCount() {
        return clusterById.size();
    }

    /**
     * Get sorted clusters
     */
    public List<ISpectralCluster> getClusters() {
        if (sortedClusters.isEmpty()) {
            ArrayList<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>(clusterById.values());
            sortedClusters.addAll(clusters);
            try {
                Collections.sort(sortedClusters);
            } catch (Exception e) {
                Throwable t = e;
                while(t.getCause() != null && t.getCause() != t)
                    t = t.getCause();
                t.printStackTrace();
                Collections.sort(sortedClusters);
            }
        }
        return sortedClusters;
    }

    public void addCluster(ISpectralCluster cluater) {
        guaranteeClusterId(cluater);
        clusterById.put(cluater.getId(), cluater);
        sortedClusters.clear();
    }

    public void guaranteeClusterId(ISpectralCluster cluster) {
        if (cluster.getId() != null) {
            return;
        }

        buildAndSetIdForClusterWithoutId(cluster);
    }

    protected void buildAndSetIdForClusterWithoutId(ISpectralCluster cluster) {
        String id = cluster.toString();

        if (cluster instanceof LazyLoadedSpectralCluster) {
            ((LazyLoadedSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof SpectralCluster) {
            ((SpectralCluster) cluster).setId(id);
            return;
        }
        throw new IllegalStateException("cannot guarantee non-null id");
    }

    private static int clusterIdCounter = 1000;

    public static String generateClusterId() {

        return "GenId" + clusterIdCounter++;
    }

    public SimpleClusterRetriever(ISpectralCluster... cluaters) {
        this(Arrays.asList(cluaters));
    }

    @Override
    public ISpectralCluster retrieve(String clusterId) {
        return clusterById.get(clusterId);
    }

    @Override
    public List<ISpectralCluster> retrieve(double minMz, double maxMz) {
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
