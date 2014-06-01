package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumToClusters {
    private final Map<String, List<ICluster>> clustersWithSpectra = new HashMap<String, List<ICluster>>();
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
        List<ISpectrum> all = spc.retrieveAll();
        List<ISpectrum> ret = new ArrayList<ISpectrum>();
        for (ISpectrum spec : all) {
            if (!clustersWithSpectra.containsKey(spec.getId()))
                ret.add(spec);
        }

        return ret;
    }


    public List<ISpectrum> getUsedSpectra(ISpectrumRetriever spc) {
        List<ISpectrum> all = spc.retrieveAll();
        List<ISpectrum> ret = new ArrayList<ISpectrum>();
        for (ISpectrum spec : all) {
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

    public Map<String, List<ICluster>> getClustersWithSpectra() {
        return clustersWithSpectra;
    }

    public void addCluster(ICluster cluster) {
        List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();

        if (clusteredSpectra.isEmpty()) {
            numberOfEmptyCluster++;
        }

        for (ISpectrum clusteredSpectrum : clusteredSpectra) {
            addSpectrumCluster(clusteredSpectrum.getId(), cluster, clustersWithSpectra);
            numberOfSpectra++;
        }
    }

    private void addSpectrumCluster(String spectrumId, ICluster cluster, Map<String, List<ICluster>> clusters) {
        List<ICluster> existingClusters = clusters.get(spectrumId);
        if (existingClusters == null) {
            existingClusters = new ArrayList<ICluster>();
            clusters.put(spectrumId, existingClusters);
        } else {
            numberOfDuplicatedSpectra++;
        }

        existingClusters.add(cluster);
    }

}
