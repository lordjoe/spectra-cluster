package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterSet {

    public String getName();

    public void setName(String name);

    /**
     * return header imformation
     * @return
     */
    public @Nullable ClusteringHeader getHeader();

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
