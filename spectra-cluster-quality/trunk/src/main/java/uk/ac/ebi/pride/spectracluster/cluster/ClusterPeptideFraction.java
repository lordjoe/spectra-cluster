package uk.ac.ebi.pride.spectracluster.cluster;


import uk.ac.ebi.pride.spectracluster.util.CompareTo;

import javax.annotation.Nonnull;

/**
 * @author Steve Lewis
 * @version $Id$
 */

public class ClusterPeptideFraction implements Comparable<ClusterPeptideFraction> {

    @SuppressWarnings("UnusedDeclaration")
    public Comparable<ClusterPeptideFraction> TOP_FIRST = new Comparable<ClusterPeptideFraction>() {
        @Override
        public int compareTo(@Nonnull final ClusterPeptideFraction o) {
            int ret = CompareTo.compare(getPurity(), o.getPurity());
            if (ret == 0)
                return getPeptide().compareTo(o.getPeptide());
            return -ret;
        }
    };

    private final String peptide;
    private final double purity;
    private final boolean decoy;

    public static int number_pure_decoy = 0;

    public ClusterPeptideFraction(String pPeptide, final double pPurity, boolean pDecoy) {
        pPeptide = pPeptide.trim();
//        if(!isValid(pPeptide))  {
//            throw new IllegalArgumentException("bad pepide " + pPeptide);
//        }
        if (pPurity > 1)
            purity = pPurity;  // why could we get this
        else
            purity = pPurity;

        peptide = pPeptide;
        decoy = pDecoy;
        if (!pDecoy)
            return;
        if (pPurity > 0.8) {
            {
                number_pure_decoy++;
            }
        }
    }

    public String getPeptide() {
        return peptide;
    }

    public boolean isDecoy() {
        return decoy;
    }

    public double getPurity() {
        return purity;
    }

    /**
     * sort least pure first
     *
     * @param o other
     * @return
     */
    @Override
    public int compareTo(@Nonnull ClusterPeptideFraction o) {
        int ret = CompareTo.compare(getPurity(), o.getPurity());
        if (ret != 0)
            return ret;
//        double del = getMean() - o.getMean();
//        if (Math.abs(del) > 0.0001) {
//            return del < 0 ? -1 : 1;
//        }
        return getPeptide().compareTo(o.getPeptide());
    }

    @Override
    public String toString() {
        //noinspection ImplicitArrayToString
        return peptide + ":" + String.format("%6.3f", getPurity());
    }
}
