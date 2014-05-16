package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngine implements IStableClusteringEngine {

    private final SortedSet<IPeptideSpectralCluster> unstableClusters = new TreeSet<IPeptideSpectralCluster>();

    private final SimilarityChecker similarityChecker;

    private boolean stableClusterProcessed;

    public StableClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker());
    }

    public StableClusteringEngine(SimilarityChecker similarityChecker) {
        this.similarityChecker = similarityChecker;
    }

    /**
     * add one cluster and return any clusters which are too far in mz from further consideration
     * NOTE clusters MUST be added in ascending MZ order
     *
     * @param added !null cluster to add
     * @return !null list of clusters not far enough away they will no longer change
     */
    @Override
    public Collection<IPeptideSpectralCluster> addClusterIncremental(final IPeptideSpectralCluster added) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * add some clusters
     *
     * @param cluster
     */
    @Override
    public void addClusters(final IPeptideSpectralCluster... cluster) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return "StableClusteringEngine";
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return unstableClusters.size();
    }

    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(final IProgressHandler handler) {
        throw new UnsupportedOperationException("This Should NEVER be Called");

    }

    @Override
    public void addUnstableCluster(IPeptideSpectralCluster unstableCluster) {
        if (stableClusterProcessed) {
            // let this slide
            // throw new IllegalStateException("Adding unstable cluster after processing stable clusters");
        }

        unstableClusters.add(unstableCluster);
    }

    @Override
    public void processStableCluster(IPeptideSpectralCluster stableCluster) {
        stableClusterProcessed = true;

        Set<IPeptideSpectralCluster> emptyUnstableClusters = new HashSet<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster unstableCluster : unstableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unstableCluster);
            if (empty) {
                emptyUnstableClusters.add(unstableCluster);
            }
        }

        unstableClusters.removeAll(emptyUnstableClusters);
    }

    private boolean mergeUnstableCluster(IPeptideSpectralCluster stableCluster, IPeptideSpectralCluster unstableCluster) {
        ISpectrum consensusSpectrum = stableCluster.getConsensusSpectrum();

        // find all the unstable spectra which can be merged into the stable cluster
        Set<ISpectrum> spectraToRemove = new HashSet<ISpectrum>();
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
    public Collection<IPeptideSpectralCluster> getClusters() {
        return new ArrayList<IPeptideSpectralCluster>(unstableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<IPeptideSpectralCluster> findNoneFittingSpectra(IPeptideSpectralCluster cluster) {
        List<IPeptideSpectralCluster> noneFittingSpectra = new ArrayList<IPeptideSpectralCluster>();

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


    /**
     * allow nonfitting spectra to leave and return a list of clusters to write out
     *
     * @param cluster
     * @return !null List<ISpectralCluster
     */
    @Nonnull
    @Override
    public List<IPeptideSpectralCluster> asWritttenSpectra(@Nonnull IPeptideSpectralCluster cluster) {
        return ClusteringUtilities.asWritttenSpectra(cluster, this);
    }

    public int getNumberOfUnstableSpectra() {
        int count = 0;

        for (IPeptideSpectralCluster unstableCluster : unstableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
