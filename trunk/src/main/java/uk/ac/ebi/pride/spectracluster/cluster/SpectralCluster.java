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
public class SpectralCluster implements ISpectralCluster,  Equivalent<ISpectralCluster> {

    private final String id;
    private ISpectrum consensusSpectrum;
    private final List<ISpectrum> clusteredSpectra = new ArrayList<ISpectrum>();
    private boolean dirty;
    private final ConsensusSpectrumBuilder consensusSpectrumBuilder;

    public SpectralCluster(String id ) {
        this(id, Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder());
    }

    public SpectralCluster(String id, ConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.id = id;
        this.consensusSpectrum = null;
        this.dirty = false;
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
    }

    public String getId() {
        return id;
    }

    public double getPrecursorMz() {
        return getConsensusSpectrum().getPrecursorMz();
    }

    public double getPrecursorCharge() {
        return getConsensusSpectrum().getPrecursorCharge();
    }

    public List<IPeak> getPeaks() {
        return getConsensusSpectrum().getPeaks();
    }


    public ISpectrum getConsensusSpectrum() {
        guaranteeClean();
        return consensusSpectrum;
    }

    public List<ISpectrum> getClusteredSpectra() {
        guaranteeClean();
        return new ArrayList<ISpectrum>(clusteredSpectra);
    }

    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }

    public void addSpectra(ISpectrum... merged) {
        if (merged != null && merged.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToMerge : merged) {
//                if (spectrumToMerge instanceof ISpectralCluster) {
//                    Collection<ISpectrum> nestedSpectra = ((ISpectralCluster) spectrumToMerge).getClusteredSpectra();
//                    for (ISpectrum nestedSpectrum : nestedSpectra) {
//                        if (!clusteredSpectra.contains(spectrumToMerge)) {
//                            clusteredSpectra.add(nestedSpectrum);
//                        }
//                    }
//
//                }
//                else
                if (!clusteredSpectra.contains(spectrumToMerge)) {
                    clusteredSpectra.add(spectrumToMerge);
              }
            }
        }
    }

    public void removeSpectra(ISpectrum... removed) {
        if (removed != null && removed.length > 0) {
            dirty = true;

            for (ISpectrum spectrumToRemove : removed) {
//                if (spectrumToRemove instanceof ISpectralCluster) {
//                    Collection<ISpectrum> nestedSpectra = ((ISpectralCluster) spectrumToRemove).getClusteredSpectra();
//                    clusteredSpectra.removeAll(nestedSpectra);
//                } else {
                    clusteredSpectra.remove(spectrumToRemove);
//                }
            }
        }
    }

    private void guaranteeClean() {
        if (dirty) {
            consensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(clusteredSpectra);
            dirty = false;
        }
    }

    /**
     * sort by mz - might be useful
     *
     * @param o
     * @return
     */
    public int compareTo(ISpectralCluster o) {
        if (o == this)
            return 0;
        if (getPrecursorMz() != o.getPrecursorMz()) {
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
     * @param o poiibly null other object
     * @return true if other is "similar enough to this"
     */
    public boolean equivalent(ISpectralCluster o) {
        if (o == this)
            return true;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return false;
        if (getClusteredSpectraCount() == 1) {     //  todo Rui - make the algorithm
            double del = o.getPrecursorMz() - getPrecursorMz();
            double abs = Math.abs(del);
            if (abs > IPeak.SMALL_MZ_DIFFERENCE) {
                return false;
            }

        }
        List<ISpectrum> spc1 = getClusteredSpectra();
        List<ISpectrum> spc2 = o.getClusteredSpectra();
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
            throw new UnsupportedOperationException("Fix This"); // ToDo
//
//                for (int i = 0; i < spc1.size(); i++) {
//                    Equivalent<ISpectrum> pk0 = (Equivalent<ISpectrum>) spc1;
//                    ISpectrum pk1 = spc2[i];
//                    boolean test = !pk0.equivalent(pk1);
//                    if (test)
//                        return false;
//                }
//                return true;
        }
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
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
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * do not add begin and end cluster - useful for rebuilding a mgf
     *
     * @param out
     */
    public void appendSpectra(Appendable out) {
        List<ISpectrum> clusteredSpectra = getClusteredSpectra();
        for (ISpectrum cs : clusteredSpectra) {
            cs.appendMGF(out);  // single spectgra become mgfs

        }
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
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
