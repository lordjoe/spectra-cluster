package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.QualityScorer;

import java.util.Comparator;

/**
 *
 * todo: implement this class
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparator implements Comparator<ISpectralCluster> {

    private QualityScorer qualityScorer;

    public ClusterComparator(QualityScorer qualityScorer) {
        this.qualityScorer = qualityScorer;
    }

    @Override
    public int compare(ISpectralCluster cluster1, ISpectralCluster cluster2) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
