package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities
 *
 * @author Steve Lewis
 * @date 5/13/14
 */
public class ClusteringUtilities {

    /**
     * allow nonfitting spectra to leave and retuen a list of clusters to write out
     *
     * @param cluster
     * @return !null List<ISpectralCluster
     */
    @Nonnull
    public static List<IPeptideSpectrumCluster> asWritttenSpectra(@Nonnull IPeptideSpectrumCluster cluster, @Nonnull IClusteringEngine engine) {
        final List<IPeptideSpectrumCluster> allClusters = engine.findNoneFittingSpectra(cluster);
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        if (!allClusters.isEmpty()) {
            for (IPeptideSpectrumCluster removedCluster : allClusters) {

                // drop all spectra
                final List<ISpectrum> clusteredSpectra = removedCluster.getClusteredSpectra();
                ISpectrum[] allRemoved = clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]);
                cluster.removeSpectra(allRemoved);

                // and save as stand alone
                holder.add(removedCluster);
            }

        }
        if (cluster.getClusteredSpectraCount() > 0)
            holder.add(cluster);
        return holder;
    }


}
