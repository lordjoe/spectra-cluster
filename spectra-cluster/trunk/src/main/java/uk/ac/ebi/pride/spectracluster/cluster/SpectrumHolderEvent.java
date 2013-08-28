package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumHolderEvent extends EventObject {

    /**
     * Event type enum
     */
    public enum Type {ADD, REMOVE};

    private final Type type;

    private final List<ISpectrum> spectra = new ArrayList<ISpectrum>();

    public SpectrumHolderEvent(ISpectrumHolder spectrumHolder, Type type, ISpectrum... spectra) {
        super(spectrumHolder);

        this.type = type;

        if (spectra != null) {
            this.spectra.addAll(Arrays.asList(spectra));
        }
    }

    public ISpectrumHolder getSpectrumHolder() {
        Object source = getSource();
        return source == null ? null : (ISpectrumHolder) source;
    }

    public Type getType() {
        return type;
    }

    public List<ISpectrum> getSpectra() {
        return Collections.unmodifiableList(spectra);
    }
}
