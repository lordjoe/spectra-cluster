package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * A simple spectrum used when creating TSV files - this allows many spectra to be in memory without
 * growing it alot
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SimplifiedSpectrum implements IPeptideSpectrumMatch {
    private final String id;
    private final String peptide;
    private final int precursorCharge;
    private final float precursorMz;

    public SimplifiedSpectrum(IPeptideSpectrumMatch copy) {
        id = copy.getId();
        peptide = copy.getPeptide();
        precursorCharge = copy.getPrecursorCharge();
        precursorMz = copy.getPrecursorMz();
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


    /**
     * true if we know this is a decoy
     *
     * @return
     */
    @Override
    public boolean isDecoy() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    @Override
    public void appendMGF(Appendable out) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * an optimization for computing distance based on dot product
     *
     * @return number > 0
     */
    @Override
    public double getSelfDotProduct() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public double getTotalIntensity() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public double getSumSquareIntensity() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public boolean equivalent(ISpectrum other) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public List<IPeak> getPeaks() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public int getPeaksCount() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public int compareTo(ISpectrum o) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public double getQualityScore() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
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
