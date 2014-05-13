package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideToClusters {
    private final Map<String, List<ISpectralCluster>> clustersWithPeptides = new HashMap<String, List<ISpectralCluster>>();
    private final Map<String, List<ISpectralCluster>> clustersWithMajorPeptides = new HashMap<String, List<ISpectralCluster>>();

    public Map<String, List<ISpectralCluster>> getClustersWithPeptides() {
        return clustersWithPeptides;
    }

    public Map<String, List<ISpectralCluster>> getClustersWithMajorPeptides() {
        return clustersWithMajorPeptides;
    }

    public void addCluster(ISpectralCluster cluster) {
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

    private void addPeptideCluster(String peptide, ISpectralCluster cluster, Map<String, List<ISpectralCluster>> clusters) {
        List<ISpectralCluster> existingClusters = clusters.get(peptide);
        if (existingClusters == null) {
            existingClusters = new ArrayList<ISpectralCluster>();
            clusters.put(peptide, existingClusters);
        }
        existingClusters.add(cluster);
    }

}
