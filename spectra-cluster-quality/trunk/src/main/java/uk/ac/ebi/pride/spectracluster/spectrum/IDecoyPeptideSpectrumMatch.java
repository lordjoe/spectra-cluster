package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IDecoyPeptideSpectrumMatch extends ISpectrum {


    /**
     * true if we know this is a decoy
     *
     * @return
     */
    public boolean isDecoy();
}
