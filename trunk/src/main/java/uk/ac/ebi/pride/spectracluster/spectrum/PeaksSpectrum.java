package uk.ac.ebi.pride.spectracluster.spectrum;



import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.PeaksSpectrum
 * User: Steve
 * Date: 6/20/13
 */
public class PeaksSpectrum implements IPeaksSpectrum {

    /**
     * who knows why Johannes does this but we can as well
     * todo generalize
     *
     * @param p1
     * @return
     */
    public static double johannesIntensityConverted(IPeak p1) {
        double intensity = p1.getIntensity();
        if (intensity == 0)
            return 0;
        double intensity2 = 1 + Math.log(intensity);
        return intensity2;

    }


    public static final PeaksSpectrum[] EMPTY_ARRAY = {};
    private final String id;
    private final double precursorCharge;
    private final double precursorMz;
    /**
     * for calculate similarity between spectra, all peaks should be sorted by intensity
     */
    private final List<IPeak> peaks = new ArrayList<IPeak>();
    private final double totalIntensity;
    private final double sumSquareIntensity;


    public PeaksSpectrum(final String pId, final double pPrecursorCharge, final double pPrecursorMz, List<IPeak> peaks) {
        id = pId;
        precursorCharge = pPrecursorCharge;
        precursorMz = pPrecursorMz;
        this.peaks.clear();
        Collections.sort(peaks);
        this.peaks.addAll(peaks);
        Collections.sort(this.peaks, PeakMzComparator.getInstance());
        double  totalIntensityX = 0;
        double sumSquareIntensityX = 0;
         for (IPeak peak : peaks) {
             double intensity = peak.getIntensity();
             totalIntensityX += intensity;
             // johannes uses this in his dotProduct // todo generalize for other algorithms
             double ji = johannesIntensityConverted(peak);
             sumSquareIntensityX += ji * ji;
         }
        totalIntensity  = totalIntensityX;
        sumSquareIntensity = sumSquareIntensityX;
    }

    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public PeaksSpectrum(ISpectrum spectrum) {
        this(spectrum, spectrum.getPeaks());
    }

    /**
     * copy with different peaks
     *
     * @param spectrum base used for charge, mz
     * @param peaks    new peaks
     */
    public PeaksSpectrum(ISpectrum spectrum, List<IPeak> inpeaks) {

        this.id = spectrum.getId();
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.precursorMz = spectrum.getPrecursorMz();

        peaks.clear();
        Collections.sort(inpeaks);

        peaks.addAll(inpeaks);

        Collections.sort(this.peaks, PeakMzComparator.getInstance());
        double  totalIntensityX = 0;
        double sumSquareIntensityX = 0;
         for (IPeak peak : peaks) {
             double intensity = peak.getIntensity();
             totalIntensityX += intensity;
             // johannes uses this in his dotProduct // todo generalize for other algorithms
             double ji = johannesIntensityConverted(peak);
             sumSquareIntensityX += ji * ji;
         }
        totalIntensity  = totalIntensityX;
        sumSquareIntensity = sumSquareIntensityX;

    }




    protected void makeCalculations() {
     }


    public String getId() {
        return id;
    }

    public double getPrecursorMz() {
        return precursorMz;
    }

    public double getPrecursorCharge() {
        return precursorCharge;
    }

    public double getTotalIntensity() {
        //    guaranteeClean();
        return totalIntensity;
    }

    /**
     * return the sum  Square of all intensities
     */
    public double getSumSquareIntensity() {
        //  guaranteeClean();
        return sumSquareIntensity;
    }

    /**
     * return an unmodifiable version of the internal list
     * ??? should this be a copy
     * @return as above
     */
    @Override
    public List<IPeak> getPeaks() {
        // guaranteeClean();
        return  Collections.unmodifiableList(peaks); //  Collections.unmodifiableList(new ArrayList<IPeak>(peaks)) ;    todo or should I do a copy
    }

    /**
     * return internal array - use internally when safe
     *
     * @return
     */
    protected List<IPeak> internalGetPeaks() {
        // guaranteeClean();
        return peaks;
    }

    /**
     * return number of peaks
     *
     * @return count
     */
    public int getPeaksCount() {
        //   guaranteeClean();
        return peaks.size();
    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendMGF(Appendable out) {


        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            double precursorCharge = getPrecursorCharge();
            double massChargeRatio = getPrecursorMz();

            out.append("PEPMASS=" + massChargeRatio);
            out.append("\n");

            out.append("CHARGE=" + precursorCharge);
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            for (IPeak peak : internalGetPeaks()) {
                String line = String.format("%10.3f", peak.getMz()).trim() + "\t" +
                        String.format("%10.3f", peak.getIntensity()).trim();
                out.append(line);
                out.append("\n");
            }
            out.append("END IONS");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    @Override
    public String toString() {
        return getId();
    }


    /**
     * natural sort order is first charge then mz
     * finally compare id
     *
     * @param o !null other spectrum
     * @return as above
     */
    @Override
    public int compareTo(ISpectrum o) {
        if (this == o)
            return 0;
        //    guaranteeClean();
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return getPrecursorCharge() < o.getPrecursorCharge() ? -1 : 1;
        if (getPrecursorMz() != o.getPrecursorMz())
            return getPrecursorMz() < o.getPrecursorMz() ? -1 : 1;

        return getId().compareTo(o.getId());


    }


    /**
     * like equals but weaker - says other is equivalent to this
     *
     * @param o poiibly null other object
     * @return true if other is "similar enough to this"
     */
    public boolean equivalent(ISpectrum o) {
        if (o == this)
            return true;
        //   guaranteeClean();
        if (o.getPrecursorMz() != getPrecursorMz()) {
            return false;
        }
        final List<IPeak> iPeaks = internalGetPeaks();
        IPeak[] peaks = iPeaks.toArray(new IPeak[iPeaks.size()]);
        IPeak[] peaks1;
        if (o instanceof PeaksSpectrum) {
            final List<IPeak> iPeaks1 = ((PeaksSpectrum) o).internalGetPeaks();
            peaks1 = iPeaks1.toArray(new IPeak[iPeaks1.size()]);

        }
        else {
            final List<IPeak> peaks2 = o.getPeaks();
            peaks1 = peaks2.toArray(new IPeak[peaks2.size()]);

        }
        if (peaks.length != peaks1.length) {
            return false;
        }

        for (int i = 0; i < peaks1.length; i++) {
            IPeak pk0 = peaks[i];
            IPeak pk1 = peaks1[i];
            if (!pk0.equivalent(pk1))
                return false;
        }

        return true;
    }

}
