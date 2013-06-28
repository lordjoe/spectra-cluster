package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterContentDistance
 * User: Steve
 * Date: 6/17/13
 */
public class ClusterContentDistance implements ClusterDistance {

    public static final double MAX_DISTANCE = 1;
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
        final ISpectrum s1 = c1.getHighestQualitySpectrum();
        final ISpectrum s2 = c2.getHighestQualitySpectrum();

        // do not even share highest quality
        if(!s1.equivalent(s2))  {
            return MAX_DISTANCE;
         }

        final List<ISpectrum> sp1 = c1.getClusteredSpectra(); // these are by mz
        final List<ISpectrum> sp2 = c2.getClusteredSpectra(); // these are by mz


         final int totalSpectra = sp1.size() + sp2.size();
         double ret = totalSpectra;

        boolean lastIsT = false;
        int t = 0;
        int e = 0;
        while (t < sp1.size() && e < sp2.size()) {
            ISpectrum spectrum1 = sp1.get(t);
            double mz1 = spectrum1.getPrecursorMz();
            ISpectrum spectrum2 = sp2.get(e);
            double mz2 = spectrum2.getPrecursorMz();

            double mass_difference = mz2 - mz1;
            if (Math.abs(mass_difference) <= MZ_EQUIVALENT_RANGE) {
                if(spectrum1.equivalent(spectrum2))
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
        return ret / totalSpectra; // should be 0..1

    }
}
