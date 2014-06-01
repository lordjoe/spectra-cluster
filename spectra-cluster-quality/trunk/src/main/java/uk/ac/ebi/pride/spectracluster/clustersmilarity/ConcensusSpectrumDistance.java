package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ConcensusSpectrumDistance
 * User: Steve
 * Date: 6/17/13
 */
public class ConcensusSpectrumDistance implements IClusterDistance {

    public static final int NUMBER_COMPARED_PEAKS = 32;
    public static final double MZ_EQUIVALENT_RANGE = 0.3;

    public static final String VERSION = "0.1";

    public static final ConcensusSpectrumDistance INSTANCE = new ConcensusSpectrumDistance();

    private ConcensusSpectrumDistance() {
    }

    /**
     * return a name which should not change
     *
     * @return !null name
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * return a version number - this may be updated over time
     *
     * @return !null version
     */
    @Override
    public String getCurrentVersion() {
        return VERSION;
    }


    /**
     * measure the distance between two clusters - a value of 0 says the clusters are the same or equivalent.
     * a cluster will always have a distance of 0 with itself
     *
     * @param c1 !null cluster
     * @param c2 !null cluster
     * @return distance >= 0
     */
    @Override
    public double distance(final ICluster c1, final ICluster c2) {
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
            if (Math.abs(mass_difference) <= MZ_EQUIVALENT_RANGE / 3) {
                ret -= 2; // match
                int shouldBe1 = (TotakPeaks - 2) - 2 * e;

                // debugging code only important when they should be the same
                if (ret != shouldBe1)
                    shouldBe1 = (TotakPeaks - 2) - 2 * e; // break here
                int shouldBe2 = (TotakPeaks - 2) - 2 * t;
                if (ret != shouldBe2)
                    shouldBe2 = (TotakPeaks - 2) - 2 * t; // break here
            }
            if (mass_difference == 0) {     // choose the last to increment
                if (lastIsT) {
                    e++;
                    lastIsT = false;
                } else {
                    t++;
                    lastIsT = true;
                }
            } else {
                if (mass_difference < 0) {
                    e++;
                    lastIsT = false;
                } else {
                    t++;
                    lastIsT = true;
                }
            }
        }
        return (double) ret / (double) TotakPeaks; // hould be 0..1

    }

    @Override
    public double getMinimalMatchDistance() {
        return DEFAULT_MINIMAL_DISTANCE;
    }
}
