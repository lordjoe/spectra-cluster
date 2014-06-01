package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.ClusterPeptideFraction;
import uk.ac.ebi.pride.spectracluster.cluster.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.chart.CummulativeFDR
 * User: Steve
 * Date: 1/24/14
 */
public class CummulativeFDR {
    public static final double fractionalPurityPoints[] = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0};
    public static final double cummulativePurityPoints[] = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 0.98, 0.99, 1.0};
    public static final double reverse_cummulativePurityPoints[] = {1.0, 0.99, 0.95, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0};

    private final SimpleSpectrumRetriever spectra;

    private final IDecoyDiscriminator decoy_detector;

    private final IClusterSet data;

    private final int[] targetsLessThanCutpoint = new int[cummulativePurityPoints.length];

    private final int[] decoysLessThanCutpoint = new int[cummulativePurityPoints.length];


    public CummulativeFDR(final IClusterSet pData, final SimpleSpectrumRetriever pSpectra, final IDecoyDiscriminator pDecoy_detector) {
        spectra = pSpectra;
        decoy_detector = pDecoy_detector;
        data = pData;

    }

    public IDecoyDiscriminator getDecoy_detector() {
        return decoy_detector;
    }

    public IClusterSet getData() {
        return data;
    }

    public List<ClusterPeptideFraction> getCumulativeDecoyData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ICluster> tv = new CummulativeFDR.AccumulateDecoyVisitor(decoys, minimumSize, getDecoy_detector());
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }

    public List<ClusterPeptideFraction> getCumulativeTargetData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> targets = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ICluster> tv2 = new CummulativeFDR.AccumulateTargetVisitor(targets, minimumSize, getDecoy_detector());
        cs.visitClusters(tv2);
        Collections.sort(targets);

        return targets;
    }


    public int[] buildCummulativeSeries(String name, List<ClusterPeptideFraction> values) {
        int[] itemsLessGreaterCut = new int[reverse_cummulativePurityPoints.length];
        int index = 0;
        int cutindex = 0;
        double numberPeptides = values.size();

        double cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex];
        for (ClusterPeptideFraction decoy : values) {

            double purity = decoy.getPurity();
            while (purity > cutPoint) {
                itemsLessGreaterCut[cutindex] = index;
                cutPoint = cummulativePurityPoints[++cutindex];
            }
            index++;
        }
        while (cutindex < CummulativeFDR.cummulativePurityPoints.length) {
            itemsLessGreaterCut[cutindex] = index;
            cutindex++;
        }
        return itemsLessGreaterCut;
    }


    public int[] reverseBuildCummulativeSeries(String name, List<ClusterPeptideFraction> values) {
        int index = 0;
        int cutindex = 0;
        double numberPeptides = values.size();
        int[] itemsLessThanCut = new int[reverse_cummulativePurityPoints.length];

        List<ClusterPeptideFraction> reverseValues = new ArrayList<ClusterPeptideFraction>(values);
        Collections.reverse(reverseValues);
        double cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex];

        double y = (index) / numberPeptides;
        for (ClusterPeptideFraction decoy : reverseValues) {

            double purity = decoy.getPurity();
            while (purity < cutPoint) {
                itemsLessThanCut[cutindex] = index;
                cutPoint = reverse_cummulativePurityPoints[++cutindex];
            }
            index++;
        }
        while (cutindex < reverse_cummulativePurityPoints.length) {
            double x = cutPoint;
            itemsLessThanCut[++cutindex] = index;
            if (cutindex > CummulativeFDR.reverse_cummulativePurityPoints.length)
                break;
        }
        return itemsLessThanCut;
    }


    /**
     * collect all decoys as  ClusterPeptideFraction
     */
    public static class AccumulateClusterPeptideFractionVisitor implements TypedVisitor<ICluster> {

        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;
        private final ClusterDataType type;

        public AccumulateClusterPeptideFractionVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, ClusterDataType pType) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            type = pType;
        }

        public AccumulateClusterPeptideFractionVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize) {
            this(pData, pminimumClusterSize, ClusterDataType.All);
        }



        /**
         */
        @Override
        public void visit(@Nonnull final ICluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;

            for (ClusterPeptideFraction pp : ClusterSimilarityUtilities.getPeptidePurity(cluster,null)) {
                switch (type) {
                    case All:
                        data.add(pp);
                        break;
                    case Decoy:
                        if (pp.isDecoy())
                            data.add(pp);
                        break;
                    case Target:
                        if (!pp.isDecoy())
                            data.add(pp);
                        break;
                }
            }
        }

    }


    /**
     * collect all ClusterMZSpread
     */
    public static class AccumulateClusterMZRangeVisitor implements TypedVisitor<ICluster> {

        private final List<ClusterMZSpread> data;
        private final int minimumClusterSize;

        public AccumulateClusterMZRangeVisitor(final List<ClusterMZSpread> pData, int pminimumClusterSize) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
        }

        /**
         */
        @Override
        public void visit(@Nonnull final ICluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            data.add(new ClusterMZSpread(cluster));
        }

    }

    /**
     * collect all decoys as  ClusterPeptideFraction
     */
    public static class AccumulateClusterPSMFractionVisitor implements TypedVisitor<ICluster> {

        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateClusterPSMFractionVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
        }

        /**
         */
        @Override
        public void visit(@Nonnull final ICluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : ClusterSimilarityUtilities.getPeptidePurity(cluster,null)) {
                data.add(pp);
            }
        }

    }


    /**
     * collect all decoys as  ClusterPeptideFraction
     */
    public static class AccumulateDecoyVisitor implements TypedVisitor<ICluster> {

        private final IDecoyDiscriminator discriminator;
        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateDecoyVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, IDecoyDiscriminator pdiscriminator) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            discriminator = pdiscriminator;
        }

        /**
         */
        @Override
        public void visit(@Nonnull final ICluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : ClusterSimilarityUtilities.getPeptidePurity(cluster,discriminator)) {
                boolean decoy = pp.isDecoy();
                if (decoy)
                    data.add(pp);
            }
        }

    }

    /**
     * collect all targets as  ClusterPeptideFraction
     */
    public static class AccumulateTargetVisitor implements TypedVisitor<ICluster> {

        private final IDecoyDiscriminator discriminator;
        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateTargetVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, IDecoyDiscriminator pdiscriminator) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            discriminator = pdiscriminator;
        }

        /**
         */
        @Override
        public void visit(@Nonnull final ICluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : ClusterSimilarityUtilities.getPeptidePurity(cluster,discriminator)) {
                boolean decoy = pp.isDecoy();
                if (!decoy)
                    data.add(pp);
            }

        }

    }


}
