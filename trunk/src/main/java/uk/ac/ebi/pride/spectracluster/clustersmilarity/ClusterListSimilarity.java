package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterListSimilarity
 * User: Steve
 * Date: 6/17/13
 */
public class ClusterListSimilarity {
    public static final double MZ_EQUIVALENT_RANGE = 0.3;  // try comparing if this close

    private final ClusterDistance distanceMeasure;

    public ClusterListSimilarity(final ClusterDistance pDistanceMeasure) {
        distanceMeasure = pDistanceMeasure;
    }

    public ClusterDistance getDistanceMeasure() {
        return distanceMeasure;
    }

    /**
     * @param s1 - entering lists of spectral clusters - exit unmatched clusters
     * @param s2 - entering lists of spectral clusters - exit unmatched clusters
     * @return matched clusters from set 1
     */
    public List<ISpectralCluster> identicalClusters(List<ISpectralCluster> s1, List<ISpectralCluster> s2) {
        ClusterDistance dm = getDistanceMeasure();
        List<ISpectralCluster> identical = new ArrayList<ISpectralCluster>();
        List<ISpectralCluster> matched1 = new ArrayList<ISpectralCluster>();
        List<ISpectralCluster> matched2 = new ArrayList<ISpectralCluster>();

        Collections.sort(s1);   // sort by mz
        Collections.sort(s2);   // sort by mz

        final int nPK1 = s1.size();
        final int nPK2 = s2.size();

        final int TotakPeaks = nPK1 + nPK2;
        double ret = TotakPeaks;

        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        double oldSC1MZ = Double.MIN_VALUE;
        double oldSC2MZ = Double.MIN_VALUE;

        ISpectralCluster oldSc1 = null;
        Set<ISpectralCluster> alreadyMatched = new HashSet<ISpectralCluster>();

        while (t < nPK1 && e < nPK2) {
            ISpectralCluster sc1 = s1.get(t);
            double mz1 = sc1.getPrecursorMz();
            if (mz1 < oldSC1MZ)
                throw new IllegalStateException("Clusters must be sorted by MZ");
            oldSC1MZ = mz1;

            ISpectralCluster sc2 = s2.get(e);
            double mz2 = sc2.getPrecursorMz();
            if (mz2 < oldSC2MZ)
                throw new IllegalStateException("Clusters must be sorted by MZ");
            oldSC1MZ = mz2;

            double mass_difference = mz2 - mz1;
            if (Math.abs(mass_difference) <= MZ_EQUIVALENT_RANGE) {
                double distance = dm.distance(sc1, sc2);

                if (nPK1 == nPK2 && mass_difference == 0 && distance != 0)
                    distance = dm.distance(sc1, sc2);  // break here

                if (distance == 0) {
                    identical.add(sc1);
                    matched1.add(sc1);
                    matched2.add(sc2);
                    alreadyMatched.add(sc1);
                    alreadyMatched.add(sc2);
                }
                else {
                    boolean equivalent = sc1.equivalent(sc2);
                    if (equivalent) {
                        distance = dm.distance(sc1, sc2);  // break here
                    }
                }
            }
            if (mass_difference == 0) {
                if (!alreadyMatched.isEmpty()) {
                    if (alreadyMatched.contains(sc1)) {
                        lastIsT = true;
                        t++;
                        alreadyMatched.remove(sc1);
                    }
                    if (alreadyMatched.contains(sc2)) {
                        lastIsT = false;
                        e++;
                        alreadyMatched.remove(sc2);
                    }
                }
                else {
                    if (lastIsT) {
                      e++;
                      lastIsT = false;
                  }
                  else {
                      t++;
                      lastIsT = true;
                  }

                }
             }
            else {
                if (mass_difference < 0) {
                    alreadyMatched.clear();
                    e++;
                }
                else {
                    alreadyMatched.clear();
                    t++;
                }
            }
        }
        s1.removeAll(matched1); // drop matches
        s2.removeAll(matched2); // drop matches
        return identical;
    }
}
