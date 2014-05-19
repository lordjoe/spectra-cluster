package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 *
 * todo: development
 */
@Deprecated
public class UnStableClusteringEngine implements IUnStableClusteringEngine {

    private final Set<ICluster> stableClusters = new HashSet<ICluster>();

    private final SimilarityChecker similarityChecker;

    private boolean unStableClusterProcessed;

    public UnStableClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker());
    }

    public UnStableClusteringEngine(SimilarityChecker similarityChecker) {
        this.similarityChecker = similarityChecker;
    }

    @Override
    public void addStableCluster(ICluster stableCluster) {
        if (unStableClusterProcessed) {
            throw new IllegalStateException("Adding stable cluster after processing unstable clusters");
        }

        stableClusters.add(stableCluster);
    }


    /**
     * try to move spectra to the stable cluster
     *
     * @return true if changed
     */
    @Override
    public boolean processUnStableCluster(ICluster unStableCluster) {
        unStableClusterProcessed = true;

        int startSpectraCount = unStableCluster.getClusteredSpectraCount();
        for (ICluster stableCluster : stableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unStableCluster);
            if (empty)
                break;
        }
        return startSpectraCount != unStableCluster.getClusteredSpectraCount();

    }

    private boolean mergeUnstableCluster(ICluster stableCluster, ICluster unstableCluster) {
        ISpectrum consensusSpectrum = stableCluster.getConsensusSpectrum();

        // find all the unstable spectra which can be merged into the stable cluster
        HashSet<ISpectrum> spectraToRemove = new HashSet<ISpectrum>();
        for (ISpectrum unstableSpectrum : unstableCluster.getClusteredSpectra()) {
            double similarity = similarityChecker.assessSimilarity(unstableSpectrum, consensusSpectrum);
            if (similarity >= similarityChecker.getDefaultThreshold()) {
                spectraToRemove.add(unstableSpectrum);
            }
        }

        // remove from unstable cluster
        ISpectrum[] arrayOfSpectraToRemove = spectraToRemove.toArray(new ISpectrum[spectraToRemove.size()]);
        unstableCluster.removeSpectra(arrayOfSpectraToRemove);

        // add into stable cluster
        stableCluster.addSpectra(arrayOfSpectraToRemove);

        return unstableCluster.getClusteredSpectraCount() == 0;
    }

    @Override
    public Collection<ICluster> getClusters() {
        return new ArrayList<ICluster>(stableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ICluster> findNoneFittingSpectra(ICluster cluster) {
        List<ICluster> noneFittingSpectra = new ArrayList<ICluster>();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = similarityChecker.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = similarityChecker.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(ClusterUtilities.asCluster(spectrum));
                }
            }
        }

        return noneFittingSpectra;
    }

    public int getNumberOfUnstableSpectra() {
        int count = 0;

        for (ICluster unstableCluster : stableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
