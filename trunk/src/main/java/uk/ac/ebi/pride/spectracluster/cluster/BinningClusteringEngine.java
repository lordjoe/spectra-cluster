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

    public static final int DEFAULT_MAX_BIN = 1600;
    public static final int DEFAULT_MIN_BIN = 300;


    private final IWideBinner binner;
    private final Map<Integer, IClusteringEngine> engineForBin = new HashMap<Integer, IClusteringEngine>();
    private String name = "BinningClusteringEngine";

    public BinningClusteringEngine() {
        this(new LinearWideBinner((int) (DEFAULT_MAX_BIN + 0.5), 1, DEFAULT_MIN_BIN, true));
    }


    public BinningClusteringEngine(final IWideBinner pBinner) {
        binner = pBinner;
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
        Collections.sort(holder);
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
     *
     * @param pBin
     * @return
     */
    protected IClusteringEngine getEngine(final int pBin) {
        synchronized (engineForBin) {
            IClusteringEngine ret = engineForBin.get(pBin);
            if (ret == null) {
                ret = new BinnedClusteringEngine(binner, pBin);
                 engineForBin.put(pBin, ret);
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
    public boolean processClusters() {

        boolean anythingDone = false;
        // todo use multiple threads
        for (IClusteringEngine engine : engineForBin.values()) {
            final List<ISpectralCluster> clusters = engine.getClusters();
            anythingDone |= engine.processClusters();
        }
        return anythingDone;
    }

    /**
     * nice for debugging to name an engine
     * @return  possibly null name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * nice for debugging to name an engine
     * @param pName   possibly null name
     */
    @Override
    public void setName(final String pName) {
        name = pName;
    }

    /**
      * allow engines to be named
      * @return
      */
     @Override
     public String toString() {
          if(name != null)
              return name;
         return super.toString();
     }

    /**
      * total number of clusters including queued clustersToAdd
      *
      * @return
      */
     @Override
     public int size() {
         int n = 0;
         // todo use multiple threads
         for (IClusteringEngine engine : engineForBin.values()) {
              n += engine.size();
         }
         return n;
     }

}
