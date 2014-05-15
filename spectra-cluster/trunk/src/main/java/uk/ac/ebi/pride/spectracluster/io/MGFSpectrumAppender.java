package uk.ac.ebi.pride.spectracluster.io;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.IOException;

/**
 * Write spectrum out as MGF peak list format
 *
 * @author Rui Wang
 * @version $Id$
 */
public class MGFSpectrumAppender implements ISpectrumAppender {

    @Override
    public void appendSpectrum(Appendable out, ISpectrum spectrum, Object... otherData) {
        try {
            out.append("BEGIN IONS");
            out.append("\n");

            appendTitle(spectrum, out);
            out.append("\n");

            double precursorCharge = spectrum.getPrecursorCharge();
            double massChargeRatio = spectrum.getPrecursorMz();

            out.append("PEPMASS=").append(String.valueOf(massChargeRatio));
            out.append("\n");

            out.append("CHARGE=").append(String.valueOf(precursorCharge));
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            appendPeaks(spectrum, out);

            out.append("END IONS");
            out.append("\n");
        } catch (IOException e) {
            throw new AppenderException(e);
        }
    }

    /**
     * override to add peptide later
     *
     * @param out
     * @throws IOException
     */
    private void appendTitle(final ISpectrum spectrum, final Appendable out) throws IOException {
        out.append("TITLE=id=").append(spectrum.getId());
    }

    protected void appendPeaks(final ISpectrum spectrum, final Appendable out) throws IOException {
        for (IPeak peak : spectrum.getPeaks()) {
            String line = String.format("%10.3f", peak.getMz()).trim() + "\t" +
                    String.format("%10.3f", peak.getIntensity()).trim();
            out.append(line);
            out.append("\n");
        }
    }

    @Override
    public void appendStart(Appendable out, Object... otherData) {

    }

    @Override
    public void appendEnd(Appendable out, Object... otherData) {

    }
}