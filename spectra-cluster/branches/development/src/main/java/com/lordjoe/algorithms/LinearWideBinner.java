package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.LinearWideBinner
 * User: Steve
 * Date: 6/28/13
 */
public class LinearWideBinner extends LinearBinner implements IWideBinner {

    private final int m_NumberOverlapBins;

    public LinearWideBinner(final double maxValue, final double binSize, final double minValue, final boolean overFlowBinned) {
        this(maxValue, binSize, minValue, overFlowBinned, 1);
    }

    public LinearWideBinner(final double maxValue, final double binSize, final double minValue, final boolean overFlowBinned,int numberOverlap) {
        super(maxValue, binSize, minValue, overFlowBinned);
        m_NumberOverlapBins = numberOverlap;
    }

    protected int getNumberOverlapBins() {
        return m_NumberOverlapBins;
    }

    /**
     * give a list of all bins that the value may be assigned to
     *
     * @param value value to test
     * @return !null array of bins
     */
    @Override
    public int[] asBins(final double value) {
        int mainBin = asBin(value);
        int lower = Math.max(getMinBin(), mainBin - getNumberOverlapBins());
        int upper = Math.min(getMaxBin() - 1, mainBin + getNumberOverlapBins());
        int[] ret = new int[upper - lower + 1];
        for (int i = lower; i <= upper; i++) {
            ret[i - lower] = i ;
          }
        return ret;
    }
}
