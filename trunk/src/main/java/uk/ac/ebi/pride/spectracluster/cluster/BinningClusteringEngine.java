package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinningClusteringEngine
 * performs clustering using a series of binned engines
 * User: Steve
 * Date: 6/28/13
 */
public class BinningClusteringEngine implements IClusteringEngine {

    private final IWideBinner binner;
    private final Map<Integer, IClusteringEngine> engineForBin = new HashMap<Integer, IClusteringEngine>();
    private final IClusteringEngine engine;

    public BinningClusteringEngine(final IClusteringEngine pEngine, final IWideBinner pBinner) {
        binner = pBinner;
        engine = pEngine;
    }


    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (IClusteringEngine engine : engineForBin.values()) {
            final List<ISpectralCluster> clusters = engine.getClusters();
            holder.addAll(clusters);
        }
        return holder;
    }

    /**
     * add some clusters
     */
    @Override
    public void addClusters(final ISpectralCluster... cluster) {
        for (int i = 0; i < cluster.length; i++) {
            ISpectralCluster cl = cluster[i];
            // only add ones in the right bin
            final float precursorMz = cl.getPrecursorMz();
            int[] bins = binner.asBins(precursorMz);
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                IClusteringEngine engine = getEngine(bin);
                engine.addClusters(cl);
            }
        }
    }

    /**
     * find the engine for a bin creating one as needed
     * @param pBin
     * @return
     */
    protected IClusteringEngine getEngine(final int pBin) {
        synchronized (engineForBin)  {
            IClusteringEngine ret = engineForBin.get(pBin);
            if(ret == null)  {
                ret = new BinnedClusteringEngine(engine,binner,pBin) ;
                engineForBin.put(pBin,ret);
            }
            return ret;
        }
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean mergeClusters() {

        boolean anythingDone = false;
        // todo use multiple threads
         for (IClusteringEngine engine : engineForBin.values()) {
             final List<ISpectralCluster> clusters = engine.getClusters();
             anythingDone |= engine.mergeClusters();
         }
        return anythingDone ;
    }
}
