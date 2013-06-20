package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * todo: implement this class
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparator implements Comparator<ISpectralCluster> {

    /**
     * The range to use when precursor m/z values
     * are considered equal.
     */
    private static final double PRECURSOR_RANGE = 2;

    private final QualityScorer scorer;


    public ClusterComparator(QualityScorer qualityScorer) {
        scorer = qualityScorer;
    }

    public QualityScorer getScorer() {
        return scorer;
    }

    @Override
    public int compare(ISpectralCluster cluster1, ISpectralCluster cluster2) {
        if (cluster1 == cluster2)
            return 0;
        // first check whether the precursor m/z is different
        double del = cluster1.getPrecursorMz() - cluster2.getPrecursorMz();
        if (Math.abs(del) > PRECURSOR_RANGE) {
            return del < 0 ? -1 : 1;
        }

        // as the m/z ranges are the same check the quality
        ISpectrum consensusSpectrum1 = cluster1.getConsensusSpectrum();
        ISpectrum consensusSpectrum2 = cluster2.getConsensusSpectrum();
        if(consensusSpectrum1 == null)
            consensusSpectrum1 = cluster1.getConsensusSpectrum(); // break here
        if(consensusSpectrum2 == null)
             consensusSpectrum2 = cluster2.getConsensusSpectrum(); // break here

        double quality1 = consensusSpectrum1.getQualityScore();
        double quality2 = consensusSpectrum2.getQualityScore();

        double del2 = quality2 - quality1;
        if (del2 == 0)
            return 0;
        return del < 0 ? -1 : 1;

    }


}
