package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;

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

    private final Map<String, IPeptideSpectrumCluster> clusterById = new HashMap<String, IPeptideSpectrumCluster>();
    private final List<IPeptideSpectrumCluster> sortedClusters = new ArrayList<IPeptideSpectrumCluster>();
    private int numberDuplicateClusters;

    public SimpleClusterRetriever(Collection<IPeptideSpectrumCluster> cluaters) {
        for (IPeptideSpectrumCluster cluater : cluaters) {
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
    public List<IPeptideSpectrumCluster> getClusters() {
        if (sortedClusters.isEmpty()) {
            ArrayList<IPeptideSpectrumCluster> clusters = new ArrayList<IPeptideSpectrumCluster>(clusterById.values());
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

    public void addCluster(IPeptideSpectrumCluster cluster) {
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

    public void guaranteeClusterId(IPeptideSpectrumCluster cluster) {
        if (cluster.getId() != null) {
            return;
        }

        buildAndSetIdForClusterWithoutId(cluster);
    }

    protected void buildAndSetIdForClusterWithoutId(IPeptideSpectrumCluster cluster) {
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

    public SimpleClusterRetriever(IPeptideSpectrumCluster... cluaters) {
        this(Arrays.asList(cluaters));
    }

    @Override
    public IPeptideSpectrumCluster retrieve(String clusterId) {
        return clusterById.get(clusterId);
    }

    @Override
    public List<IPeptideSpectrumCluster> retrieve(double minMz, double maxMz) {
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        for (IPeptideSpectrumCluster cluster : clusterById.values()) {
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
