package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterListSimilarity
 * User: Steve
 * Date: 6/17/13
 */
public class ClusterListSimilarity {
    public static final double MZ_EQUIVALENT_RANGE = 0.3;  // try comparing if this close

    private final IClusterDistance distanceMeasure;

    public ClusterListSimilarity(final IClusterDistance pDistanceMeasure) {
        distanceMeasure = pDistanceMeasure;
    }

    public IClusterDistance getDistanceMeasure() {
        return distanceMeasure;
    }

    /**
     * @param s1 - entering lists of spectral clusters - exit unmatched clusters
     * @param s2 - entering lists of spectral clusters - exit unmatched clusters
     * @return matched clusters from set 1
     */
    public List<IPeptideSpectrumCluster> identicalClusters(List<IPeptideSpectrumCluster> s1, List<IPeptideSpectrumCluster> s2) {
        IClusterDistance dm = getDistanceMeasure();
        List<IPeptideSpectrumCluster> identical = new ArrayList<IPeptideSpectrumCluster>();
        List<IPeptideSpectrumCluster> matched1 = new ArrayList<IPeptideSpectrumCluster>();
        List<IPeptideSpectrumCluster> matched2 = new ArrayList<IPeptideSpectrumCluster>();

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

        IPeptideSpectrumCluster oldSc1 = null;
        Set<IPeptideSpectrumCluster> alreadyMatched = new HashSet<IPeptideSpectrumCluster>();

        while (t < nPK1 && e < nPK2) {
            IPeptideSpectrumCluster sc1 = s1.get(t);

            IPeptideSpectrumCluster sc2 = s2.get(e);

            final ISpectrum spc1 = sc1.getConsensusSpectrum();
            final ISpectrum spc2 = sc2.getConsensusSpectrum();

            double mz1 = sc1.getPrecursorMz();
            if (mz1 < oldSC1MZ)
                throw new IllegalStateException("Clusters must be sorted by MZ");
            oldSC1MZ = mz1;

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
                } else {
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
                } else {
                    if (lastIsT) {
                        e++;
                        lastIsT = false;
                    } else {
                        t++;
                        lastIsT = true;
                    }

                }
            } else {
                if (mass_difference < 0) {
                    alreadyMatched.clear();
                    e++;
                } else {
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
