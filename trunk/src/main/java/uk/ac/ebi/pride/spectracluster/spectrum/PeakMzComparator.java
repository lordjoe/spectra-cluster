package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.Comparator;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeakMzComparator implements Comparator<IPeak> {

    private static PeakMzComparator instance = new PeakMzComparator();

    private PeakMzComparator() {

    }

    public static PeakMzComparator getInstance() {
        return instance;
    }

    public int compare(IPeak o1, IPeak o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }

        if (o2 == null) {
            return 1;
        }

        if (o1.getMz() != o2.getMz()) {
            return o1.getMz() < o2.getMz() ? -1 : 1;
        }

        return 0;
    }
}