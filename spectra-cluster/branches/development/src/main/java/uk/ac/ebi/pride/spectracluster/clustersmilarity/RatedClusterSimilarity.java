package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.RatedClusterSimilarity
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public class RatedClusterSimilarity implements Comparable<RatedClusterSimilarity> {


    private final MostSimilarClusters clusters;
    private final ClusterSimilarityEnum rating;
    private double similarityDistance;
    private final Set<ClusterSimilarityProblem> problems = new HashSet<ClusterSimilarityProblem>();
    private final SimilarityChecker similarity = Defaults.INSTANCE.getDefaultSimilarityChecker();

    public RatedClusterSimilarity(MostSimilarClusters clusters) {
        this.clusters = clusters;
        rating = buildRating(clusters);
    }

    public static final double GOOD_DISTANCE = 0.05;
    public static final double MEDIUM_DISTANCE = 0.10;
    public static final double BAD_DISTANCE = 0.3;

    protected ClusterSimilarityEnum buildRating(MostSimilarClusters clusters) {
        ClusterDistanceItem m1 = clusters.getBestMatch();

        if (m1 == null)
            return ClusterSimilarityEnum.NoFit;

        double distance = m1.getDistance();
        if (distance > MEDIUM_DISTANCE) {
            problems.add(ClusterSimilarityProblem.HighDotProduct);
        }

        ClusterDistanceItem m2 = clusters.getNextBestMatches();
        double d2 = m2.getDistance();
        if (d2 < MEDIUM_DISTANCE) {
            problems.add(ClusterSimilarityProblem.CloseSecondCluster);
        }

        ISpectralCluster source = m1.getSource();
        ISpectralCluster target = m1.getTarget();

        similarityDistance = similarity.assessSimilarity(source.getConsensusSpectrum(),target.getConsensusSpectrum());

        int sourceCount = source.getClusteredSpectraCount();
        int targetCount = target.getClusteredSpectraCount();

        double del = Math.abs(sourceCount - targetCount);
        double min = Math.min(targetCount, sourceCount);
        if (min == 0) {
            problems.add(ClusterSimilarityProblem.NoSpectra);
        } else {
            double farcDifference = del / min;
            if (farcDifference > 0.20) {
                problems.add(ClusterSimilarityProblem.UnevenSize);
            }
        }
        String peptide = source.getMostCommonPeptide();
        String p2 = target.getMostCommonPeptide();
        if (!peptide.equals(p2))
            problems.add(ClusterSimilarityProblem.DifferentBestPeptide);

        if (distance >= BAD_DISTANCE)
            return ClusterSimilarityEnum.Bad;

        if (distance >= MEDIUM_DISTANCE)
            return ClusterSimilarityEnum.Medium;

        if (problems.isEmpty())
            return ClusterSimilarityEnum.Good;
        return ClusterSimilarityEnum.Medium;
    }


    public MostSimilarClusters getClusters() {
        return clusters;
    }

    public ClusterSimilarityEnum getRating() {
        return rating;
    }

    public boolean hasProblem(ClusterSimilarityProblem prob) {
        return problems.contains(prob);
    }


    public List<ClusterSimilarityProblem> getProblems() {
        List<ClusterSimilarityProblem> ret = new ArrayList<ClusterSimilarityProblem>(problems);
        Collections.sort(ret);
        return ret;
    }

    public void appendReportTSV(Appendable appendable) {
        try {
            MostSimilarClusters mostSimilarClusters = getClusters();
            ClusterDistanceItem bestMatch = mostSimilarClusters.getBestMatch();
            appendable.append(getRating().toString());
            appendable.append("\t");
            ISpectralCluster source = bestMatch.getSource();
            ISpectralCluster target = bestMatch.getTarget();
            appendable.append(source.getId());
            appendable.append("\t");
            appendable.append(target.getId());
            appendable.append("\t");

            appendable.append(Integer.toString(source.getClusteredSpectraCount()));
            appendable.append("\t");
            appendable.append(Integer.toString(target.getClusteredSpectraCount()));
            appendable.append("\t");
            Set<String> commonIds = ClusterSimilarityUtilities.commonSpectralIds(source, target);
            appendable.append(Integer.toString(commonIds.size()));
            appendable.append("\t");
            // header up tp here

            double distance = bestMatch.getDistance();
            String distStr = String.format("%f8.3", distance);
            appendable.append(distStr);
            appendable.append("\t");

            String mostCommonPeptide = source.getMostCommonPeptide();
            if(!hasProblem(ClusterSimilarityProblem.DifferentBestPeptide))
                appendable.append(mostCommonPeptide);
            appendable.append("\t");

             String simdistStr = String.format("%f8.3", similarityDistance);
            appendable.append(simdistStr);
            appendable.append("\t");



            appendable.append("\n");
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static void appendHeaderTSV(Appendable appendable) {
        try {
            appendable.append("rating");
            appendable.append("\t");
            appendable.append("source id");
            appendable.append("\t");
            appendable.append("Target id");
            appendable.append("\t");

            appendable.append("source count");
            appendable.append("\t");
            appendable.append("target count");
            appendable.append("\t");
            appendable.append("common count");


            appendable.append("distance");
            appendable.append("\t");
            appendable.append("common peptide");
            appendable.append("\t");
            appendable.append("similarity distance");
             appendable.append("\t");

            appendable.append("\n");


        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public void appendReport(Appendable appendable) {
        try {
            if (getRating() == ClusterSimilarityEnum.Bad) {
                appendable.append("===== BAD BAD ========\n");
                // return;
            }
            if (getRating() == ClusterSimilarityEnum.NoFit) {
                appendable.append("===== NOFIT  ========\n");
                return;
            }
            String simdistStr = String.format("%f8.3", similarityDistance);
             appendable.append(getRating().toString() + " " + getProblemString() +
                    simdistStr +
                    "\n");
            MostSimilarClusters mostSimilarClusters = getClusters();
            List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();

            ClusterDistanceItem bestMatch = bestMatches.get(0);
            bestMatch.appendReport(appendable);
            appendable.append("\n");


//                    for (int i = 1; i < bestMatches.size(); i++) {
//                        ClusterDistanceItem clusterDistanceItem = bestMatches.get(i);
//                             clusterDistanceItem.appendReport(appendable);
//                            appendable.append("\n");
//                        }
//                    }
            appendable.append("============================================\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProblemString() {
        StringBuilder sb = new StringBuilder();
        for (ClusterSimilarityProblem prob : problems) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(prob);
        }

        return sb.toString();

    }


    @Override
    public int compareTo(RatedClusterSimilarity o2) {
        ClusterSimilarityEnum r1 = getRating();
        ClusterSimilarityEnum r2 = o2.getRating();
        if (r1 != r2)
            return ClusterSimilarityEnum.compare(r1, r2);
        ClusterDistanceItem m1 = getClusters().getBestMatch();
        ClusterDistanceItem m2 = o2.getClusters().getBestMatch();

        double d1 = m1.getDistance();
        double d2 = m2.getDistance();

        return Double.compare(d1, d2);


    }
}
