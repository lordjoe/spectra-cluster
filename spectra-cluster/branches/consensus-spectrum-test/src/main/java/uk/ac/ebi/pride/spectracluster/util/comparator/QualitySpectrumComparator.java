package uk.ac.ebi.pride.spectracluster.util.comparator;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Comparator;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class QualitySpectrumComparator implements Comparator<ISpectrum> {

    public static final Comparator<ISpectrum> INSTANCE = new QualitySpectrumComparator();

    private QualitySpectrumComparator() {
    }


    /**
     * sort with the highest quality spectra first
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    @Override
    public int compare(ISpectrum cluster1, ISpectrum cluster2) {
        if (cluster1 == cluster2)
            return 0;

        double q1 = cluster1.getQualityScore();
        double q2 = cluster2.getQualityScore();
        return Double.compare(q2, q1);

    }


}