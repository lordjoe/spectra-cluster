package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinnedClusteringEngine
 * performs clustering for a single mz bin
 * User: Steve
 * Date: 6/28/13
 */
public class BinnedClusteringEngine implements IClusteringEngine {

    private final IWideBinner binner;
    private final int mainBin;
    private final IClusteringEngine engine;

    public BinnedClusteringEngine(final IClusteringEngine pEngine,final IWideBinner pBinner, final int pMainBin) {
        binner = pBinner;
        mainBin = pMainBin;
        engine = pEngine;
    }



    /**
     * Get clustered clusters
     * SLewis - I think a guarantee that they are sorted by MZ is useful
     */
    @Override
    public List<ISpectralCluster> getClusters() {
        final List<ISpectralCluster> clusters = engine.getClusters();
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            final float precursorMz = cluster.getPrecursorMz();
            final int bin = binner.asBin(precursorMz);
            if(bin == mainBin)
                 holder.add(cluster); // only report clusters in the main bin
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
            boolean useCluster = false;
            for (int j = 0; j < bins.length; j++) {
                if(mainBin == bins[j])  {
                    useCluster = true;
                    break;
                }
               }
            if(useCluster)
                 engine.addClusters(cl);
         }
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean mergeClusters() {
        return engine.mergeClusters();
     }
}
