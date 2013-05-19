package uk.ac.ebi.pride.spectracluster.spectrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * PeptideSepctrumMatch represents a peptide and a spectrum match
 *
 * todo: implement quality measure
 * todo: implement equals and hashcode
 *
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideSpectrumMatch implements IPeptideSpectrumMatch {
    private String id;
    private String peptide;
    private double precursorCharge;
    private double precursorMz;
    private double precursorIntensity;
    /**
     * for calculate similarity between spectra, all peaks should be sorted by intensity
     */
    private final List<IPeak> peaks = new ArrayList<IPeak>();
    private double qualityMeasure;

    public PeptideSpectrumMatch(String id,
                                String peptide,
                                double precursorCharge,
                                double precursorMz,
                                double precursorIntensity,
                                Collection<IPeak> peaks) {
        this.id = id;
        this.peptide = peptide;
        this.precursorCharge = precursorCharge;
        this.precursorMz = precursorMz;
        this.precursorIntensity = precursorIntensity;
        this.qualityMeasure = -1;
        setPeaks(peaks);
    }

    public String getId() {
        return id;
    }

    public String getPeptide() {
        return peptide;
    }

    public double getPrecursorMz() {
        return precursorMz;
    }

    public double getPrecursorIntensity() {
        return precursorIntensity;
    }

    public double getPrecursorCharge() {
        return precursorCharge;
    }

    public List<IPeak> getPeaks() {
        return new ArrayList<IPeak>(peaks);
    }

    public void setPeaks(Collection<IPeak> peaks) {
        this.peaks.clear();
        if (peaks != null) {
            this.peaks.addAll(peaks);
            Collections.sort(this.peaks, PeakIntensityComparator.getInstance());
        }
    }

    public double getQualityMeasure() {
        return qualityMeasure;
    }

    public void setQualityMeasure(double qualityMeasure) {
        this.qualityMeasure = qualityMeasure;
    }
}
