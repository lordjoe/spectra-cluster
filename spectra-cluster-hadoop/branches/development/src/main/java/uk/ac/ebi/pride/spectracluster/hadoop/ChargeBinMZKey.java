package uk.ac.ebi.pride.spectracluster.hadoop;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargePeakMZKey
 * key represents charge bin number and precursor mz
 * this change forces a commit
 * User: Steve
 * Date: 8/13/13
 * key object using charge,mz,peak
 */
public class ChargeBinMZKey implements Comparable<ChargeBinMZKey> {

    private final int charge;
    private final int bin;
    private final double precursorMZ;
    private String asString;
    private int partitionHash;

      public ChargeBinMZKey(final int pCharge, final int pBin, final double pPrecursorMZ) {
        charge = pCharge;
        bin = pBin;
        precursorMZ = pPrecursorMZ;
        asString = null;
    }

     public ChargeBinMZKey(String str) {
        final String[] split = str.split(":");
        charge = Integer.parseInt(split[0]);
        String key1 = split[1];
        bin = Integer.parseInt(key1);
        String key2 = split[2];
        precursorMZ = SpectraHadoopUtilities.keyToMZ(key2);
        asString = null;    // force string regeneration
    }

    public int getCharge() {
        return charge;
    }

    /**
     * MZ_RESOLUTION * peakMZ
     *
     * @return
     */
    public int getBin() {
        return bin;
    }

    /**
     * MZ_RESOLUTION * getPrecursorMZ
     *
     * @return
     */
    public double getPrecursorMZ() {
        return precursorMZ;
    }

    @Override
    public String toString() {
        if (asString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%02d", getCharge()));
            sb.append(":");
            sb.append(String.format("%06d", getBin()));
            // only include charge and bin
            partitionHash = sb.toString().hashCode();
            sb.append(":");
            sb.append(SpectraHadoopUtilities.mzToKey(getPrecursorMZ()));

            asString = sb.toString();
        }
        return asString;

    }

    @Override
    public boolean equals(final Object o) {
        if(o == null)
             return false;
        //noinspection SimplifiableIfStatement
        if (getClass() != o.getClass())
            return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }


    /**
     * sort by string works
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(final ChargeBinMZKey o) {
        return toString().compareTo(o.toString());
    }

    /**
     * here is an int that a partitioner would use
     *
     * @return
     */
    public int getPartitionHash() {
        //noinspection UnusedDeclaration
        int x = hashCode(); // force toString to be called once.
        return Math.abs(partitionHash);
    }
}
