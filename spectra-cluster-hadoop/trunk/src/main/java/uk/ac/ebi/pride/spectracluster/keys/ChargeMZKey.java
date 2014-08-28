package uk.ac.ebi.pride.spectracluster.keys;

import uk.ac.ebi.pride.spectracluster.hadoop.*;
import uk.ac.ebi.pride.spectracluster.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargeMZKey
 * User: Steve
 * Date: 8/13/13
 * key object using charge,mz
 */
public class ChargeMZKey  implements Comparable<ChargeMZKey> {

    private final int charge;
    private final double precursorMZ;
    private String asString;

    public ChargeMZKey(final int pCharge, final double pPrecursorMZ) {
        charge = pCharge;
        precursorMZ = pPrecursorMZ;
        asString = null;    // force string regeneration
     }

    @SuppressWarnings("UnusedDeclaration")
    public ChargeMZKey(String str) {
        final String[] split = str.split(":");
        charge = Integer.parseInt(split[0]);
        precursorMZ = SpectraHadoopUtilities.keyToMZ(split[1]);
        asString = null;    // force string regeneration
    }

    public int getCharge() {
        return charge;
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
            double precursorMZ1 = getPrecursorMZ();
            String str = SpectraHadoopUtilities.mzToKey(precursorMZ1);
            sb.append(str);

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
    public int compareTo(final ChargeMZKey o) {
        return toString().compareTo(o.toString());
    }


    /**
     * here is an int that a partitioner would use
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getPartitionHash() {
        int ret = getCharge() * 10000;
        ret += (int)(getPrecursorMZ() * MZIntensityUtilities.MZ_RESOLUTION + 0.5);
        return ret;
    }

}
