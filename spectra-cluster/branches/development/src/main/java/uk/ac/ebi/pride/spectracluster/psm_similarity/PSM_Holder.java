package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.utilities.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.PSMSpectrum_similarity.PSMSpectrum_holder
 * User: Steve
 * Date: 2/3/14
 */
public class PSM_Holder {

    private final Map<String,Set<PSMSpectrum>> idToPSMSpectrum = new HashMap<String, Set<PSMSpectrum>>();
    private final Map<String,Set<PSMSpectrum>> peptideToPSMSpectrum = new HashMap<String, Set<PSMSpectrum>>();
    private Integer decoyCount;
    private Integer PSMSpectrumCount;



    public synchronized void addPSMSpectrum(PSMSpectrum added)   {
        String id = added.getId();
        if(idToPSMSpectrum.containsKey(id))   {
            idToPSMSpectrum.get(id).add(added);
        }
        else {
            Set<PSMSpectrum> holder = new HashSet<PSMSpectrum>();
            holder.add(added);
            idToPSMSpectrum.put(id, holder);
        }
        String peptide = added.getPeptide();

        if(peptideToPSMSpectrum.containsKey(peptide))   {
            peptideToPSMSpectrum.get(peptide).add(added);
        }
        else {
            Set<PSMSpectrum> holder = new HashSet<PSMSpectrum>();
             holder.add(added);
            peptideToPSMSpectrum.put(peptide, holder);
        }
       decoyCount = null; // force recount
    }

    public Set<PSMSpectrum> getPSMSpectrums(String id)   {
         return idToPSMSpectrum.get(id);
     }

    public Set<PSMSpectrum> getPSMSpectrumsByPeptide(String id)   {
         return peptideToPSMSpectrum.get(id);
     }

    public synchronized void visit( TypedVisitor<PSMSpectrum>... visitors) {
        for(String key : idToPSMSpectrum.keySet())  {
            for (PSMSpectrum PSMSpectrum : idToPSMSpectrum.get(key)) {
                for (TypedVisitor<PSMSpectrum> visitor : visitors) {
                    visitor.visit(PSMSpectrum);
                }
            }
        }
    }

    public synchronized int getDecoyCount() {
        if(decoyCount == null)  {
            TypedCounter<PSMSpectrum> total =  new TypedCounter<PSMSpectrum>();
            TypedCounter<PSMSpectrum> decoys =  new TypedPredicateCounter<PSMSpectrum>(PSMUtilities.IS_PSM_DECOY);
            visit(total,decoys);
            decoyCount = decoys.getCount();
            PSMSpectrumCount = total.getCount();

        }
        return decoyCount;
    }

    public int getPSMSpectrumCount() {
        getDecoyCount(); // force a real count
        return PSMSpectrumCount;
    }
}
