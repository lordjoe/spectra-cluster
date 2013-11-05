package uk.ac.ebi.pride.spectracluster.hadoop;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.StableChargeBinMZKey
 * key represents charge bin number, group and precursor mz
 * used for stable clusters guaranteed to sort late
 * User: Steve
 * Date: 8/13/13
 * key object using charge,mz,peak
 */
public class StableChargeBinMZKey implements Comparable<StableChargeBinMZKey> {



    /**
     * figre out
     * @param s
     * @return
     */
    public static StableChargeBinMZKey buildKey(String s) {
        if(s.contains(SORT_PREFIX))
            return new StableChargeBinMZKey(s);
        if(s.contains(UnStableChargeBinMZKey.SORT_PREFIX))
            return new UnStableChargeBinMZKey(s);
        throw new IllegalArgumentException("Not stable or unstable key");
    }
      public static final String SORT_PREFIX = "|"; // ascii decimal guaranteed to sort LATE


    private final int charge;
    private final int bin;
    private final int group;
    private final double precursorMZ;
    private String asString;
    private int partitionHash;

    public StableChargeBinMZKey(final int pCharge, final int pBin, int pGroup, final double pPrecursorMZ) {
        charge = pCharge;
        bin = pBin;
        group = pGroup;
        precursorMZ = pPrecursorMZ;
        asString = null;
    }

    /**
     * use  StableChargeBinMZKey.buildKey WHICH FIGURES OUT OF THE KEY US STABLE OR UNSTABLE
     * @param str  !null String generated by toString
     */
    protected StableChargeBinMZKey(String str) {
         final String[] split = str.split(":");
        int index = 0;
        String prefix = split[index++];
        // todo - check for proper

        charge = Integer.parseInt(split[index++]);
        String key1 = split[index++];
        bin = Integer.parseInt(key1);
        String key2 = split[index++];
        group = Integer.parseInt(key2);

         String key3 = split[index++];
        precursorMZ = SpectraHadoopUtilities.keyToMZ(key3);
        asString = null;    // force string regeneration
    }

    /**
     * are these two comparable
     * @param o
     * @return
     */
    public boolean inSameGroup( StableChargeBinMZKey o)
      {
          if(getCharge() != o.getCharge())
                return false;
          if(getBin() != o.getBin())
                return false;
          if(getGroup() != o.getGroup())
                return false;
          return true;
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

    public int getGroup() {
        return group;
    }

    protected String getSortPrefix()
    {
        return  SORT_PREFIX;
    }

    @Override
    public String toString() {
        if (asString == null) {
            StringBuilder sb = new StringBuilder();
             sb.append(String.format("%02d", getCharge()));
            sb.append(":");
            sb.append(String.format("%06d", getBin()));
            sb.append(":");
            sb.append(String.format("%06d", getGroup()));

            // only include charge and bin
            String part = sb.toString();
            partitionHash = part.hashCode();

            sb = new StringBuilder();
            sb.append(getSortPrefix());
            // ok after partition sort first by prefix then by mz
            sb.append(":");
            sb.append(part);

            sb.append(":");
            double precursorMZ1 = getPrecursorMZ();
            sb.append(SpectraHadoopUtilities.mzToKey(precursorMZ1));

            asString = sb.toString();
        }
        return asString;

    }

    @Override
    public boolean equals(final Object o) {
        if (o == null)
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
    public int compareTo(final StableChargeBinMZKey o) {
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
