package com.lordjoe.algorithms;

import uk.ac.ebi.pride.spectracluster.hadoop.*;

import java.util.*;

/**
 * map items to counts of their occurrances
 * @param <T>
 */
public class CountedMap<T> {
    private Map<T, Integer> items = new HashMap<T, Integer>();

    public void add(T item) {
        int value = 1;
        if (items.containsKey(item)) {
            value = items.get(item) + 1;
        }
        items.put(item, value);
    }

    public void add(T item,int value) {
            items.put(item, value);
       }


    public int get(T item) {
        if (items.containsKey(item)) {
            return items.get(item);
        }
        return 0;
    }

    public int getTotal() {
        int sum = 0;
        for (Integer v : items.values()) {
            sum += v;
        }
        return sum;
    }


    public int size() {
        return items.size();
    }

    public int getCountsMoreThanN(int n) {
        int sum = 0;
        for (Integer v : items.values()) {
           if(v > n)
               sum++;
        }
        return sum;
    }

    public int getDuplicates() {
        return getCountsMoreThanN(1) ;
    }

    public CountedMap<T> getDifferences(CountedMap<T> others) {
        CountedMap<T> ret = new CountedMap<T>();
        for (T t : items.keySet()) {
            int me = get(t) ;
            int other = others.get(t);
            if(me != other)
                  ret.add(t,me - other);
        }
        // handle cases we have but not they
        for (T t : others.items.keySet()) {
             if(ret.get(t) != 0)
                 continue;
              int other = others.get(t);
             ret.add(t,  - other);
         }
        return ret;
     }

    @Override
    public String toString()
    {
       StringBuilder sb = new StringBuilder();
        sb.append("size:");
        sb.append(size());
        sb.append(" total:");
         sb.append(getTotal());
        sb.append(" duplicates:");
        sb.append(getDuplicates());
        return sb.toString();
    }
}
