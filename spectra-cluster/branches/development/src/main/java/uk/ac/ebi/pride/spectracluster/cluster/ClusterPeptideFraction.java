package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;

import javax.annotation.*;

/**
 * @author Rui Wang
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
            purity = pPurity;
        else
            purity = pPurity;

        peptide = pPeptide;
        decoy = pDecoy;
        if (pPurity > 0.8 && pDecoy) {
            {
                  number_pure_decoy++;
            }
        }
    }

    public static boolean isValid(final String pPeptide) {
        for (int i = 0; i < pPeptide.length(); i++) {
            char c = pPeptide.charAt(i);
            switch (c) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    break;
                default:
                    return false;
            }
        }
        return true;
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
     * @param o other
     * @return
     */
    @Override
    public int compareTo(@Nonnull ClusterPeptideFraction o) {
        int ret = CompareTo.compare(getPurity(), o.getPurity()) ;
        if(ret != 0)
            return ret;
//        double del = getPurity() - o.getPurity();
//        if (Math.abs(del) > 0.0001) {
//            return del < 0 ? -1 : 1;
//        }
        return getPeptide().compareTo(o.getPeptide());
    }

    @Override
    public String toString() {
        //noinspection ImplicitArrayToString
        return peptide.toString() + ":" + String.format("%6.3f", getPurity());
    }
}
