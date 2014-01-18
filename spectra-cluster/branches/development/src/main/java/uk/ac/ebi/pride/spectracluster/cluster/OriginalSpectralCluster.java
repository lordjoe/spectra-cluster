package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.OriginalSpectralCluster
 * User: jg
 * Date: 7/30/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class OriginalSpectralCluster implements ISpectralCluster {
    private final IConsensusSpectrumBuilder consensusSpectrum = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
    private List<ISpectrum> spectra = new ArrayList<ISpectrum>();
    private boolean stable;

    public OriginalSpectralCluster(ISpectralCluster cluster) {
        spectra.addAll(cluster.getClusteredSpectra());
        consensusSpectrum.addSpectra(cluster.getClusteredSpectra().toArray(new ISpectrum[cluster.getClusteredSpectra().size()]));
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public float getPrecursorMz() {
        return consensusSpectrum.getConsensusSpectrum().getPrecursorMz();
    }

    @Override
    public int getPrecursorCharge() {
        return consensusSpectrum.getConsensusSpectrum().getPrecursorCharge();
    }

    @Override
    public List<String> getPeptides() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public String getMostCommonPeptide() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    @Nonnull
    @Override
    public List<ClusterPeptideFraction> getPeptidePurity() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
     }

    @Override
    public ISpectrum getConsensusSpectrum() {
        return consensusSpectrum.getConsensusSpectrum();
    }

    @Override
    public ISpectrum getHighestQualitySpectrum() {
        // TODO change to proper function

        if (spectra.size() < 1)
            return null;

        return spectra.get(0);
    }

    /**
     * return a set of all ids
     *
     * @return
     */
    @Override
    public Set<String> getSpectralIds() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        return null;
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        return new ArrayList<ISpectrum>(spectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return consensusSpectrum.getSpectraCount();
    }


    /**
     * make a one line report
     * @param out
     */
    @Override
    public void appendData(Appendable out) {
        try {
            out.append(getId());
            out.append("\t");

            String mz = String.format("%f8.2", getPrecursorMz());
            out.append(mz);
            out.append("\t");

            out.append(Integer.toString(getPrecursorCharge()));
            out.append("\t");


        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }


    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void append(Appendable out) {
        //noinspection UnusedDeclaration
        int indent = 0;

        try {
            out.append("BEGIN CLUSTER");
            out.append(" Id=" + getId());
            out.append(" Charge=" + getPrecursorCharge());

            out.append("\n");

            appendSpectra(out);
            out.append("END CLUSTER");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void appendSpectra(Appendable out) {
        List<ISpectrum> clusteredSpectra = getClusteredSpectra();
        for (ISpectrum cs : clusteredSpectra) {
            cs.appendMGF(out);  // single spectgra become mgfs

        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void appendClustering(Appendable out) {
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration
        int indent = 0;

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(getClusteredSpectra()) + "]");
            out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(getConsensusSpectrum()));
            out.append("\n");

            for (ISpectrum spec : getClusteredSpectra()) {
                out.append("SPEC\t");
                out.append(spec.getId());
                out.append("\ttrue\n");

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }



    @Override
    public boolean containsMajorPeak(int mz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSpectra(List<ISpectrum> added) {
        addSpectra(added.toArray(new ISpectrum[added.size()]));
    }


    /**
     * stable clusters do not support remove others do
     *
     * @return as above
     */
    @Override
    public boolean isRemoveSupported() {
        return !isStable();
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isStable() {
        return stable;
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    @Override
    public boolean isSemiStable() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return false;
    }

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @param stable as
     */
    public void setStable(boolean stable) {
        this.stable = stable;
    }



    @Override
    /**
     * Sort according to m/z.
     */
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

        return 0;
    }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(ISpectralCluster other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] asMajorPeakMZs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IPeak> getPeaks() {
        return new ArrayList<IPeak>(consensusSpectrum.getConsensusSpectrum().getPeaks());
    }

    @Override
    public int getPeaksCount() {
        return consensusSpectrum.getConsensusSpectrum().getPeaks().size();
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        // spectra can deliberately added more than once
        consensusSpectrum.addSpectra(merged);
        //noinspection ManualArrayToCollectionCopy
        for (ISpectrum spectrum : merged) {
            spectra.add(spectrum);
        }
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        consensusSpectrum.removeSpectra(removed);
        for (ISpectrum spectrum : removed)
            spectra.remove(spectrum);
    }

    @Override
    public void addSpectrumHolderListener(SpectrumHolderListener added) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        throw new UnsupportedOperationException();
    }
}
