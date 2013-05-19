package uk.ac.ebi.pride.spectracluster;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;

///**
// * uk.ac.ebi.pride.spectracluster.SpecClusterPeak
// *    represents a peak - sorts by mz,intensity,count
// * @author Steve Lewis
// * @date 10/05/13
// */
//public class SpecClusterPeak implements IPeak {
//
//    private final double m_MassChargeRatio;
//    private final int m_Count;
//    private final double m_Intensity;
//
//
//    /**
//     * default count to 1
//     * @param massChargeRatio
//     * @param intensity
//     */
//    public SpecClusterPeak(double massChargeRatio, double intensity ) {
//        this(massChargeRatio, intensity, 1);
//    }
//
//    /**
//     *
//     * @param massChargeRatio
//     * @param intensity
//     * @param count
//     */
//    public SpecClusterPeak(double massChargeRatio, double intensity, int count) {
//        m_MassChargeRatio = massChargeRatio;
//        m_Count = count;
//        m_Intensity = intensity;
//    }
//
//    public double getMz() {
//        return m_MassChargeRatio;
//    }
//
//    public double getIntensity() {
//          return m_Intensity;
//    }
//
//    /**
//     * number associated spectra
//     *
//     * @return
//     */
//    public int getCount() {
//         return m_Count;
//    }
//
//
//    public int compareTo(IPeak o) {
//        if(o == this)
//            return 0;
//        if(o.getMz() != getMz()) {
//            return getMz() < o.getMz() ? -1 : 1;
//        }
//        if(o.getIntensity() != getIntensity()) {
//             return getIntensity() < o.getIntensity() ? -1 : 1;
//         }
//        if(o.getCount() != getCount()) {
//             return getCount() < o.getCount() ? -1 : 1;
//         }
//
//        return 0;
//    }
//
//    /**
//     * like equals but weaker - says other is equivalent to this
//     *  Here only MZ matters
//     * @param other poiibly null other object
//     * @return true if other is "similar enough to this"
//     */
//    public boolean equivalent(IPeak other) {
//        if (other == null)
//            return false;
//        if(Math.abs((getMz() - other.getMz())) > SMALL_MZ_DIFFERENCE)
//            return false;
//         // we dont care about intensity or count
//         return true;
//    }
//
//    /**
//     * return exactly what an MGF would use
//     * @return
//     */
//    @Override
//    public String toString() {
//
//        String item = String.format("%10.5f",  getMz()).trim();
//        String item2 = String.format("%8.2f",  getIntensity()).trim();
//        return item + "\t" + item2;
//     }
//}
