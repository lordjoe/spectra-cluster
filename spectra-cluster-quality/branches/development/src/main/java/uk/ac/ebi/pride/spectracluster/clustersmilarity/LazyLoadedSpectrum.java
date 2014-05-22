package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.spectrum.IDecoyPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class LazyLoadedSpectrum implements IDecoyPeptideSpectrumMatch {
    private final String id;
    private final uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever retriever;
    private IPeptideSpectrumMatch internalSpectrum;
    private Double selfDotProduct;

    public LazyLoadedSpectrum(String id, uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever retriever) {
        this.id = id;
        this.retriever = retriever;
    }

    public uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever getRetriever() {
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
        PSMSpectrum psm = PSMSpectrum.getSpectrum(getId());
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
        if (!getId().equals(other.getId()))
            return false;
        if (getRetriever() != null)
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

    @Override
    public IQualityScorer getQualityScorer() {
        return getInternalSpectrum().getQualityScorer();
    }

    /**
     * return scored peptide - maybe null
     */
    @Override
    public String getPeptide() {
        if (getRetriever() == null)
            return null;
        IPeptideSpectrumMatch internalSpectrum1 = getInternalSpectrum();
        if (internalSpectrum1 == null)
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
    public int[] asMajorPeakMZs(int majorPeakCount) {
        return getInternalSpectrum().asMajorPeakMZs(majorPeakCount);
    }

    protected IPeptideSpectrumMatch getInternalSpectrum() {
        if (internalSpectrum == null) {
            uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever retriever1 = getRetriever();
            internalSpectrum = (IPeptideSpectrumMatch) retriever1.retrieve(getId());
        }
        return internalSpectrum;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void setInternalSpectrum(IPeptideSpectrumMatch internalSpectrumx) {
        internalSpectrum = internalSpectrumx;
    }

    @Override
    public ISpectrum getHighestNPeaks(int numberRequested) {
        return getInternalSpectrum().getHighestNPeaks(numberRequested);
    }

    @Override
    public boolean containsMajorPeak(int mz, int majorPeakCount) {
        return getInternalSpectrum().containsMajorPeak(mz, majorPeakCount);
    }

    public String toString() {
        return getId();
    }

}
