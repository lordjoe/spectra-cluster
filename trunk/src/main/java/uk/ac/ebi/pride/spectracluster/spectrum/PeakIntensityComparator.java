package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.Comparator;

/**
 * Comparator to compare peaks by intensity first then mz rather than the
 * standard mz then intensity
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public final class PeakIntensityComparator implements Comparator<IPeak> {

    private static PeakIntensityComparator instance = new PeakIntensityComparator();

    private PeakIntensityComparator() {

    }

    public static PeakIntensityComparator getInstance() {
        return instance;
    }

    public int compare(IPeak o1, IPeak o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }

        if (o2 == null) {
            return 1;
        }

        if (o1.getIntensity() != o2.getIntensity()) {
            return o2.getIntensity() < o1.getIntensity() ? -1 : 1;
        }

        return 0;
    }
}
