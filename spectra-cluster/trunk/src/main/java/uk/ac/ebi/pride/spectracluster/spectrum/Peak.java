package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.CompareTo;
import uk.ac.ebi.pride.spectracluster.util.Constants;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class Peak implements IPeak {

    private final float massChargeRatio;
    private final float intensity;
    private final int count;

    public Peak(float massChargeRatio, float intensity) {
        this(massChargeRatio, intensity, 1);
    }

    public Peak(float massChargeRatio, float intensity, int count) {
        this.massChargeRatio = massChargeRatio;
        this.intensity = intensity;
        this.count = count;
    }

    /**
     * copy constructor
     *
     * @param copied
     */
    public Peak(IPeak copied) {
        this.massChargeRatio = copied.getMz();
        this.intensity = copied.getIntensity();
        this.count = copied.getCount();
    }

    public float getMz() {
        return massChargeRatio;
    }

    public float getIntensity() {
        return intensity;
    }


    public int getCount() {
        return count;
    }


    @Override
    public int compareTo(IPeak o) {
        if (Math.abs(getMz() - o.getMz()) > Constants.SMALL_MZ_DIFFERENCE)
            return CompareTo.compare(getMz(), o.getMz());
        if (Math.abs(getIntensity() - o.getIntensity()) > Constants.SMALL_INTENSITY_DIFFERENCE)
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
        return compareTo(other) == 0;
    }

    /**
     * return exactly what an MGF would use
     *
     * @return
     */
    @Override
    public String toString() {
        String mz = String.format("%10.5f", getMz()).trim();
        String intensity = String.format("%8.2f", getIntensity()).trim();
        return "m/z = " + mz + ", intensity = " + intensity + ", count = " + getCount();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Peak peak = (Peak) o;

        if (Float.compare(peak.intensity, intensity) != 0) return false;
        if (Float.compare(peak.massChargeRatio, massChargeRatio) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (massChargeRatio != +0.0f ? Float.floatToIntBits(massChargeRatio) : 0);
        result = 31 * result + (intensity != +0.0f ? Float.floatToIntBits(intensity) : 0);
        return result;
    }
}
