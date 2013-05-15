package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.ISpecClusterPeak
 *
 * @author Steve Lewis
 * @date 10/05/13
 */
public interface ISpecClusterPeak extends Comparable<ISpecClusterPeak>,
        Equivalent<ISpecClusterPeak> {
    public static ISpecClusterPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ISpecClusterPeak.class;

    public static final double SMALL_MZ_DIFFERENCE = 0.01;

    /**
     * comparator to compare peaks by intensity first then mz rather than the
     * standard mz then intensity
     *
     */
    public static final Comparator<ISpecClusterPeak> BY_INTENSITY = new Comparator<ISpecClusterPeak>() {
        @Override
        public int compare(ISpecClusterPeak o1, ISpecClusterPeak o2) {
            if(o2 == o1)
               return 0;
            if(o1.getIntensity() != o2.getIntensity()) {
                 return o2.getIntensity() < o1.getIntensity() ? -1 : 1;
             }
           if(o1.getMassChargeRatio() != o2.getMassChargeRatio()) {
               return o2.getMassChargeRatio() < o1.getMassChargeRatio() ? -1 : 1;
           }
           if(o1.getCount() != o2.getCount()) {
                return o2.getCount() < o1.getCount() ? -1 : 1;
            }
   
           return 0;
        }
    };
    
 

    public double getMassChargeRatio();

    public double getIntensity();

    /**
     * number associated spectra
     * @return
     */
    public int getCount();


}
