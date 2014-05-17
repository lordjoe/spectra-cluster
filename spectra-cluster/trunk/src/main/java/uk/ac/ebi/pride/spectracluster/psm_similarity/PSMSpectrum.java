package uk.ac.ebi.pride.spectracluster.psm_similarity;

import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PSMSpectrum implements IPeptideSpectrumMatch {

    private static Map<String, PSMSpectrum> spectra = new HashMap<String, PSMSpectrum>();

    /**
     * ONLY way to to get a spectrum
     *
     * @param id
     * @return
     */
    public static synchronized PSMSpectrum getSpectrum(String id) {
        PSMSpectrum ret = spectra.get(id);
        if (ret == null) {
            ret = new PSMSpectrum(id);
            spectra.put(id, ret);
        }
        return ret;
    }


    /**
     * set uret.setPeptide( from a tsv line
     *
     * @param tsv
     * @return
     */
    public static synchronized PSMSpectrum getSpectrumFromLine(String tsv) {
        String[] items = tsv.split("\t");
        if (items.length != 3)
            throw new IllegalArgumentException("should have 3 items");
        String id = items[0].trim();
        PSMSpectrum ret = getSpectrum(id);
        ret.setPeptide(items[1].trim());
        ret.setDecoy("1".equals(items[2].trim()));
        return ret;
    }


    public static synchronized void clearSpectrumCache() {
        spectra.clear();
    }


    private final String id;
    private String peptide;
    private float precursorMz;
    private int precursorCharge;
    private boolean decoy;

    private PSMSpectrum(String id) {
        this.id = id;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public float getPrecursorMz() {
        return precursorMz;
    }

    @Override
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    public void setPrecursorMz(final float pPrecursorMz) {
        precursorMz = pPrecursorMz;
    }

    public void setPrecursorCharge(final int pPrecursorCharge) {
        precursorCharge = pPrecursorCharge;
    }

    public void setPeptide(final String pPeptide) {
        peptide = pPeptide;
    }

    public boolean isDecoy() {
        return decoy;
    }

    public void setDecoy(final boolean pDecoy) {
        decoy = pDecoy;
    }

    @Override
    public double getTotalIntensity() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public double getSumSquareIntensity() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public boolean equivalent(ISpectrum other) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public List<IPeak> getPeaks() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public int getPeaksCount() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public int compareTo(ISpectrum o) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public double getQualityScore() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public IQualityScorer getQualityScorer() {
        throw new UnsupportedOperationException("Fix This");
    }

    /**
     * return scored peptide - maybe null
     */
    @Override
    public String getPeptide() {
        return peptide;
    }

    /**
     * return text in the id not peptide or id
     */
    @Override
    public String getAnnotation() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public int[] asMajorPeakMZs(int majorPeakCount) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public ISpectrum getHighestNPeaks(int numberRequested) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public boolean containsMajorPeak(int mz, int majorPeakCount) {
        throw new UnsupportedOperationException("Fix This");
    }


}
