package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.psm_similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class LazyLoadedSpectrum implements IPeptideSpectrumMatch {
    private final String id;
    private final ISpectrumRetriever retriever;
    private IPeptideSpectrumMatch internalSpectrum;
    private Double selfDotProduct;

    public LazyLoadedSpectrum(String id, ISpectrumRetriever retriever) {
        this.id = id;
        this.retriever = retriever;
    }

    public ISpectrumRetriever getRetriever() {
        return retriever;
    }

    @Override
    public String getId() {
        return id;
    }


    /**
     * true if we know this is a decoy
     *
     * @return
     */
    @Override
    public boolean isDecoy() {
        PSMSpectrum psm = PSMSpectrum.getSpectrum(getId()) ;
        return psm.isDecoy();
    }


    @Override
    public float getPrecursorMz() {
        return getInternalSpectrum().getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        return getInternalSpectrum().getPrecursorCharge();
    }


    /**
     * an optimization for computing distance based on dot product
     *
     * @return number > 0
     */
    @Override
    public double getSelfDotProduct() {
        if (selfDotProduct == null) {
            selfDotProduct = Defaults.INSTANCE.getDefaultSimilarityChecker().assessSimilarity(this,this);
        }
        return selfDotProduct;
    }

    @Override
    public void appendMGF(Appendable out) {
        getInternalSpectrum().appendMGF(out);
    }

    @Override
    public double getTotalIntensity() {
        return getInternalSpectrum().getTotalIntensity();
    }

    @Override
    public double getSumSquareIntensity() {
        return getInternalSpectrum().getSumSquareIntensity();
    }

    @Override
    public boolean equivalent(ISpectrum other) {
        if(!getId().equals(other.getId()))
            return false;
          if(getRetriever() != null )
            return getInternalSpectrum().equivalent(other);
        else
            return true;
    }

    @Override
    public List<IPeak> getPeaks() {
        return getInternalSpectrum().getPeaks();
    }

    @Override
    public int getPeaksCount() {
        return getInternalSpectrum().getPeaksCount();
    }

    @Override
    public int compareTo(ISpectrum o) {
        return getInternalSpectrum().compareTo(o);
    }

    @Override
    public double getQualityScore() {
        return getInternalSpectrum().getQualityScore();
    }

    /**
     * return scored peptide - maybe null
     */
    @Override
    public String getPeptide() {
        if(getRetriever() == null)
            return null;
        IPeptideSpectrumMatch internalSpectrum1 = getInternalSpectrum();
        if(internalSpectrum1 == null)
            return null;
        return internalSpectrum1.getPeptide();
    }

    /**
     * return text in the id not peptide or id
     */
    @Override
    public String getAnnotation() {
        return getInternalSpectrum().getAnnotation();
    }

    @Override
    public int[] asMajorPeakMZs() {
        return getInternalSpectrum().asMajorPeakMZs();
    }

    protected IPeptideSpectrumMatch getInternalSpectrum() {
        if (internalSpectrum == null) {
            ISpectrumRetriever retriever1 = getRetriever();
            internalSpectrum = (IPeptideSpectrumMatch) retriever1.retrieve(getId());
        }
        return internalSpectrum;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void setInternalSpectrum(IPeptideSpectrumMatch internalSpectrumx) {
        internalSpectrum = internalSpectrumx;
    }

    @Override
    public ISpectralCluster asCluster() {
        return getInternalSpectrum().asCluster();
    }

    @Override
    public IPeaksSpectrum getHighestNPeaks(int numberRequested) {
        return getInternalSpectrum().getHighestNPeaks(numberRequested);
    }

    @Override
    public INormalizedSpectrum asNormalizedTo(double totalIntensity) {
        return getInternalSpectrum().asNormalizedTo(totalIntensity);
    }

    @Override
    public BigInteger asMajorPeakBits() {
        return getInternalSpectrum().asMajorPeakBits();
    }

    @Override
    public IPeaksSpectrum asMajorPeaks() {
        return getInternalSpectrum().asMajorPeaks();
    }

    @Override
    public boolean containsMajorPeak(int mz) {
        return getInternalSpectrum().containsMajorPeak(mz);
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

    public String toString()
    {
        return getId();
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
            String mzString = String.format("%10.2f",getPrecursorMz()).trim();
            out.append(mzString);
            out.append("\t");
            out.append(getPeptide());
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

      }


}
