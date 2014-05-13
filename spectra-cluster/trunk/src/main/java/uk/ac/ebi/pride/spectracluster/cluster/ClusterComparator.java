package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Comparator;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparator implements Comparator<ISpectralCluster> {
    /**
     * compare with high quailty at the top
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    @Override
    public int compare(ISpectralCluster cluster1, ISpectralCluster cluster2) {
        if (cluster1 == cluster2)
            return 0;

        // as the m/z ranges are the same check the quality
        ISpectrum consensusSpectrum1 = cluster1.getConsensusSpectrum();
        ISpectrum consensusSpectrum2 = cluster2.getConsensusSpectrum();

        double quality1 = consensusSpectrum1.getQualityScore();
        double quality2 = consensusSpectrum2.getQualityScore();

        return -Double.compare(quality1, quality2);
    }
}
