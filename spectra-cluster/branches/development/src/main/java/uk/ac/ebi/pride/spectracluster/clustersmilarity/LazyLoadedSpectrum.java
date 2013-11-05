package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.INormalizedSpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeaksSpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class LazyLoadedSpectrum implements ISpectrum
{
    private final String id;
    private final ISpectrumRetriever retriever;
    private ISpectrum internalSpectrum;

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

    @Override
    public int[] asMajorPeakMZs() {
        return getInternalSpectrum().asMajorPeakMZs();
    }

    protected ISpectrum getInternalSpectrum() {
        if(internalSpectrum == null)
            internalSpectrum = getRetriever().retrieve(getId());
        return internalSpectrum;
    }

    protected void setInternalSpectrum(ISpectrum internalSpectrum) {
        this.internalSpectrum = internalSpectrum;
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
