package uk.ac.ebi.pride.spectracluster.export;

import uk.ac.ebi.pride.jmztab.model.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.MSRunProcessor
 *
 * @author Steve Lewis
 * @date 22/05/2014
 */
public class MSRunProcessor {

    private final MZTabProcessor exporter;
    private final Map<String,PSM>  spectrumToPSM = new HashMap<String, PSM>();

    public MSRunProcessor(MZTabProcessor exporter) {
        this.exporter = exporter;
    }

    public void addPSM(String spectrum,PSM added) {
        spectrumToPSM.put(spectrum,added);
    }


    public PSM getPSM(String spectrum ) {
        return spectrumToPSM.get(spectrum );
    }

}
