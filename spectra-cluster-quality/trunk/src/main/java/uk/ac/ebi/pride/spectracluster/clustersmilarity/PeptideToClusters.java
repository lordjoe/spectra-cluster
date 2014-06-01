package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideToClusters {
    private final Map<String, List<ICluster>> clustersWithPeptides = new HashMap<String, List<ICluster>>();
    private final Map<String, List<ICluster>> clustersWithMajorPeptides = new HashMap<String, List<ICluster>>();

    public Map<String, List<ICluster>> getClustersWithPeptides() {
        return clustersWithPeptides;
    }

    public Map<String, List<ICluster>> getClustersWithMajorPeptides() {
        return clustersWithMajorPeptides;
    }

    public void addCluster(ICluster cluster) {
        List<String> peptides = ClusterSimilarityUtilities.getClusterPeptides(cluster) ;
        if (peptides.size() == 0) {
            return;
        }

        String firstPeptide = peptides.get(0);
        addPeptideCluster(firstPeptide, cluster, clustersWithMajorPeptides);

        for (String peptide : peptides) {
            addPeptideCluster(peptide, cluster, clustersWithPeptides);
        }
    }

    private void addPeptideCluster(String peptide, ICluster cluster, Map<String, List<ICluster>> clusters) {
        List<ICluster> existingClusters = clusters.get(peptide);
        if (existingClusters == null) {
            existingClusters = new ArrayList<ICluster>();
            clusters.put(peptide, existingClusters);
        }
        existingClusters.add(cluster);
    }

}
