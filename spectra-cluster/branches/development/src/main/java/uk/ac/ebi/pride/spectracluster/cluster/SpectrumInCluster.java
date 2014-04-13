package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster
 * Used in a Hadoop step where the key is the spectrum id and the value is the spectrum in cluster
 * return says whether to keep the spectrum or not
 * User: Steve
 * Date: 4/7/14
 */
public class SpectrumInCluster implements Equivalent<SpectrumInCluster> {

    public static Map<String,List<String>> serializeSpectrumInCluster(final Map<String, List<SpectrumInCluster>> pByClusterId) {
         Map<String,List<String>>   ret = new HashMap<String, List<String>>();
        for (String key : pByClusterId.keySet()) {
            List<String>  serializes = new ArrayList<String>();
            for (SpectrumInCluster s1 : pByClusterId.get(key)) {
                 StringBuilder sb = new StringBuilder();
                 s1.append(sb);
                serializes.add(sb.toString());
            }
            ret.put(key,serializes);
        }
        return ret;
      }


    public static  Map<String, List<SpectrumInCluster>> mapByClusterContentsString(final Map<String, List<SpectrumInCluster>> pById) {
        // populate a list  by clusterId
        Map<String, List<SpectrumInCluster>> byClusterId = new HashMap<String, List<SpectrumInCluster>>();
        for (String s : pById.keySet()) {
            List<SpectrumInCluster> original = pById.get(s);
            List<SpectrumInCluster> copy = new ArrayList<SpectrumInCluster>(original);
            SpectrumInCluster.handleClusters(copy);
            for (SpectrumInCluster spectrumInCluster : copy) {
                String clusterIds = SpectrumInCluster.listClusterIds(spectrumInCluster.getCluster());
                if (!byClusterId.containsKey(clusterIds)) {
                    byClusterId.put(clusterIds, new ArrayList<SpectrumInCluster>());
                }
                byClusterId.get(clusterIds).add(spectrumInCluster);
            }

        }
        return byClusterId;
    }



    public static List<SpectrumInCluster> buildSpectrumInClusters(final List<ISpectralCluster> pScs) {
        List<SpectrumInCluster> inClusters = new ArrayList<SpectrumInCluster>();
        // turn into
        for (ISpectralCluster sc : pScs) {
            List<SpectrumInCluster> spectrumInClusters = SpectrumInCluster.fromCluster(sc);
            inClusters.addAll(spectrumInClusters);
        }
        return inClusters;
    }


    public static Map<String, List<SpectrumInCluster>> mapById(final List<SpectrumInCluster> pInClusters) {
        Map<String, List<SpectrumInCluster>> byId = new HashMap<String, List<SpectrumInCluster>>();
        for (SpectrumInCluster inCluster : pInClusters) {
            String id = inCluster.getCluster().getId();
            if (!byId.containsKey(id)) {
                byId.put(id, new ArrayList<SpectrumInCluster>());
            }
            byId.get(id).add(inCluster);
        }
        return byId;
    }

    public static String listClusterIds(ISpectralCluster sc) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        List<String> holder = new ArrayList<String>();

        for (ISpectrum iSpectrum : sc.getClusteredSpectra()) {
            holder.add(iSpectrum.getId());
        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);

