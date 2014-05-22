package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.PeptideSpectralCluster;

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

    private final Map<String, IPeptideSpectralCluster> clusterById = new HashMap<String, IPeptideSpectralCluster>();
    private final List<IPeptideSpectralCluster> sortedClusters = new ArrayList<IPeptideSpectralCluster>();
    private int numberDuplicateClusters;

    public SimpleClusterRetriever(Collection<IPeptideSpectralCluster> cluaters) {
        for (IPeptideSpectralCluster cluater : cluaters) {
            addCluster(cluater);
        }
    }

    public final int getClusterCount() {
        return clusterById.size();
    }

    public int getNumberDuplicateClusters() {
        return numberDuplicateClusters;
    }


    /**
     * Get sorted clusters
     */
    public List<IPeptideSpectralCluster> getClusters() {
        if (sortedClusters.isEmpty()) {
            ArrayList<IPeptideSpectralCluster> clusters = new ArrayList<IPeptideSpectralCluster>(clusterById.values());
            sortedClusters.addAll(clusters);
            try {
                Collections.sort(sortedClusters);
            } catch (Exception e) {
                Throwable t = e;
                while (t.getCause() != null && t.getCause() != t)
                    t = t.getCause();
                t.printStackTrace();
                Collections.sort(sortedClusters);
            }
        }
        return sortedClusters;
    }

    public void addCluster(IPeptideSpectralCluster cluster) {
        try {
            guaranteeClusterId(cluster);
        } catch (Exception e) {
            guaranteeClusterId(cluster);
            throw new RuntimeException(e);

        }
        String id = cluster.getSpectralId();
        if (!clusterById.containsKey(id)) {
            clusterById.put(id, cluster);
        } else {
            numberDuplicateClusters++;
        }
        sortedClusters.clear();
    }

    public void guaranteeClusterId(IPeptideSpectralCluster cluster) {
        if (cluster.getId() != null) {
            return;
        }

        buildAndSetIdForClusterWithoutId(cluster);
    }

    protected void buildAndSetIdForClusterWithoutId(IPeptideSpectralCluster cluster) {
        String id = cluster.toString();

        if (cluster instanceof LazyLoadedSpectralCluster) {
            ((LazyLoadedSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof PeptideSpectralCluster) {
            ((PeptideSpectralCluster) cluster).setId(id);
            return;
        }
        throw new IllegalStateException("cannot guarantee non-null id");
    }

    private static int clusterIdCounter = 1000;

    public static String generateClusterId() {

        return "GenId" + clusterIdCounter++;
    }

    public SimpleClusterRetriever(IPeptideSpectralCluster... cluaters) {
        this(Arrays.asList(cluaters));
    }

    @Override
    public IPeptideSpectralCluster retrieve(String clusterId) {
        return clusterById.get(clusterId);
    }

    @Override
    public List<IPeptideSpectralCluster> retrieve(double minMz, double maxMz) {
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster cluster : clusterById.values()) {
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
