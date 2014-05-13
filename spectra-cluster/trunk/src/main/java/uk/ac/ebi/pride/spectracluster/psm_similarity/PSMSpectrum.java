package uk.ac.ebi.pride.spectracluster.psm_similarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.io.IOException;
import java.math.BigInteger;
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

    /**
     * an optimization for computing distance based on dot product
     *
     * @return number > 0
     */
    @Override
    public double getSelfDotProduct() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public void appendMGF(Appendable out) {
        throw new UnsupportedOperationException("Fix This");
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
    public int[] asMajorPeakMZs() {
        throw new UnsupportedOperationException("Fix This");
    }


    @Override
    public ISpectralCluster asCluster() {
        throw new UnsupportedOperationException("Fix This");

    }

    @Override
    public IPeaksSpectrum getHighestNPeaks(int numberRequested) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public INormalizedSpectrum asNormalizedTo(double totalIntensity) {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public BigInteger asMajorPeakBits() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public IPeaksSpectrum asMajorPeaks() {
        throw new UnsupportedOperationException("Fix This");
    }

    @Override
    public boolean containsMajorPeak(int mz) {
        throw new UnsupportedOperationException("Fix This");
    }

    /**
     * write as MGF
     *
     * @param out
     */
    @Override
    public void appendMSF(final Appendable out) {
        throw new UnsupportedOperationException("Fix This");
    }

    /**
     * write as MGF
     *
     * @param out
     */
    @Override
    public void appendSPText(final Appendable out) {
        throw new UnsupportedOperationException("Fix This");


    }

    /**
     * write basic data as TSV
     *
     * @param out
     */
    @Override
    public void appendTSV(final Appendable out) {
        try {
            out.append(getId());
            out.append("\t");
            out.append(Integer.toString(getPrecursorCharge()));
            out.append("\t");
            String mzString = String.format("%10.2f", getPrecursorMz()).trim();
            out.append(mzString);
            out.append("\t");
            out.append(getPeptide());
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
