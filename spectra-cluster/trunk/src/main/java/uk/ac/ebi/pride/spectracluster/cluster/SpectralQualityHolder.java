package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.comparator.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.SpectralQualityHolder
 * This is a good example of a  SpectrumHolderListener which updates its
 * state when its cluster adds spectra - it tracks the highest quality 20 spectra
 * User: Steve
 * Date: 7/10/13
 */
public class SpectralQualityHolder implements SpectrumHolderListener  {

    // only highest quality spectra used for concensus
    public static final int NUMBER_SPECTRA_FOR_CONSENSUS = 20;

    private final List<ISpectrum> highestQualitySpectra = new ArrayList<ISpectrum>();
    private double lowestClusteredQuality = Double.MIN_VALUE;
    private boolean dirty;

    public SpectralQualityHolder() {
    }


    protected boolean isDirty() {
        return dirty;
    }

    protected void setDirty(final boolean pDirty) {
        dirty = pDirty;
    }

    protected double getLowestClusteredQuality() {
        return lowestClusteredQuality;
    }

    protected void setLowestClusteredQuality(final double pLowestClusteredQuality) {
        lowestClusteredQuality = pLowestClusteredQuality;
    }


    /**
     * should only be called if we insert the highest quality spectrum
     */
    protected void handleQualityInsert(ISpectrum inserted) {
        double quality = inserted.getQualityScore();
        if (highestQualitySpectra.size() < NUMBER_SPECTRA_FOR_CONSENSUS) {
            highestQualitySpectra.add(inserted);
            setLowestClusteredQuality(Math.min(getLowestClusteredQuality(), quality));
            setDirty(true);
        } else {
            if (quality <= getLowestClusteredQuality())
                return; // worse than  the lowest
            setDirty(true);
            highestQualitySpectra.add(inserted);

        }
    }

    /**
     * should only be called if we remove the highest quality spectrum
     */
    protected void handleQualityRemove(ISpectrum removed) {
        double quality = removed.getQualityScore();
        if (quality < getLowestClusteredQuality())
            return; // worse than  the lowest
        if (highestQualitySpectra.remove(removed)) {
            setDirty(true);
        }
    }

    /**
     * all internally spectrum
     */
    public List<ISpectrum> getHighestQualitySpectra() {
        guaranteeClean();
        return Collections.unmodifiableList(highestQualitySpectra);
    }


    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    public ISpectrum getHighestQualitySpectrum() {
        guaranteeClean();
        if (highestQualitySpectra.isEmpty())
            return null;
        return (highestQualitySpectra.get(0));
    }


    /**
     * should only be called if we remove the highest quality spectrum
     */
    protected void guaranteeClean() {
        if (isDirty()) {
            if (highestQualitySpectra.isEmpty()) {
                setLowestClusteredQuality(Double.MAX_VALUE);
                setDirty(false);
                return;
            }

            Collections.sort(highestQualitySpectra, new QualitySpectrumComparator()); // sort highest quality first
            if (highestQualitySpectra.size() > NUMBER_SPECTRA_FOR_CONSENSUS) {
                List<ISpectrum> retained = new ArrayList<ISpectrum>();
                for (int i = 0; i < NUMBER_SPECTRA_FOR_CONSENSUS; i++) {
                    retained.add(highestQualitySpectra.get(i)); // only keep the top NUMBER_SPECTRA_FOR_CONSENSUS
                }
                highestQualitySpectra.clear();
                highestQualitySpectra.addAll(retained);
            }

            setLowestClusteredQuality(highestQualitySpectra.get(highestQualitySpectra.size() - 1).getQualityScore());
            setDirty(false);
        }
    }

    /**
     * handle notification of adding spectra
     *
     * @param holder !null holder
     * @param added  added spectra
     */
    @Override
    public void onSpectraAdd(final ISpectrumHolder holder, final ISpectrum... added) {
        for (ISpectrum add : added) {
            handleQualityInsert(add);
        }
    }

    /**
     * handle notification of removing spectra
     *
     * @param holder  !null holder
     * @param removed removed spectra
     */
    @Override
    public void onSpectraRemove(final ISpectrumHolder holder, final ISpectrum... removed) {
        for (ISpectrum rem : removed) {
            handleQualityRemove(rem);
        }
    }
}
