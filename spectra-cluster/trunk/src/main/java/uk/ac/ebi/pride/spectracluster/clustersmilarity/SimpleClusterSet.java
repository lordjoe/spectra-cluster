package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;
import uk.ac.ebi.pride.spectracluster.io.TSVClusterAppender;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class SimpleClusterSet extends SimpleClusterRetriever implements IClusterSet {


    /**
     * merge similar clusters
     *
     * @param inp
     * @return
     */
    public static IClusterSet removeDuplicates(IClusterSet inp) {

        int total = 0;
        int mergable = 0;
        int samePeptideNonMergable = 0;

        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        List<IPeptideSpectrumCluster> clusters = inp.getClusters();
        // NOTE this is a hack sine the ids are the most common peptide
        Collections.sort(clusters, new Comparator<IPeptideSpectrumCluster>() {
            @Override
            public int compare(IPeptideSpectrumCluster o1, IPeptideSpectrumCluster o2) {
                String id1 = o1.getId();
                String id2 = o2.getId();
                return id1.compareTo(id2);
            }
        });
        String currentPeptide = "";
        IPeptideSpectrumCluster lastCluster = null;
        List<IPeptideSpectrumCluster> toMerge = new ArrayList<IPeptideSpectrumCluster>();

        final TSVClusterAppender tsvClusterAppender = new TSVClusterAppender();
        for (IPeptideSpectrumCluster cluster : clusters) {
            total++;
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String id = cluster.getId();
            String mostCommonPeptide = cluster.getMostCommonPeptide();
            if ("HGHLGFLPR".equals(mostCommonPeptide))
                mostCommonPeptide = cluster.getMostCommonPeptide(); // break here
            // should only happen once
            if (lastCluster == null) {
                lastCluster = cluster;
                toMerge.clear();
                toMerge.add(cluster);
                currentPeptide = mostCommonPeptide;
            } else {
                if (!currentPeptide.equals(mostCommonPeptide)) {
                    if (toMerge.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        tsvClusterAppender.appendCluster(sb, lastCluster);
                        sb.append("\n");
                        for (IPeptideSpectrumCluster sc : toMerge) {
                            if (sc != lastCluster) {
                                tsvClusterAppender.appendCluster(sb, sc);
                                sb.append("\n");
                                lastCluster.addSpectra(sc.getClusteredSpectra());
                            }
                        }
                        sb.append("\n");
                        System.out.println(sb.toString());
                    }
                    holder.add(lastCluster);
                    lastCluster = cluster;
                    toMerge.clear();
                    toMerge.add(cluster);
                    currentPeptide = mostCommonPeptide;

                } else {
                    // these may be the same
                    double sim = similarityChecker.assessSimilarity(lastCluster.getConsensusSpectrum(), cluster.getConsensusSpectrum());
                    if (sim > similarityChecker.getDefaultThreshold()) {
                        mergable++;
                        toMerge.add(cluster);
                        //   lastCluster.addSpectra(cluster.getClusteredSpectra());
                    } else {
                        holder.add(lastCluster);
                        samePeptideNonMergable++;
                    }
                }
            }

        }

        System.out.println("Start Total " + total);
        System.out.println("Mergable " + mergable);
        System.out.println("Not Mergable " + samePeptideNonMergable);
        System.out.println("New Total " + holder.size());
        return new SimpleClusterSet(holder);
    }

    private String name;
    private ClusteringHeader header;
    private PeptideUseId usage = new PeptideUseId();

    public SimpleClusterSet(Collection<IPeptideSpectrumCluster> clusters) {
        super(clusters);
    }

    public SimpleClusterSet() {
        super();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String pName) {
        name = pName;
    }


    /**
     * return header imformation
     *
     * @return
     */
    @Nullable
    @Override
    public ClusteringHeader getHeader() {
        return header;
    }

    public void setHeader(ClusteringHeader header) {
        this.header = header;
    }

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    @Override
    public List<IPeptideSpectrumCluster> getMatchingClusters(TypedPredicate<IPeptideSpectrumCluster> condition) {
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        for (IPeptideSpectrumCluster cluster : getClusters()) {
            if (condition.apply(cluster))
                holder.add(cluster);
        }
        return holder;
    }


    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<IPeptideSpectrumCluster> visitor, TypedVisitor<IPeptideSpectrumCluster>... otherVisitors) {
        for (IPeptideSpectrumCluster cluster : getClusters()) {
            visitor.visit(cluster);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < otherVisitors.length; i++) {
                otherVisitors[i].visit(cluster);

            }
        }

    }


    @Override
    public void addClusters(Collection<IPeptideSpectrumCluster> clusters) {
        for (IPeptideSpectrumCluster cluster : clusters) {
            guaranteeClusterId(cluster);
            addCluster(cluster);
        }
    }

    protected void buildAndSetIdForClusterWithoutId(IPeptideSpectrumCluster cluster) {
        List<String> peptides = cluster.getPeptides();
        String id = cluster.getSpectralId();
//        if (peptides.isEmpty())
//            id = cluster.toString();
//        else
//            id = usage.getPeptideId(peptides.get(0));

        if (cluster instanceof LazyLoadedSpectralCluster) {
            ((LazyLoadedSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof PSMSpectralCluster) {
            ((PSMSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof SpectralCluster) {
            ((SpectralCluster) cluster).setId(id);
            return;
        }
        throw new IllegalStateException("cannot guarantee non-null id");
    }

    @Override
    public List<IPeptideSpectrumCluster> getClustersWithSpectrum(ISpectrum spectrum) {
        String id = spectrum.getId();
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        for (IPeptideSpectrumCluster test : getClusters()) {
            List<ISpectrum> spcetrums = test.getClusteredSpectra();
            for (ISpectrum spcetrum : spcetrums) {
                if (id.equals(spcetrum.getId())) {
                    holder.add(test);
                    break;
                }
            }
        }

        return holder;
    }

    @Override
    public IClusterSet dropClustersLessThanSize(final int minsize) {
        SimpleClusterSet ret = new SimpleClusterSet();
        for (IPeptideSpectrumCluster sc : getClusters()) {
            if (sc.getClusteredSpectraCount() >= minsize)
                ret.addCluster(sc);
        }
        return ret;
    }

    @Override
    public String toString() {
        if (getName() != null)
            return getName();
        return super.toString();
    }

    @Override
    public List<IPeptideSpectrumCluster> getBestMatchingClusters(IPeptideSpectrumCluster cluster, int maxMatches) {
        ClusterQualityComparator clusterQualityComparator = new ClusterQualityComparator(cluster);

        return getBestMatchesWithQuality(maxMatches, clusterQualityComparator);
    }

    private List<IPeptideSpectrumCluster> getBestMatchesWithQuality(int maxMatches, ClusterQualityComparator clusterQualityComparator) {
        List<IPeptideSpectrumCluster> clusters = getClusters();
        Collections.sort(clusters, clusterQualityComparator);

        int numberOfMatches = clusters.size() < maxMatches ? clusters.size() : maxMatches;
        return clusters.subList(0, numberOfMatches);
    }

    private static class ClusterQualityComparator implements Comparator<IPeptideSpectrumCluster> {

        private final IPeptideSpectrumCluster clusterToMatch;

        private ClusterQualityComparator(IPeptideSpectrumCluster clusterToMatch) {
            this.clusterToMatch = clusterToMatch;
        }

        @Override
        public int compare(IPeptideSpectrumCluster o1, IPeptideSpectrumCluster o2) {
            double match1 = matchQuality(o1, clusterToMatch);
            double match2 = matchQuality(o2, clusterToMatch);

            return CompareTo.compare(match1, match2);
        }

        private double matchQuality(IPeptideSpectrumCluster c1, IPeptideSpectrumCluster c2) {
            Set<String> pep1 = new HashSet<String>(c1.getPeptides());
            Set<String> pep2 = new HashSet<String>(c2.getPeptides());
            int totalPeptides = pep1.size() + pep2.size();
            if (totalPeptides == 0)
                return 0;
            Set<String> common = new HashSet<String>(pep1);
            common.retainAll(pep2);

            double commonCunt = common.size();
            return commonCunt / totalPeptides;


        }
    }
}
