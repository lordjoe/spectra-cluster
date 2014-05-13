package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.IOException;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.SpectrumToCluster
 * used for reporting duplicated spectra and similar clusters
 * User: Steve
 * Date: 3/28/14
 */
public class SpectrumToCluster {

    public static final int MAX_DUPLICATED_REPORTED = 16;

    private final Map<String, List<ISpectralCluster>> spectrumIdToCluster = new HashMap<String, List<ISpectralCluster>>();


    public void addClusters(List<ISpectralCluster> added) {
        for (ISpectralCluster sc : added) {
            addCluster(sc);
        }
    }

    public void addCluster(ISpectralCluster added) {
        for (ISpectrum sc : added.getClusteredSpectra()) {
            addSpectrum(sc, added);
        }
    }

    private void addSpectrum(final ISpectrum pSc, final ISpectralCluster pAdded) {
        String id = pSc.getId();
        if (spectrumIdToCluster.containsKey(id)) {
            spectrumIdToCluster.get(id).add(pAdded); // this cluster as well
        } else {
            List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
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
            List<ISpectralCluster> clusters = spectrumIdToCluster.get(id);
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
    private void appendDuplicatedSpectraOfSize(List<ISpectralCluster> clusters, final Appendable out) {

        List<List<String>> holder = new ArrayList<List<String>>();

        for (ISpectralCluster cluster : clusters) {
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
