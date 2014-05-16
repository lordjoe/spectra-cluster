package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class UnStableClusteringEngine implements IUnStableClusteringEngine {

    private final Set<IPeptideSpectrumCluster> stableClusters = new HashSet<IPeptideSpectrumCluster>();

    private final SimilarityChecker similarityChecker;

    private boolean unStableClusterProcessed;

    public UnStableClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker());
    }

    public UnStableClusteringEngine(SimilarityChecker similarityChecker) {
        this.similarityChecker = similarityChecker;
    }

    @Override
    public void addStableCluster(IPeptideSpectrumCluster stableCluster) {
        if (unStableClusterProcessed) {
            throw new IllegalStateException("Adding stable cluster after processing unstable clusters");
        }

        stableClusters.add(stableCluster);
    }


    /**
     * try to move spectra to the stable cluster
     *
     * @param unstableCluster
     * @return true if changed
     */
    @Override
    public boolean processUnStableCluster(IPeptideSpectrumCluster unStableCluster) {
        unStableClusterProcessed = true;

        int startSpectraCount = unStableCluster.getClusteredSpectraCount();
        for (IPeptideSpectrumCluster stableCluster : stableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unStableCluster);
            if (empty)
                break;
        }
        return startSpectraCount != unStableCluster.getClusteredSpectraCount();

    }

    private boolean mergeUnstableCluster(IPeptideSpectrumCluster stableCluster, IPeptideSpectrumCluster unstableCluster) {
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
    public Collection<IPeptideSpectrumCluster> getClusters() {
        return new ArrayList<IPeptideSpectrumCluster>(stableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<IPeptideSpectrumCluster> findNoneFittingSpectra(IPeptideSpectrumCluster cluster) {
        List<IPeptideSpectrumCluster> noneFittingSpectra = new ArrayList<IPeptideSpectrumCluster>();

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

        for (IPeptideSpectrumCluster unstableCluster : stableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
