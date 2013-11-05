package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSet implements IClusterSet {

    private final List<ISpectralCluster> clusters = new CopyOnWriteArrayList<ISpectralCluster>();

    public synchronized void addCluster(ISpectralCluster... sc) {
        for (int i = 0; i < sc.length; i++) {
            ISpectralCluster scc = sc[i];
            clusters.add(scc);
        }
    }
    @Override
    public Collection<ISpectralCluster> getClusters() {
        return new ArrayList<ISpectralCluster>(clusters);
          }

    @Override
    public Collection<ISpectralCluster> getClustersWithSpectrum(ISpectrum spectrum) {
        String id = spectrum.getId();
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster test : clusters) {
            List<ISpectrum> spcetrums = test.getClusteredSpectra() ;
            for (ISpectrum spcetrum : spcetrums) {
                 if(id.equals(spcetrum.getId()))  {
                     holder.add(test);
                     break;
                 }
            }
        }

        return holder;
    }

    @Override
    public Collection<ISpectralCluster> getBestMatchingClusters(ISpectralCluster spectrum, int maxMatches) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
