package uk.ac.ebi.pride.spectracluster.util.comparator;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Comparator;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class DefaultClusterComparator implements Comparator<IPeptideSpectralCluster> {
    public static final Comparator<IPeptideSpectralCluster> INSTANCE = new DefaultClusterComparator();

    private DefaultClusterComparator() {
    }

    /**
     * compare with high quailty at the top
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    @Override
    public int compare(IPeptideSpectralCluster cluster1, IPeptideSpectralCluster cluster2) {
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
