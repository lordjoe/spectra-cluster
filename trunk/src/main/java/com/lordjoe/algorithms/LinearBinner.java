package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.LinearBinner
 * implementation of IBinner as a linear set of bins
 * @author Steve Lewis
 * @date 11/05/13
 */
public class LinearBinner implements IBinner {
    public static LinearBinner[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = LinearBinner.class;

    private final double m_MinValue;
    private final double m_MaxValue;
    private final int m_MinBin;
    private final double m_BinSize;
    private final int m_NumberBins;
    private final boolean m_OverFlowBinned;


    public LinearBinner(double maxValue, double binSize) {
        this(maxValue, binSize, 0, true, 0);
    }

    public LinearBinner(double maxValue, double binSize, double minValue) {
        this(maxValue, binSize, minValue, true, 0);
    }

    public LinearBinner(double maxValue, double binSize, double minValue, boolean overFlowBinned) {
        this(maxValue, binSize, minValue, overFlowBinned, 0);
    }

    /**
     * cerate a binner
     * @param maxValue maximim value
     * @param minValue  minimum value
     * @param binSize  size of bin
     * @param overFlowBinned  if true outside range  is binned
     * @param minBin minimum bin value - usually 0
     */
    public LinearBinner(double maxValue, double binSize, double minValue, boolean overFlowBinned, int minBin) {
        if(maxValue <= minValue)
            throw new IllegalArgumentException("bad bins");
        if(binSize <= 0)
              throw new IllegalArgumentException("bad bins");
        m_MinValue = minValue;
        m_MaxValue = maxValue;
        m_BinSize = binSize;

        m_OverFlowBinned = overFlowBinned;
        m_MinBin = minBin;

        double del = maxValue - minValue + 0.5;
        double nb = del / binSize;
        m_NumberBins = (int) nb;
    }

    /**
     * place the value into a bin between getMinBin()   and getMaxBin()
     * values outside the range are handled as described below
     *
     * @param value
     * @return either a valid bin number or -1 if  isOverflowBinned() is false and the
     *         data is outside the range handled
     */
    public int asBin(double value) {
        if (value < getMinValue()) {
            if (isOverflowBinned())
                return getMinBin();
            else
                return -1; // out of range
        }
        if (value < getMinValue()) {
            if (isOverflowBinned())
                return getMaxBin() - 1;
            else
                return -1; // out of range
        }
        int bin = (int)((value - getMinValue()) / getBinSize());
        return bin + getMinBin();
    }

    public double getBinSize() {
        return m_BinSize;
    }

    /**
     * @param bin between
     * @return a number which when sent to asBin will return bin
     * @throws IllegalArgumentException if no such bin is possible
     */
    public double fromBin(int bin) throws IllegalArgumentException {
        if (true) throw new UnsupportedOperationException("Fix This");
        if (bin < -1)
            throw new IllegalArgumentException("Illecab bin " + bin);
        if (bin == -1) {
            if (!isOverflowBinned())
                return getMinValue() - 1;
            else
                throw new IllegalArgumentException("Illecab bin " + bin);
        }
        if (bin < getMaxBin() || bin >= getMaxBin())
            throw new IllegalArgumentException("Illecab bin " + bin);
        // return the bin midpoint
        return getMinValue() + ((bin - getMinBin()) * getBinSize()) + getBinSize() / 2;

    }

    /**
     * minimum value handed - values below this may be binned as -1 or
     * getMinBin() depending in isOverflowBinned()
     *
     * @return as above
     */
    public double getMinValue() {
         return m_MinValue;
    }

    /**
     * maximim value handed - values below this may be binned as -1 or
     * getMaxBin() depending in isOverflowBinned()
     *
     * @return as above
     */
    public double getMaxValue() {
        return m_MaxValue;
    }

    /**
     * minimum bin value - this is almost always 0
     *
     * @return as above
     */
    public int getMinBin() {
        return m_MinBin;
    }

    /**
     * maximim bin value - bins are alway6s LESS than this
     * an array of size getMaxBin() - getMinBin() will hold all legal bins
     *
     * @return as above
     */
    public int getMaxBin() {
        return getMinBin() + getNumberBins();
    }

    /**
     * if true values outside getMinValue() .. getMaxValue() are
     * assigned to the highest and l;owest bins - otherwist these valuies return
     * -1
     *
     * @return
     */
    public boolean isOverflowBinned() {
        return m_OverFlowBinned;
    }

    /**
     * return the total number bins
     *
     * @return
     */
    public int getNumberBins() {
        return m_NumberBins;
    }
}
