package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
   *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparator implements Comparator<ISpectralCluster> {

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
        if (del2 == 0)   {
            return 0;
        }

        return del2 < 0 ? -1 : 1;
    }


}
