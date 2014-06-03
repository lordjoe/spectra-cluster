package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.utilities.TypedCounter;
import com.lordjoe.utilities.TypedPredicateCounter;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.PSMSpectrum_similarity.PSMSpectrum_holder
 * User: Steve
 * Date: 2/3/14
 */
public class PSM_Holder {

    private final Map<String, Set<PSMSpectrum>> idToPSMSpectrum = new HashMap<String, Set<PSMSpectrum>>();
    private final Map<String, Set<PSMSpectrum>> peptideToPSMSpectrum = new HashMap<String, Set<PSMSpectrum>>();
    private Integer decoyCount;
    private Integer PSMSpectrumCount;


    public synchronized void addPSMSpectrum(PSMSpectrum added) {
        String id = added.getId();
        if (idToPSMSpectrum.containsKey(id)) {
            idToPSMSpectrum.get(id).add(added);
        } else {
            Set<PSMSpectrum> holder = new HashSet<PSMSpectrum>();
            holder.add(added);
            idToPSMSpectrum.put(id, holder);
        }
        String peptide = added.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);

        if (peptideToPSMSpectrum.containsKey(peptide)) {
            peptideToPSMSpectrum.get(peptide).add(added);
        } else {
            Set<PSMSpectrum> holder = new HashSet<PSMSpectrum>();
            holder.add(added);
            peptideToPSMSpectrum.put(peptide, holder);
        }
        decoyCount = null; // force recount
    }

    public Set<PSMSpectrum> getPSMSpectrums(String id) {
        Set<PSMSpectrum> psmSpectrums = idToPSMSpectrum.get(id);
        if (psmSpectrums == null) {
            psmSpectrums = new HashSet<PSMSpectrum>();
            psmSpectrums.add(PSMSpectrum.getSpectrum(id));
            idToPSMSpectrum.put(id, psmSpectrums);
        }
        return psmSpectrums;
    }

    public Set<PSMSpectrum> getAllSpectrums() {
        Set<PSMSpectrum> ret = new HashSet<PSMSpectrum>();
        for (String id : idToPSMSpectrum.keySet()) {
            ret.addAll(getPSMSpectrums(id));
        }
        return ret;
    }

    public Set<PSMSpectrum> getPSMSpectrumsByPeptide(String id) {
        return peptideToPSMSpectrum.get(id);
    }

    public synchronized void visit(TypedVisitor<PSMSpectrum>... visitors) {
        for (String key : idToPSMSpectrum.keySet()) {
            for (PSMSpectrum PSMSpectrum : idToPSMSpectrum.get(key)) {
                for (TypedVisitor<PSMSpectrum> visitor : visitors) {
                    visitor.visit(PSMSpectrum);
                }
            }
        }
    }

    public synchronized int getDecoyCount() {
        if (decoyCount == null) {
            TypedCounter<PSMSpectrum> total = new TypedCounter<PSMSpectrum>();
            TypedCounter<PSMSpectrum> decoys = new TypedPredicateCounter<PSMSpectrum>(PSMUtilities.IS_PSM_DECOY);
            visit(total, decoys);
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