        sb.append(ret[0]);
        for (int i = 1; i < ret.length; i++) {
            sb.append(",");
            sb.append(ret[i]);

        }
        sb.append("]");
          return sb.toString();
    }

    public static List<SpectrumInCluster> fromCluster(ISpectralCluster cluster) {
        List<SpectrumInCluster> holder = new ArrayList<SpectrumInCluster>();
        for (ISpectrum sc : cluster.getClusteredSpectra()) {
            if (sc instanceof IPeptideSpectrumMatch) {
                holder.add(new SpectrumInCluster((IPeptideSpectrumMatch) sc, cluster));
            }
        }
        return holder;

    }

    public static void handleClusters(List<SpectrumInCluster> clusters) {
        dropSingleClusters(clusters);
        if (clusters.size() == 1)  // only single clusters  or just one
        {
            return;
        }
        SpectrumInCluster best = findBestCluster(clusters);
        for (SpectrumInCluster cluster : clusters) {
            if (best == cluster)
                continue;
            cluster.setRemoveFromCluster(true);  // not best
        }
    }

    public static SpectrumInCluster findBestCluster(final List<SpectrumInCluster> pClusters) {
        SpectrumInCluster best = pClusters.get(0);
        double bestDistance = best.getDistance();
        for (int i = 1; i < pClusters.size(); i++) {
            SpectrumInCluster test = pClusters.get(i);
            double testDistance = test.getDistance();
            if (testDistance < bestDistance) {
                best = test;
                bestDistance = testDistance;
            }

        }
        return best;
    }


    public static void dropSingleClusters(List<SpectrumInCluster> clusters) {
        if (clusters.size() == 1)
            return;
        // find all clusters of size 1
        List<SpectrumInCluster> holder = new ArrayList<SpectrumInCluster>();
        for (SpectrumInCluster cluster : clusters) {
            if (cluster.getCluster().getClusteredSpectraCount() == 1) {
                holder.add(cluster);
            }
        }
        // drop then
        clusters.removeAll(holder);
        // if nothing left add one back
        if (clusters.size() == 0)
            clusters.add(holder.get(0));
    }

    private IPeptideSpectrumMatch spectrum;
    private ISpectralCluster cluster;
    private double distance = -1;
    private boolean removeFromCluster;
    private boolean leadSpectrum; // is or was first spectrum in the cluster


    public SpectrumInCluster() {
    }

    public SpectrumInCluster(final IPeptideSpectrumMatch pSpectrum, final ISpectralCluster pCluster) {
        spectrum = pSpectrum;
        cluster = pCluster;
    }

    public void append(Appendable out) {
        try {
            out.append("=SpectrumInCluster=\n");
            out.append("removeFromCluster=" + isRemoveFromCluster() + "\n");
            out.append("distance=" + getDistance() + "\n");
            getSpectrum().appendSPText(out);
            getCluster().appendClustering(out);

        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override public boolean equivalent(final SpectrumInCluster o) {
        if (isRemoveFromCluster() != o.isRemoveFromCluster())
            return false;
        if (isLeadSpectrum() != o.isLeadSpectrum())
            return false;
        double distance1 = getDistance();
        double distance2 = o.getDistance();
        if (Math.abs(distance1 - distance2) > 0.001)
            return false;
        if (!getSpectrum().equivalent(o.getSpectrum()))
            return false;
        ISpectralCluster cluster1 = getCluster();
        ISpectralCluster cluster2 = o.getCluster();
        if (!cluster1.equivalent(cluster2))
            return false;
        return true;
    }


    public IPeptideSpectrumMatch getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(final IPeptideSpectrumMatch pSpectrum) {
        spectrum = pSpectrum;
    }

    public ISpectralCluster getCluster() {
        return cluster;
    }

    public void setCluster(final ISpectralCluster pCluster) {
        if(cluster == pCluster)
            return; // nothing to do
        cluster = pCluster;
        if (distance < 0)
            distance = computeDistance();
     }

    public double getDistance() {
        return distance;
    }

    public void setDistance(final double pDistance) {
        distance = pDistance;
    }

    protected double computeDistance() {
        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        IPeptideSpectrumMatch spectrum1 = getSpectrum();
        ISpectralCluster cluster1 = getCluster();
        ISpectrum spectrum2 = cluster1.getConsensusSpectrum();
        double distance = similarityChecker.assessSimilarity(spectrum1, spectrum2);
        return distance;
    }

    public boolean isRemoveFromCluster() {
        return removeFromCluster;
    }

    public void setRemoveFromCluster(final boolean pRemoveFromCluster) {
        removeFromCluster = pRemoveFromCluster;
    }

    public boolean isLeadSpectrum() {
        return leadSpectrum;
    }

    public void setLeadSpectrum(final boolean pLeadSpectrum) {
        leadSpectrum = pLeadSpectrum;
    }


}
