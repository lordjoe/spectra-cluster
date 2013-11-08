package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.InternalSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectrumHolderListener;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

import java.io.IOException;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class LazyLoadedSpectralCluster implements ISpectralCluster{
    private String id;
    private float precursorMz;
    private int precursorCharge = 0;
    private ISpectrum consensusSpectrum;
    private final Set<ISpectrum> clusteredSpectra = new LinkedHashSet<ISpectrum>();
    private final List<String> peptides = new ArrayList<String>();


    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public float getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(float precursorMz) {
        this.precursorMz = precursorMz;
    }

    @Override
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    @Override
    public List<String> getPeptides() {
        return new ArrayList<String>(peptides);
    }

    public void addPeptides(String peptides) {
        String[] parts = peptides.split(",");
        addPeptide(parts);
    }

    public void addPeptide(String ... peptides) {
        this.peptides.addAll(Arrays.asList(peptides));
    }

    public void setPrecursorCharge(int precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    @Override
    public ISpectrum getConsensusSpectrum() {
        return consensusSpectrum;
    }

    public void setConsensusSpectrum(ISpectrum consensusSpectrum) {
        this.consensusSpectrum = consensusSpectrum;
    }

    @Override
    public ISpectrum getHighestQualitySpectrum() {
        throw new UnsupportedOperationException("This method is not supported");
    }

    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        throw new UnsupportedOperationException("This method is not supported");
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }

    /**
     * write out the data as a CGF file
     *
     * @param out place to append
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void append(Appendable out) {

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
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * write out the data as a .clustering file
     *
     * @param out place to append
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void appendClustering(Appendable out) {

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
                StringBuilder sb = new StringBuilder();
                sb.append("SPEC\t");
                String id1 = spec.getId();
                while (id1.startsWith("="))
                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
                sb.append(id1);
                sb.append("\ttrue\n");  // changed to look at output
                String csq = sb.toString();
                out.append(csq);

            }
        } catch (IOException e) {
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
    public boolean containsMajorPeak(int mz) {
        return getConsensusSpectrum().containsMajorPeak(mz);
    }

    @Override
    public void addSpectra(List<ISpectrum> added) {
        clusteredSpectra.addAll(added);
    }

    @Override
    public boolean isStable() {
        return ClusterUtilities.isClusterStable(this);
    }

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
                //noinspection UnusedAssignment
                highestQualitySpectrum1 = getHighestQualitySpectrum();
                throw new IllegalStateException("problem"); // ToDo change
            }

            return highestQualitySpectrum1.getQualityScore() < highestQualitySpectrum2.getQualityScore() ? -1 : 1;
        }

        int hash1 = hashCode();
        int hash2 = o.hashCode();
        if (hash1 != hash2)
            return hash1 < hash2 ? -1 : 0;

        return 0;
    }

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

        List<ISpectrum> spc1 = getClusteredSpectra();
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
        } else {
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

    @Override
    public int[] asMajorPeakMZs() {
        return getConsensusSpectrum().asMajorPeakMZs();
    }

    @Override
    public List<IPeak> getPeaks() {
        return getConsensusSpectrum().getPeaks();
    }

    @Override
    public int getPeaksCount() {
        return getConsensusSpectrum().getPeaksCount();
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        Collections.addAll(clusteredSpectra, merged);
    }

    @Override
    public boolean isRemoveSupported() {
        return false;
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        throw new UnsupportedOperationException("Removal is not supported");
    }

    @Override
    public void addSpectrumHolderListener(SpectrumHolderListener added) {
        throw new UnsupportedOperationException("Add spectrum holder listener is not supported");
    }

    @Override
    public void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        throw new UnsupportedOperationException("Remove spectrum holder listener is not supported");
    }
}
