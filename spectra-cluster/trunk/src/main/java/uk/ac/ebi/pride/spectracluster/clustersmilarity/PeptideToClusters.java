package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideToClusters {
    private final Map<String, List<IPeptideSpectrumCluster>> clustersWithPeptides = new HashMap<String, List<IPeptideSpectrumCluster>>();
    private final Map<String, List<IPeptideSpectrumCluster>> clustersWithMajorPeptides = new HashMap<String, List<IPeptideSpectrumCluster>>();

    public Map<String, List<IPeptideSpectrumCluster>> getClustersWithPeptides() {
        return clustersWithPeptides;
    }

    public Map<String, List<IPeptideSpectrumCluster>> getClustersWithMajorPeptides() {
        return clustersWithMajorPeptides;
    }

    public void addCluster(IPeptideSpectrumCluster cluster) {
        List<String> peptides = cluster.getPeptides();
        if (peptides.size() == 0) {
            return;
        }

        String firstPeptide = peptides.get(0);
        addPeptideCluster(firstPeptide, cluster, clustersWithMajorPeptides);

        for (String peptide : peptides) {
            addPeptideCluster(peptide, cluster, clustersWithPeptides);
        }
    }

    private void addPeptideCluster(String peptide, IPeptideSpectrumCluster cluster, Map<String, List<IPeptideSpectrumCluster>> clusters) {
        List<IPeptideSpectrumCluster> existingClusters = clusters.get(peptide);
        if (existingClusters == null) {
            existingClusters = new ArrayList<IPeptideSpectrumCluster>();
            clusters.put(peptide, existingClusters);
        }
        existingClusters.add(cluster);
    }

}
