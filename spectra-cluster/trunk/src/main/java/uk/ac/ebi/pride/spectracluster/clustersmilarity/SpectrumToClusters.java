package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumToClusters {
    private final Map<String, List<IPeptideSpectralCluster>> clustersWithSpectra = new HashMap<String, List<IPeptideSpectralCluster>>();
    private int numberOfEmptyCluster = 0;
    private int numberOfDuplicatedSpectra = 0;
    private int numberOfSpectra = 0;

    public int getNumberOfSpectra() {
        return numberOfSpectra;
    }

    public int getUniqueSpectra() {
        return clustersWithSpectra.keySet().size();
    }

    public List<ISpectrum> getUnusedSpectra(ISpectrumRetriever spc) {
        List<IPeptideSpectrumMatch> all = spc.retrieveAll();
        List<ISpectrum> ret = new ArrayList<ISpectrum>();
        for (IPeptideSpectrumMatch spec : all) {
            if (!clustersWithSpectra.containsKey(spec.getId()))
                ret.add(spec);
        }

        return ret;
    }


    public List<ISpectrum> getUsedSpectra(ISpectrumRetriever spc) {
        List<IPeptideSpectrumMatch> all = spc.retrieveAll();
        List<ISpectrum> ret = new ArrayList<ISpectrum>();
        for (IPeptideSpectrumMatch spec : all) {
            if (clustersWithSpectra.containsKey(spec.getId()))
                ret.add(spec);
        }

        return ret;
    }

    public int getNumberOfEmptyCluster() {
        return numberOfEmptyCluster;
    }

    public int getNumberOfDuplicatedSpectra() {
        return numberOfDuplicatedSpectra;
    }

    public Map<String, List<IPeptideSpectralCluster>> getClustersWithSpectra() {
        return clustersWithSpectra;
    }

    public void addCluster(IPeptideSpectralCluster cluster) {
        List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();

        if (clusteredSpectra.isEmpty()) {
            numberOfEmptyCluster++;
        }

        for (ISpectrum clusteredSpectrum : clusteredSpectra) {
            addSpectrumCluster(clusteredSpectrum.getId(), cluster, clustersWithSpectra);
            numberOfSpectra++;
        }
    }

    private void addSpectrumCluster(String spectrumId, IPeptideSpectralCluster cluster, Map<String, List<IPeptideSpectralCluster>> clusters) {
        List<IPeptideSpectralCluster> existingClusters = clusters.get(spectrumId);
        if (existingClusters == null) {
            existingClusters = new ArrayList<IPeptideSpectralCluster>();
            clusters.put(spectrumId, existingClusters);
        } else {
            numberOfDuplicatedSpectra++;
        }

        existingClusters.add(cluster);
    }

}
