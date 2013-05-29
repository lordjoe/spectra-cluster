package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.quality.QualityScorer;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrumQuality;

import java.util.Comparator;

/**
 *
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

    private QualityScorer qualityScorer;

    public ClusterComparator(QualityScorer qualityScorer) {
        this.qualityScorer = qualityScorer;
    }

    @Override
    public int compare(ISpectralCluster cluster1, ISpectralCluster cluster2) {
        // first check whether the precursor m/z is different
        if (cluster1.getPrecursorMz() - PRECURSOR_RANGE > cluster2.getPrecursorMz()) {
            return -1;
        }

        if (cluster1.getPrecursorMz() + PRECURSOR_RANGE < cluster2.getPrecursorMz())
            return 1;

        // as the m/z ranges are the same check the quality
        double quality1 = calculateSpectrumQualityScore(cluster1.getConsensusSpectrum());
        double quality2 = calculateSpectrumQualityScore(cluster2.getConsensusSpectrum());

        if (quality1 > quality2)
            return -1;
        if (quality1 < quality2)
            return 1;

        return 0;
    }

    private double calculateSpectrumQualityScore(ISpectrum spectrum) {
        double qualityScore = spectrum.getQualityScore();

        if (qualityScore == ISpectrumQuality.BAD_QUALITY_MEASURE) {
            qualityScore = qualityScorer.calculateQualityScore(spectrum);
            spectrum.setQualityScore(qualityScore);
        }

        return qualityScore;
    }
}
