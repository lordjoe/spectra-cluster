package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class SimpleClusterSet extends SimpleClusterRetriever implements IClusterSet {

    public SimpleClusterSet(Collection<ISpectralCluster> clusters) {
        super(clusters);
    }

    public SimpleClusterSet() {
        super();
    }

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    @Override
    public List<ISpectralCluster> getMatchingClusters(TypedPredicate<ISpectralCluster> condition) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : getClusters()) {
            if (condition.apply(cluster))
                holder.add(cluster);
        }
        return holder;
    }


    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<ISpectralCluster> visitor, TypedVisitor<ISpectralCluster>... otherVisitors) {
        for (ISpectralCluster cluster : getClusters()) {
            visitor.visit(cluster);
            for (int i = 0; i < otherVisitors.length; i++) {
                otherVisitors[i].visit(cluster);

            }
        }

    }


    @Override
    public void addClusters(Collection<ISpectralCluster> clusters) {
        for (ISpectralCluster cluster : clusters) {
            addCluster(cluster);
        }
    }

    @Override
    public List<ISpectralCluster> getClustersWithSpectrum(ISpectrum spectrum) {
        String id = spectrum.getId();
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster test : getClusters()) {
            List<ISpectrum> spcetrums = test.getClusteredSpectra();
            for (ISpectrum spcetrum : spcetrums) {
                if (id.equals(spcetrum.getId())) {
                    holder.add(test);
                    break;
                }
            }
        }

        return holder;
    }

    @Override
    public List<ISpectralCluster> getBestMatchingClusters(ISpectralCluster cluster, int maxMatches) {
        ClusterQualityComparator clusterQualityComparator = new ClusterQualityComparator(cluster);

        return getBestMatchesWithQuality(maxMatches, clusterQualityComparator);
    }

    private List<ISpectralCluster> getBestMatchesWithQuality(int maxMatches, ClusterQualityComparator clusterQualityComparator) {
        List<ISpectralCluster> clusters = getClusters();
        Collections.sort(clusters, clusterQualityComparator);

        int numberOfMatches = clusters.size() < maxMatches ? clusters.size() : maxMatches;
        return clusters.subList(0, numberOfMatches);
    }

    private static class ClusterQualityComparator implements Comparator<ISpectralCluster> {

        private final ISpectralCluster clusterToMatch;

        private ClusterQualityComparator(ISpectralCluster clusterToMatch) {
            this.clusterToMatch = clusterToMatch;
        }

        @Override
        public int compare(ISpectralCluster o1, ISpectralCluster o2) {
            double match1 = matchQuality(o1, clusterToMatch);
            double match2 = matchQuality(o2, clusterToMatch);

            return Double.compare(match1, match2);
        }

        private double matchQuality(ISpectralCluster c1, ISpectralCluster c2) {
            Set<String> pep1 = new HashSet<String>(c1.getPeptides());
            Set<String> pep2 = new HashSet<String>(c2.getPeptides());
            int totalPeptides = pep1.size() + pep2.size();
            if (totalPeptides == 0)
                return 0;
            Set<String> common = new HashSet<String>(pep1);
            common.retainAll(pep2);

            double commonCunt = common.size();
            return commonCunt / totalPeptides;


        }
    }
}
