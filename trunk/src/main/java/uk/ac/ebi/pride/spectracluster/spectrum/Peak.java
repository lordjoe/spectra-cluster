package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class Peak implements IPeak, Comparable<IPeak>  {

    private final double massChargeRatio;
    private   double intensity;
    private int count;

    public Peak(double massChargeRatio, double intensity) {
        this(massChargeRatio, intensity, 1);
    }

    public Peak(double massChargeRatio, double intensity, int count) {
        this.massChargeRatio = massChargeRatio;
        this.intensity = intensity;
        this.count = count;
    }

    public double getMz() {
        return massChargeRatio;
    }

    public double getIntensity() {
        return intensity;
    }

    /**
     * Set peak intensity
     */
    @Override
    public void setIntensity(double inx) {
        intensity = inx;

    }

    public int getCount() {
        return count;
    }


    @Override
    public int compareTo(IPeak o) {
        if (getMz() != o.getMz())
            return Double.compare(getMz(), o.getMz());
        if (getIntensity() != o.getIntensity())
            return Double.compare(getIntensity(), o.getIntensity());
        return 0;
    }

    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param other poiibly null other object
     * @return true if other is "similar enough to this"
     */
    @Override
    public boolean equivalent(IPeak other) {
        if (true) throw new UnsupportedOperationException("Fix This");
        return false;
    }


}
