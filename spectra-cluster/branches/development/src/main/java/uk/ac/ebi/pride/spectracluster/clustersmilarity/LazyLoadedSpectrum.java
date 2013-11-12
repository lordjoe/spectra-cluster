package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

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

    @Override
    public float getPrecursorMz() {
        return getInternalSpectrum().getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        return getInternalSpectrum().getPrecursorCharge();
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
        return getInternalSpectrum().equivalent(other);
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
        return getInternalSpectrum().getPeptide();
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
}
