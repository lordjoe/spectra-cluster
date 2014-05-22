package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideToClusters {
    private final Map<String, List<IPeptideSpectralCluster>> clustersWithPeptides = new HashMap<String, List<IPeptideSpectralCluster>>();
    private final Map<String, List<IPeptideSpectralCluster>> clustersWithMajorPeptides = new HashMap<String, List<IPeptideSpectralCluster>>();

    public Map<String, List<IPeptideSpectralCluster>> getClustersWithPeptides() {
        return clustersWithPeptides;
    }

    public Map<String, List<IPeptideSpectralCluster>> getClustersWithMajorPeptides() {
        return clustersWithMajorPeptides;
    }

    public void addCluster(IPeptideSpectralCluster cluster) {
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

    private void addPeptideCluster(String peptide, IPeptideSpectralCluster cluster, Map<String, List<IPeptideSpectralCluster>> clusters) {
        List<IPeptideSpectralCluster> existingClusters = clusters.get(peptide);
        if (existingClusters == null) {
            existingClusters = new ArrayList<IPeptideSpectralCluster>();
            clusters.put(peptide, existingClusters);
        }
        existingClusters.add(cluster);
    }

}
