package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ConcensusSpectrumDistance
 * User: Steve
 * Date: 6/17/13
 */
public class ConcensusSpectrumDistance implements ClusterDistance {

    public static final int NUMBER_COMPARED_PEAKS = 32;
    public static final double MZ_EQUIVALENT_RANGE = 0.3;

    /**
     * measure the distance between two clusters - a value of 0 says the clusters are the same or equivalent.
     * a cluster will always have a distance of 0 with itself
     *
     * @param c1 !null cluster
     * @param c2 !null cluster
     * @return distance >= 0
     */
    @Override
    public double distance(final ISpectralCluster c1, final ISpectralCluster c2) {
        final ISpectrum s1 = c1.getConsensusSpectrum();
        final ISpectrum s2 = c2.getConsensusSpectrum();

        final List<IPeak> peaks1 = s1.getHighestNPeaks(NUMBER_COMPARED_PEAKS).getPeaks();
        final List<IPeak> peaks2 = s2.getHighestNPeaks(NUMBER_COMPARED_PEAKS).getPeaks();
        final int nPK1 = peaks1.size();
        final int nPK2 = peaks2.size();

        final int TotakPeaks = nPK1 + nPK2;
        double ret = TotakPeaks;

        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        while (t < nPK1 && e < nPK2) {
            IPeak peak1 = peaks1.get(t);
            double mz1 = peak1.getMz();
            IPeak peak2 = peaks2.get(e);
            double mz2 = peak2.getMz();

            double mass_difference = mz2 - mz1;
            if (Math.abs(mass_difference) <= MZ_EQUIVALENT_RANGE) {
                ret -= 2; // match
             }
            if (mass_difference == 0) {
                if (lastIsT) {
                    e++;
                    lastIsT = false;
                }
                else {
                    t++;
                    lastIsT = true;
                }
            }
            else {
                if (mass_difference < 0) {
                    e++;
                }
                else {
                    t++;
                }
            }
         }
        return ret / TotakPeaks; // hould be 0..1

    }
}
