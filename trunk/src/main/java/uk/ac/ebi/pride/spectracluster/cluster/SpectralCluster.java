package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectralCluster implements ISpectralCluster, InternalSpectralCluster, Equivalent<ISpectralCluster> {


    private final String id;
    private boolean dirty;
    private PeptideSpectrumMatch consensusSpectrum;
    private final List<ISpectrum> highestQualitySpectra = new ArrayList<ISpectrum>();
    private double lowestClusteredQuality = Double.MIN_VALUE;
    private boolean highestQualitySetChanged;


    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private final ConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(ISpectralCluster copied) {
        this.id = copied.getId();
        this.consensusSpectrum = new PeptideSpectrumMatch(copied.getConsensusSpectrum());
        this.highestQualitySpectra.addAll(copied.getHighestQualitySpectra());
        if (copied instanceof SpectralCluster) {
            this.lowestClusteredQuality = ((SpectralCluster) copied).lowestClusteredQuality;
            this.highestQualitySetChanged = false;
        }
        else {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }
        this.dirty = false;
        this.consensusSpectrumBuilder = copied.getConsensusSpectrumBuilder();
        clusteredSpectra.addAll(copied.getClusteredSpectra());

    }


    public SpectralCluster(String id) {
        this(id, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, ConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;
        this.consensusSpectrum = null;
        this.dirty = false;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public float getPrecursorMz() {
        guaranteeClean();
        ISpectrum consensusSpectrum1 = getConsensusSpectrum();
        if (consensusSpectrum1 == null)
            return 0;
        return consensusSpectrum1.getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        guaranteeClean();
        return getConsensusSpectrum().getPrecursorCharge();
    }

    @Override
    public List<IPeak> getPeaks() {
        guaranteeClean();
        return getConsensusSpectrum().getPeaks();
    }


    @Override
    public ConsensusSpectrumBuilder getConsensusSpectrumBuilder() {
        return consensusSpectrumBuilder;
    }

    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    @Override
    public ISpectrum getHighestQualitySpectrum() {
        guaranteeHighestQuailty();
        if (highestQualitySpectra.isEmpty())
            return null;
        return (highestQualitySpectra.get(0));
    }


    @Override
    public ISpectrum getConsensusSpectrum() {
        guaranteeClean();
        return consensusSpectrum;
    }

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guarantee clean can be byPassed
     *
     * @return exactly the current concensus spectrum
     */
    @Override
    public ISpectrum internalGetConcensusSpectrum() {
        return consensusSpectrum;
    }


    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    @Override
    public boolean containsMajorPeak(final int mz) {
        return consensusSpectrum.containsMajorPeak(mz);
    }

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank etall's suggestion that all spectra in a cluster will share at least one of these
     *
     * @return
     */
    @Override
    public int[] asMajorPeakMZs() {
        final ISpectrum consensusSpectrum1 = getConsensusSpectrum();
        return consensusSpectrum1.asMajorPeakMZs();
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guaranett clean can be byPassed
     *
     * @return
     */
    @Override
    public List<ISpectrum> internalGetClusteredSpectra() {
        return clusteredSpectra;
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        if (merged != null && merged.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToMerge : merged) {
                if (!clusteredSpectra.contains(spectrumToMerge)) {
                    clusteredSpectra.add(spectrumToMerge);
                }
                // track which spectrum in the cluster has the highest quality

                handleQualityInsert(spectrumToMerge);
            }
        }
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        if (removed != null && removed.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToRemove : removed) {
                clusteredSpectra.remove(spectrumToRemove);
                handleQualityRemove(spectrumToRemove);
            }
        }
    }

    public boolean isHighestQualitySetChanged() {
        return highestQualitySetChanged;
    }

    public void setHighestQualitySetChanged(final boolean pHighestQualitySetChanged) {
        highestQualitySetChanged = pHighestQualitySetChanged;
    }

    protected double getLowestClusteredQuality() {
        return lowestClusteredQuality;
    }

    protected void setLowestClusteredQuality(final double pLowestClusteredQuality) {
        lowestClusteredQuality = pLowestClusteredQuality;
    }


    /**
     * should only be called if we remove the highest quality spectrum
     */
    protected void handleQualityInsert(ISpectrum inserted) {
        double quality = inserted.getQualityScore();
        if (highestQualitySpectra.size() < NUMBER_SPECTRA_FOR_CONSENSUS) {
            highestQualitySpectra.add(inserted);
            setLowestClusteredQuality(Math.min(getLowestClusteredQuality(), quality));
            setHighestQualitySetChanged(true);
        }
        else {
            if (quality <= getLowestClusteredQuality())
                return; // worse than  the lowest
            setHighestQualitySetChanged(true);
            highestQualitySpectra.add(inserted);

        }
    }

    /**
     * should only be called if we remove the highest quality spectrum
     */
    protected void handleQualityRemove(ISpectrum inserted) {
        double quality = inserted.getQualityScore();
        if (quality < getLowestClusteredQuality())
            return; // worse than  the lowest
        if (highestQualitySpectra.remove(inserted)) {
            setHighestQualitySetChanged(true);
        }
    }

    /**
     * all internally spectrum
     */
    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        return Collections.unmodifiableList(highestQualitySpectra);
    }

    /**
     * should only be called if we remove the highest quality spectrum
     */
    protected void guaranteeHighestQuailty() {
        if (isHighestQualitySetChanged()) {
            if (highestQualitySpectra.isEmpty()) {
                setLowestClusteredQuality(Double.MAX_VALUE);
                setHighestQualitySetChanged(false);
                return;
            }
            Collections.sort(highestQualitySpectra, QualitySpectrumComparator.INSTANCE); // sort highest quality first
            if (highestQualitySpectra.size() > NUMBER_SPECTRA_FOR_CONSENSUS) {
                List<ISpectrum> retained = new ArrayList<ISpectrum>();
                for (int i = 0; i < NUMBER_SPECTRA_FOR_CONSENSUS; i++) {
                    retained.add(highestQualitySpectra.get(i)); // only keep the top NUMBER_SPECTRA_FOR_CONSENSUS
                }
                highestQualitySpectra.clear();
                highestQualitySpectra.addAll(retained);
            }
            setLowestClusteredQuality(highestQualitySpectra.get(highestQualitySpectra.size() - 1).getQualityScore());
            consensusSpectrum = (PeptideSpectrumMatch) consensusSpectrumBuilder.buildConsensusSpectrum(this);
            setHighestQualitySetChanged(false);
        }
        else {
            if (consensusSpectrum == null)
                consensusSpectrum = (PeptideSpectrumMatch) consensusSpectrumBuilder.buildConsensusSpectrum(this);
        }
    }


    protected void guaranteeClean() {
        if (dirty) {
            guaranteeHighestQuailty();
            dirty = false;
        }
    }

    /**
     * sort by mz - might be useful
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ISpectralCluster o) {
        if (o == this)
            return 0;
        if (getPrecursorMz() != o.getPrecursorMz()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (getPrecursorCharge() != o.getPrecursorCharge()) {
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;
        }
        if (o.getClusteredSpectraCount() != getClusteredSpectraCount()) {
            return getClusteredSpectraCount() < o.getClusteredSpectraCount() ? -1 : 1;
        }

        ISpectrum highestQualitySpectrum1 = getHighestQualitySpectrum();
        ISpectrum highestQualitySpectrum2 = o.getHighestQualitySpectrum();
        if (highestQualitySpectrum1 != highestQualitySpectrum2) {
            if (highestQualitySpectrum1 == null || highestQualitySpectrum2 == null) {
                highestQualitySpectrum1 = getHighestQualitySpectrum();
                throw new IllegalStateException("problem"); // ToDo change
            }

            return highestQualitySpectrum1.getQualityScore() < highestQualitySpectrum2.getQualityScore() ? -1 : 1;
        }

        if (true)
            System.out.println("Need better compare");
        //          throw new UnsupportedOperationException("Fix This"); // This should never happen
        return 0;
    }


    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o poiibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(ISpectralCluster o) {
        if (o == this)
            return true;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return false;
        double del = o.getPrecursorMz() - getPrecursorMz();
        double abs = Math.abs(del);
        if (abs > IPeak.SMALL_MZ_DIFFERENCE) {
            return false;
        }

        List<ISpectrum> spc1 = internalGetClusteredSpectra();
        List<ISpectrum> spc2;
        if (o instanceof InternalSpectralCluster)
            spc2 = ((InternalSpectralCluster) o).internalGetClusteredSpectra();  // no copy or clean needed
        else
            spc2 = o.getClusteredSpectra();

        if (spc1.size() != spc2.size()) {
            return false;
        }
        if (spc1.size() <= 1) {

            List<IPeak> peaks = getPeaks();
            List<IPeak> peaks1 = o.getPeaks();
            if (peaks.size() != peaks1.size()) {
                return false;
            }
            for (int i = 0; i < peaks1.size(); i++) {
                IPeak pk0 = peaks.get(i);
                IPeak pk1 = peaks1.get(i);
                if (!pk0.equivalent(pk1))
                    return false;
            }
            return true; // just one spectrum so check peaks
        }
        else {
            if (spc1.size() != spc2.size())
                return false;


            for (int i = 0; i < spc1.size(); i++) {
                ISpectrum pk1 = spc1.get(i);
                ISpectrum pk2 = spc2.get(i);
                boolean test = !pk2.equivalent(pk1);
                if (test)
                    return false;
            }
            return true;
        }
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    @Override
    public void append(Appendable out) {
        int indent = 0;

        try {
            out.append("BEGIN CLUSTER");
            out.append(" Id=" + getId());
            out.append(" Charge=" + getPrecursorCharge());

            out.append("\n");

            appendSpectra(out);
            //            ISpecClusterPeak[] peaks = getPeaks();
            //            for (int i = 0; i < peaks.length; i++) {
            //                ISpecClusterPeak peak = peaks[i];
            //                out.append(peak.toString());
            //                out.append("\n");
            //            }
            out.append("END CLUSTER");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * do not add begin and end cluster - useful for rebuilding a mgf
     *
     * @param out
     */
    @Override
    public void appendSpectra(Appendable out) {
        List<ISpectrum> clusteredSpectra = getClusteredSpectra();
        for (ISpectrum cs : clusteredSpectra) {
            cs.appendMGF(out);  // single spectgra become mgfs

        }
    }

    @Override
    public String toString() {
        double precursorMZ = getPrecursorMz();
        String text =
                "charge= " + getPrecursorCharge() + "," +
                        "mz= " + String.format("%10.3f", precursorMZ).trim() + "," +
                        "count= " + clusteredSpectra.size() +
                        ", spectrum = ";
        for (ISpectrum s : clusteredSpectra)
            text += s.getId() + ",";

        text = text.substring(0, text.length() - 1);
        return text;
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendMGF(Appendable out) {

        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            double precursorCharge = getPrecursorCharge();
            double massChargeRatio = getPrecursorMz();

            out.append("PEPMASS=" + massChargeRatio);
            out.append("\n");

            out.append("CHARGE=" + precursorCharge);
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            for (IPeak peak : getPeaks()) {
                out.append(peak.toString());
                out.append("\n");
            }
            out.append("END IONS");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
