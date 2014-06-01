package uk.ac.ebi.pride.spectracluster.hadoop.datastore;

import uk.ac.ebi.pride.spectracluster.engine.PeakMatchClusteringEngine;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Comparator;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.SpectrumClusteringData
 * Structure to hold spectral sorting data that are computed without holding the entire spectrum
 * by default sorts on mz then quality then id
 * <p/>
 * User: Steve
 * Date: 7/19/13
 */
public class SpectrumClusteringData implements Comparable<SpectrumClusteringData> {

    @SuppressWarnings("UnusedDeclaration")
    public static final Comparator<SpectrumClusteringData> QUALITY_COMPARATOR = new QualitySpectrumClusteringDataComparator();

    /**
     * comparator sorting on quality them mz rather than the default charge, mz then quality
     */
    private static class QualitySpectrumClusteringDataComparator implements Comparator<SpectrumClusteringData> {
        private QualitySpectrumClusteringDataComparator() {
        }


        @Override
        public int compare(final SpectrumClusteringData o1, final SpectrumClusteringData o) {
            if (o1 == o)
                return 0;
            int ret = Float.compare(o1.getQuality(), o.getQuality());
            if (ret != 0)
                return ret;
            if (o1.getCharge() != o.getCharge())
                return o1.getCharge() < o.getCharge() ? -1 : 1;
            ret = Float.compare(o1.getPrecursor_mz(), o.getPrecursor_mz());
            if (ret != 0)
                return ret;

            return o1.getId().compareTo(o.getId());
        }
    }

    private final String id;
    private final float quality;
    private final int charge;
    private final float precursor_mz;
    private final int[] top_peaks;

    public SpectrumClusteringData(ISpectrum spec) {
        id = spec.getId();
        charge = spec.getPrecursorCharge();
        quality = (float) spec.getQualityScore();
        precursor_mz = spec.getPrecursorMz();
        // TODO JG: this might not be an ideal solution
        top_peaks = spec.asMajorPeakMZs(PeakMatchClusteringEngine.MAJOR_PEAK_NUMBER);
    }

    public SpectrumClusteringData(final String pId, final float pQuality, int precursorCharge, final float pPrecursor_mz, final int[] pTop_peaks) {
        id = pId;
        quality = pQuality;
        charge = precursorCharge;
        precursor_mz = pPrecursor_mz;
        top_peaks = pTop_peaks;
    }

    public String getId() {
        return id;
    }

    public float getQuality() {
        return quality;
    }

    public float getPrecursor_mz() {
        return precursor_mz;
    }

    public int[] getTop_peaks() {
        return top_peaks;
    }

    public int getCharge() {
        return charge;
    }

    @Override
    public int compareTo(final SpectrumClusteringData o) {
        if (this == o)
            return 0;
        int ret = 0;
        if (this.getCharge() != o.getCharge())
            return this.getCharge() < o.getCharge() ? -1 : 1;
        ret = Float.compare(getPrecursor_mz(), o.getPrecursor_mz());
        if (ret != 0)
            return ret;
        ret = Float.compare(getQuality(), o.getQuality());
        if (ret != 0)
            return ret;

        return getId().compareTo(o.getId());
    }
}
