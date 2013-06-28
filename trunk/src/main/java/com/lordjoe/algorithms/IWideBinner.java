package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.IWideBinner
 * a values may be placed in adjacent bins
 * User: Steve
 * Date: 6/28/13
 */
public interface IWideBinner extends IBinner {

    /**
     * give a list of all bins that the value may be assigned to
     * @param value  value to test
     * @return !null array of bins
     */
    public int[] asBins(double value);


}
