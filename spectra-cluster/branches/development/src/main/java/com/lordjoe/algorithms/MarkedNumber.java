package com.lordjoe.algorithms;

import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * com.lordjoe.algorithms.MarkedNumber
 * Associate a double - usually >= 0 with an object
 * most obvious use if for APrioriBinning to assign big jobs to early reduce tasks and
 * sheild these from later additions
 * User: Steve
 * Date: 4/22/2014
 */
public class MarkedNumber<T> implements Comparable<MarkedNumber<T>> {

    /**
     * return a list with values adjusted to sum to 1
     *
     * @param items some list
     * @param <T>   type
     * @return
     */
    public @Nonnull static Map<Integer, Integer> partitionFromBinner(@Nonnull List<MarkedNumber<String>> inItems, int numberBins, IWideBinner binner) {
        List<MarkedNumber<String>> items = normalize(inItems);
        double minBin = Double.MAX_VALUE;
        Map<String, MarkedNumber> toTotal = new HashMap<String, MarkedNumber>();
        for (MarkedNumber<String> item : items) {
            double value = item.getValue();
            if(value > 0)
                minBin = Math.min(minBin,value);
            toTotal.put(item.getMark(), item);
        }


        double[] totals = new double[numberBins];
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (int i = binner.getMinBin(); i < binner.getMaxBin(); i++) {
            double mz = binner.fromBin(i);
            String s = ClusterUtilities.describeDaltons(mz);
            int thisBin = findBestBin(totals);
            MarkedNumber thisMark = toTotal.get(s);
            if(thisMark == null)
                thisMark = new  MarkedNumber(new Integer(i),minBin); // give not found the minimum weight
            ret.put(i, thisBin);
            totals[thisBin] += thisMark.getValue(); // put in this partition
        }
        return ret; // map from index to partition
    }

    /**
     * return a list with values adjusted to sum to 1
     *
     * @param items some list
     * @param <T>   type
     * @return
     */
    public @Nonnull static <T> Map<T, Integer> partitionIntoBins(@Nonnull List<MarkedNumber<T>> inItems, int numberBins) {
        List<MarkedNumber<T>> items = normalize(inItems);
        Map<T, Integer> ret = new HashMap<T, Integer>();
        double[] totals = new double[numberBins];
        for (MarkedNumber<T> item : items) {
            int thisBin = findBestBin(totals);
            ret.put(item.getMark(), thisBin);
            totals[thisBin] += item.getValue(); // put in this partition
        }
        return ret; // map from index to partition
    }


    protected static int findBestBin(double[] bins) {
        double bestValue = Double.MAX_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < bins.length; i++) {
            double bin = bins[i];
            if (bin == 0)
                return i; // always use empty bin
            if (bin < bestValue) {
                bestIndex = i;
                bestValue = bin; // now we have the smallest
            }
        }
        return bestIndex;
    }

    /**
     * return alist with values adjusted to sum to 1
     *
     * @param items some list
     * @param <T>   type
     * @return
     */
    public @Nonnull static <T> List<MarkedNumber<T>> normalize(@Nonnull List<MarkedNumber<T>> items) {
        double total = total(items);
        if (total == 1)
            return items;
        List<MarkedNumber<T>> holder = new ArrayList<MarkedNumber<T>>();
        for (MarkedNumber<T> item : items) {
            holder.add(new MarkedNumber<T>(item.getMark(), item.getValue() / total));
        }
        Collections.sort(holder);
        return holder;
    }

    public static <T> double total(@Nonnull List<MarkedNumber<T>> items) {
        double ret = 0;
        for (MarkedNumber<T> item : items) {
            ret += item.getValue();
        }
        return ret;
    }

    private final T mark;
    private final double value;

    public MarkedNumber(final T pMark, final double pValue) {
        mark = pMark;
        value = pValue;
    }

    public T getMark() {
        return mark;
    }

    public double getValue() {
        return value;
    }

    /**
     * sort my value highest first then mark
     *
     * @param o
     * @return
     */
    @Override public int compareTo(final MarkedNumber<T> o) {
        double myValue = getValue();
        double oValue = o.getValue();
        if (myValue != oValue)
            return myValue > oValue ? -1 : 1;
        T me = getMark();
        if (me instanceof Comparable)
            return ((Comparable<T>) me).compareTo(o.getMark());
        return me.toString().compareTo(o.getMark().toString());

    }

    public String toString() {
        return String.format("%s:%6.3f", getMark(), getValue());
    }
}
