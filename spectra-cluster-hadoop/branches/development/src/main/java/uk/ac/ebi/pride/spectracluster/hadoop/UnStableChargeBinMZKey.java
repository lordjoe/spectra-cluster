package uk.ac.ebi.pride.spectracluster.hadoop;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.UnStableChargeBinMZKey
 * key represents charge bin number, group and precursor mz
 * used for stable clusters guaranteed to sort late
 * must sort early
 * User: Steve
 * Date: 8/13/13
 * key object using charge,mz,peak
 */
public class UnStableChargeBinMZKey extends StableChargeBinMZKey implements Comparable<StableChargeBinMZKey> {

    public static final String SORT_PREFIX = "|"; // ascii decimal guaranteed to sort EARLY

    public UnStableChargeBinMZKey(int pCharge, int pBin, int pGroup, double pPrecursorMZ) {
        super(pCharge, pBin, pGroup, pPrecursorMZ);
    }


    /**
     * use  StableChargeBinMZKey.buildKey WHICH FIGURES OUT OF THE KEY US STABLE OR UNSTABLE
     * @param str  !null String generated by toString
     */
    protected UnStableChargeBinMZKey(String str) {
        super(str);
    }

    protected String getSortPrefix()
    {
        return  SORT_PREFIX;
    }


}
