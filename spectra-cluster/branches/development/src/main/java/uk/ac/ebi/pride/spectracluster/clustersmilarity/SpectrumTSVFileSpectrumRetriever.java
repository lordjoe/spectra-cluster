package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumTSVFileSpectrumRetriever implements IMutableSpectrumRetriever {

    private final PrintWriter writer;
    private int count;

    public SpectrumTSVFileSpectrumRetriever(File file){
        try {
            writer = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSpectra(IPeptideSpectrumMatch... spectra) {


        for (IPeptideSpectrumMatch spectrumMatch : spectra) {
            addSpectrum(spectrumMatch);
        }

    }

    public void addSpectrum(IPeptideSpectrumMatch spectrumMatch) {
        StringBuilder sb = new StringBuilder();

        sb.append(spectrumMatch.getId()).append("\t")
                .append(spectrumMatch.getPrecursorCharge()).append("\t")
                .append(spectrumMatch.getPrecursorMz()).append("\t")
                .append(spectrumMatch.getPeptide());

        writer.println(sb.toString());
        if(count++ % 100 == 0)     {
            System.out.print(".");
            if(count > 10000 ) {
                System.out.println();
                writer.flush();
                count = 0;
            }
        }
    }

    @Override
    public IPeptideSpectrumMatch retrieve(String spectrumId) {
        throw new UnsupportedOperationException("Not supported");
    }

    public void close() {
        writer.close();
    }

    public static void main(String[] args) {
        SpectrumTSVFileSpectrumRetriever spectrumRetriever = new SpectrumTSVFileSpectrumRetriever(new File(args[0]));
        ClusterSimilarityUtilities.buildFromMgfFile(new File(args[1]), spectrumRetriever);
        spectrumRetriever.close();
    }
}
