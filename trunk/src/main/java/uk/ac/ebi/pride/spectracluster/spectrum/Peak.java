package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class Peak implements IPeak {

    private double massChargeRatio;
    private double intensity;
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

    public void setMz(double mz) {
        this.massChargeRatio = mz;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peak)) return false;

        Peak peak = (Peak) o;

        if (count != peak.count) return false;
        if (Double.compare(peak.intensity, intensity) != 0) return false;
        if (Double.compare(peak.massChargeRatio, massChargeRatio) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(massChargeRatio);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(intensity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + count;
        return result;
    }
}
