package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrumQuality;

import java.util.Comparator;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class QualityClusterComparator implements Comparator<ISpectralCluster> {

    public static final Comparator<ISpectralCluster> INSTANCE = new QualityClusterComparator();

    private QualityClusterComparator() {
    }


    @Override
    public int compare(ISpectralCluster cluster1, ISpectralCluster cluster2) {
        if (cluster1 == cluster2)
            return 0;
        // first check whether the precursor m/z is different

        // as the m/z ranges are the same check the quality
        ISpectrumQuality q1 = cluster1.getHighestQualitySpectrum();
        ISpectrumQuality q2 = cluster2.getHighestQualitySpectrum();
        // handle a nasty and possibly non existent case
        if (q1 == null) {
            if (q2 == null)
                return Double.compare(cluster1.getPrecursorMz(), cluster2.getPrecursorMz());
            else
                return -1;
        }
        else {
            if (q2 == null)
                return 1;

        }

        double quality1 = q1.getQualityScore();
        double quality2 = q2.getQualityScore();

        double del2 = quality2 - quality1;
        if (del2 == 0)
            return 0;
        return del2 < 0 ? -1 : 1;

    }


}