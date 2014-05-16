package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.comparator.QualityClusterComparator;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterListSimilarity
 * User: Steve
 * Date: 6/17/13
 */
public class ClusterContentSimilarity {

    private final IClusterDistance distanceMeasure;

    public ClusterContentSimilarity(IClusterDistance dist) {
        distanceMeasure = dist;
    }

    public ClusterContentSimilarity() {
        this(ClusterContentDistance.INSTANCE);
    }

    public IClusterDistance getDistanceMeasure() {
        return distanceMeasure;
    }

    /**
     * @param s1 - entering lists of spectral clusters - exit unmatched clusters
     * @param s2 - entering lists of spectral clusters - exit unmatched clusters
     * @return matched clusters from set 1
     */
    public List<IPeptideSpectralCluster> identicalClusters(List<IPeptideSpectralCluster> s1, List<IPeptideSpectralCluster> s2) {
        IClusterDistance dm = getDistanceMeasure();

        Collections.sort(s1, QualityClusterComparator.INSTANCE); // sort by hishest quality

        Collections.sort(s2, QualityClusterComparator.INSTANCE); // sort by hishest quality


        List<IPeptideSpectralCluster> identical = new ArrayList<IPeptideSpectralCluster>();
        List<IPeptideSpectralCluster> matched1 = new ArrayList<IPeptideSpectralCluster>();
        List<IPeptideSpectralCluster> matched2 = new ArrayList<IPeptideSpectralCluster>();


        final int nPK1 = s1.size();
        final int nPK2 = s2.size();

        final int TotakPeaks = nPK1 + nPK2;
        double ret = TotakPeaks;

        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        while (t < nPK1 && e < nPK2) {
            IPeptideSpectralCluster sc1 = s1.get(t);
            ISpectrum q1 = sc1.getHighestQualitySpectrum();
            IPeptideSpectralCluster sc2 = s2.get(e);
            ISpectrum q2 = sc2.getHighestQualitySpectrum();

            double quality_difference = q1.getQualityScore() - q2.getQualityScore();

            if (q1 == q2) {
                double requiredDistance = 0;
                if (Math.min(sc1.getClusteredSpectraCount(), sc2.getClusteredSpectraCount()) > 2)
                    requiredDistance = 0.4;
                final double distance = dm.distance(sc1, sc2);
                if (distance <= requiredDistance) {
                    identical.add(new AlternativeSpectralClusters(sc1, sc2));
                    matched1.add(sc1);
                    matched2.add(sc2);
                } else {
                    quality_difference = q1.getQualityScore() - q2.getQualityScore();     // break here
                }
            }
            if (quality_difference == 0) {
                if (lastIsT) {
                    e++;
                    lastIsT = false;
                } else {
                    t++;
                    lastIsT = true;
                }
            } else {
                if (quality_difference < 0) {
                    e++;
                } else {
                    t++;
                }
            }
        }
        s1.removeAll(matched1); // drop matches
        Collections.sort(s1); // sort by mz
        s2.removeAll(matched2); // drop matches
        Collections.sort(s2); // sort by mz
        Collections.sort(identical); // sort by mz
        return identical;
    }
}
