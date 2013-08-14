package uk.ac.ebi.pride.spectracluster.hadoop;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargePeakMZKey
 * User: Steve
 * Date: 8/13/13
 * key object using charge,mz,peak
 */
public class ChargePeakMZKey implements  Comparable<ChargePeakMZKey>{

    private final int charge;
    private final double peakMZ;
    private final double precursorMZ;
    private String asString;

    @SuppressWarnings("UnusedDeclaration")
    public ChargePeakMZKey(final int pCharge, final double pPeakMZ, final double pPrecursorMZ) {
        charge = pCharge;
        peakMZ = pPeakMZ;
        precursorMZ = pPrecursorMZ;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ChargePeakMZKey(String str) {
        final String[] split = str.split(":");
        charge = Integer.parseInt(split[0]);
        peakMZ = SpectraHadoopUtilities.keyToMZ(split[1]);
        precursorMZ = SpectraHadoopUtilities.keyToMZ(split[2]);
    }

    public int getCharge() {
        return charge;
    }

    public double getPeakMZ() {
        return peakMZ;
    }

    public double getPrecursorMZ() {
        return precursorMZ;
    }

    @Override
    public String toString() {
        if (asString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%02d", getCharge()));
             sb.append(":");
            sb.append(SpectraHadoopUtilities.mzToKey(getPeakMZ()));
            sb.append(":");
            sb.append(SpectraHadoopUtilities.mzToKey(getPrecursorMZ()));

            asString = sb.toString();
        }
        return asString;

    }

    @Override
    public boolean equals(final Object o) {
         //noinspection SimplifiableIfStatement
        if(getClass() != o.getClass())
             return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }


    /**
     * sort by string works
     * @param o
     * @return
     */
    @Override
    public int compareTo(final ChargePeakMZKey o) {
        return toString().compareTo(o.toString());
    }
}
