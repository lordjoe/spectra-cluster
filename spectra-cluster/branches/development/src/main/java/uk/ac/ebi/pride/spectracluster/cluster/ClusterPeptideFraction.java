package uk.ac.ebi.pride.spectracluster.cluster;

/**
   *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterPeptideFraction implements Comparable<ClusterPeptideFraction> {

    private final String peptide;
    private final double purity;

    public ClusterPeptideFraction(final String pPeptide, final double pPurity) {
        peptide = pPeptide;
        purity = pPurity;
    }

    public String getPeptide() {
        return peptide;
    }

    public double getPurity() {
        return purity;
    }


    @Override
    public int compareTo(final ClusterPeptideFraction o) {
        int ret = Double.compare(getPurity(), o.getPurity());
        if(ret == 0)
            return getPeptide().compareTo(o.getPeptide());
        return ret;
    }

    @Override
    public String toString() {
        return  peptide +  ":" + String.format("%6.3f",getPurity());
    }
}
