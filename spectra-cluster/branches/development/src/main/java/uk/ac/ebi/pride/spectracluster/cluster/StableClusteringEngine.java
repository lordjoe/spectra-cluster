package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngine implements IStableClusteringEngine {

    private final Set<ISpectralCluster> unstableClusters = new HashSet<ISpectralCluster>();

    private final SimilarityChecker similarityChecker;

    private boolean stableClusterProcessed;

    public StableClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker());
    }

    public StableClusteringEngine(SimilarityChecker similarityChecker) {
        this.similarityChecker = similarityChecker;
    }

    @Override
    public void addUnstableCluster(ISpectralCluster unstableCluster) {
        if (stableClusterProcessed) {
            throw new IllegalStateException("Adding unstable cluster after processing stable clusters");
        }

        unstableClusters.add(unstableCluster);
    }

    @Override
    public void processStableCluster(ISpectralCluster stableCluster) {
        stableClusterProcessed = true;

        HashSet<ISpectralCluster> emptyUnstableClusters = new HashSet<ISpectralCluster>();
        for (ISpectralCluster unstableCluster : unstableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unstableCluster);
            if (empty) {
                emptyUnstableClusters.add(unstableCluster);
            }
        }

        unstableClusters.removeAll(emptyUnstableClusters);
    }

    private boolean mergeUnstableCluster(ISpectralCluster stableCluster, ISpectralCluster unstableCluster) {
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
    public Collection<ISpectralCluster> getClusters() {
        return new ArrayList<ISpectralCluster>(unstableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(ISpectralCluster cluster) {
        List<ISpectralCluster> noneFittingSpectra = new ArrayList<ISpectralCluster>();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = similarityChecker.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = similarityChecker.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(spectrum.asCluster());
                }
            }
        }

        return noneFittingSpectra;
    }

    public int getNumberOfUnstableSpectra() {
        int count = 0;

        for (ISpectralCluster unstableCluster : unstableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
