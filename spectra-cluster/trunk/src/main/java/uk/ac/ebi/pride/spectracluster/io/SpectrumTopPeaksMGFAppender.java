package uk.ac.ebi.pride.spectracluster.io;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.IOException;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumTopPeaksMGFAppender extends SpectrumMGFAppender {

    public static final int MAX_PEAKS_TO_KEEP = 100;

    private SpectrumTopPeaksMGFAppender() {
    }

    @Override
    protected void appendPeaks(ISpectrum spectrum, Appendable out) throws IOException {
        ISpectrum highestNPeaks = spectrum.getHighestNPeaks(MAX_PEAKS_TO_KEEP);
        for (IPeak peak : highestNPeaks.getPeaks()) {
            String line = String.format("%10.3f", peak.getMz()).trim() + "\t" +
                    String.format("%10.3f", peak.getIntensity()).trim();
            out.append(line);
            out.append("\n");
        }

    }
}
