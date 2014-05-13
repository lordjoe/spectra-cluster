package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideUseId {
    private final Map<String,Integer> peptideUseCount = new HashMap<String, Integer>();

    public String getPeptideId(String peptide)
    {
        Integer count = peptideUseCount.get(peptide);
        int ret = 1;
        if(count != null)     {
             ret =  count + 1;

        }
         peptideUseCount.put(peptide,ret);
        return peptide + "-" + ret;
    }
}
