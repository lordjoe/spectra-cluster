package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.chart.CummulativeFDR
 * User: Steve
 * Date: 1/24/14
 */
public class CummulativeFDR {
    public static final double cummulativePurityPoints[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99, 1.0};
    public static final double reverse_cummulativePurityPoints[] = {1.0, 0.99, 0.95, 0.9, 0.8, 0.7, 0.6, 0.5,
            0.4, 0.3, 0.2, 0.1, 0.0};

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
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateDecoyVisitor(decoys, minimumSize, getDecoy_detector());
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }

    public List<ClusterPeptideFraction> getCumulativeTargetData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> targets = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv2 = new CummulativeFDR.AccumulateTargetVisitor(targets, minimumSize, getDecoy_detector());
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
    public static class AccumulateClusterPeptideFractionVisitor implements TypedVisitor<ISpectralCluster> {

        private final IDecoyDiscriminator discriminator;
        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateClusterPeptideFractionVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, IDecoyDiscriminator pdiscriminator) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            discriminator = pdiscriminator;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : cluster.getPeptidePurity(discriminator)) {
                data.add(pp);
            }
        }

    }


    /**
     * collect all decoys as  ClusterPeptideFraction
     */
    public static class AccumulateDecoyVisitor implements TypedVisitor<ISpectralCluster> {

        private final IDecoyDiscriminator discriminator;
        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateDecoyVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, IDecoyDiscriminator pdiscriminator) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            discriminator = pdiscriminator;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : cluster.getPeptidePurity(discriminator)) {
                boolean decoy = pp.isDecoy();
                if (decoy)
                    data.add(pp);
            }
        }

    }

    /**
     * collect all targets as  ClusterPeptideFraction
     */
    public static class AccumulateTargetVisitor implements TypedVisitor<ISpectralCluster> {

        private final IDecoyDiscriminator discriminator;
        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateTargetVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize, IDecoyDiscriminator pdiscriminator) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
            discriminator = pdiscriminator;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : cluster.getPeptidePurity(discriminator)) {
                boolean decoy = pp.isDecoy();
                 if (!decoy)
                    data.add(pp);
            }

        }

    }


}
