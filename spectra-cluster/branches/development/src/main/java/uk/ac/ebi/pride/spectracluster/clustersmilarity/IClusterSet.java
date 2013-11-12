package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.google.common.base.*;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterSet {

    public int getClusterCount();

    public List<ISpectralCluster> getClusters();

    /**
     * visit all clusters
     * @param visitor !null visitor(s)
      */
    public void visitClusters(TypedVisitor<ISpectralCluster> visitor,TypedVisitor<ISpectralCluster>... otherVisitors );

    /**
     * get all clusters matching the predicate
     * @param condition
     * @return
     */
    public List<ISpectralCluster> getMatchingClusters(TypedPredicate<ISpectralCluster> condition);

    public void addClusters(Collection<ISpectralCluster> clusters);

    public List<ISpectralCluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<ISpectralCluster> getBestMatchingClusters(ISpectralCluster cluster, int maxMatches);

}
