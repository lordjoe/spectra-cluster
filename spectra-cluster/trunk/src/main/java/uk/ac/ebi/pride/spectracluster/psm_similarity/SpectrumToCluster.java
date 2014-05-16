package uk.ac.ebi.pride.spectracluster.psm_similarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.IOException;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.psm_similarity.SpectrumToCluster
 * used for reporting duplicated spectra and similar clusters
 * User: Steve
 * Date: 3/28/14
 */
public class SpectrumToCluster {

    public static final int MAX_DUPLICATED_REPORTED = 16;

    private final Map<String, List<IPeptideSpectrumCluster>> spectrumIdToCluster = new HashMap<String, List<IPeptideSpectrumCluster>>();


    public void addClusters(List<IPeptideSpectrumCluster> added) {
        for (IPeptideSpectrumCluster sc : added) {
            addCluster(sc);
        }
    }

    public void addCluster(IPeptideSpectrumCluster added) {
        for (ISpectrum sc : added.getClusteredSpectra()) {
            addSpectrum(sc, added);
        }
    }

    private void addSpectrum(final ISpectrum pSc, final IPeptideSpectrumCluster pAdded) {
        String id = pSc.getId();
        if (spectrumIdToCluster.containsKey(id)) {
            spectrumIdToCluster.get(id).add(pAdded); // this cluster as well
        } else {
            List<IPeptideSpectrumCluster> clusters = new ArrayList<IPeptideSpectrumCluster>();
            clusters.add(pAdded);
            spectrumIdToCluster.put(id, clusters);
        }
    }


    public int[] getDuplicatedOfSize() {
        int[] ret = new int[MAX_DUPLICATED_REPORTED];
        for (String id : spectrumIdToCluster.keySet()) {
            int numberDuplicates = spectrumIdToCluster.get(id).size();
            if (numberDuplicates < MAX_DUPLICATED_REPORTED)
                ret[numberDuplicates]++;
        }
        return ret;
    }


    public void appendDuplicatedSpectra(Appendable out) {
        int[] dups = getDuplicatedOfSize();
        for (int i = dups.length - 1; i > 0; i--) {
            int dup = dups[i];
            if (dup > 0) {
                appendDuplicatedSpectraOfSize(i, out);
            }

        }
    }

    private void appendDuplicatedSpectraOfSize(final int pDup, final Appendable out) {
        for (String id : spectrumIdToCluster.keySet()) {
            List<IPeptideSpectrumCluster> clusters = spectrumIdToCluster.get(id);
            int numberDuplicates = clusters.size();
            if (numberDuplicates == pDup)
                appendDuplicatedSpectraOfSize(clusters, out);
        }
    }

    /**
     * how many duplicates wthin clusters
     *
     * @param clusters
     * @param pOut
     */
    private void appendDuplicatedSpectraOfSize(List<IPeptideSpectrumCluster> clusters, final Appendable out) {

        List<List<String>> holder = new ArrayList<List<String>>();

        for (IPeptideSpectrumCluster cluster : clusters) {
            Set<String> spectralIds = cluster.getSpectralIds();
            List<String> ids = new ArrayList<String>(spectralIds);
            Collections.sort(ids);
            holder.add(ids);
        }
        try {
            for (List<String> strings : holder) {
                for (String s : strings) {
                    out.append(" ");
                    out.append(s);
                }
                out.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


}
