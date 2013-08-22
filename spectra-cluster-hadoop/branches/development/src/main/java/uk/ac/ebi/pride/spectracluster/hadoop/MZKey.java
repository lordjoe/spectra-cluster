package uk.ac.ebi.pride.spectracluster.hadoop;



/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargeMZKey
 * User: Steve
 * Date: 8/13/13
 * key object using  mz
 */
public class MZKey implements Comparable<MZKey> {

    private final double precursorMZ;
    private String asString;

    @SuppressWarnings("UnusedDeclaration")
    public MZKey(final double pPrecursorMZ) {
        precursorMZ = pPrecursorMZ;
        asString = null;    // force string regeneration
     }

    @SuppressWarnings("UnusedDeclaration")
    public MZKey(String str) {
        precursorMZ = SpectraHadoopUtilities.keyToMZ(str);
        asString = null;    // force string regeneration
    }


    public double getPrecursorMZ() {
        return precursorMZ;
    }

    @Override
    public String toString() {
        if (asString == null) {
            StringBuilder sb = new StringBuilder();
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
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(final MZKey o) {
        return toString().compareTo(o.toString());
    }
}
