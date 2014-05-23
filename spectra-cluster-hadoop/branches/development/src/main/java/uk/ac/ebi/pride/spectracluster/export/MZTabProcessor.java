package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.filters.*;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.spectracluster.retrievers.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.MSRunProcessor
 *
 * @author Steve Lewis
 * @date 22/05/2014
 */
public class MZTabProcessor {
    public static final int MAX_MS_RUNS = 1000;


    private final Exporter exporter;
    @SuppressWarnings("UnusedDeclaration")
    private Map<String, Protein> idToProtein = new HashMap<String, Protein>();
    private Map<Integer, MSRunProcessor> idToProcessor = new HashMap<Integer, MSRunProcessor>();
    private Map<MsRun, File> msRunFiles = new HashMap<MsRun, File>();
    private final MZTabhandler tabHandler;

    public MZTabProcessor(Exporter exporter, File mzTabFile) {
        this.exporter = exporter;
        tabHandler = new MZTabhandler(mzTabFile);
        addTabHandler(tabHandler);
    }

    @SuppressWarnings("UnusedDeclaration")
    public Exporter getExporter() {
        return exporter;
    }

    public MZTabhandler getTabHandler() {
        return tabHandler;
    }

    protected void buildMSRunFiles(final MZTabhandler e) {

        String fileName = "";
        final SortedMap<Integer, MsRun> msRunMap = e.getMzTabs().getMetadata().getMsRunMap();
        for (int i = 1; i < MAX_MS_RUNS; i++) {
            final MsRun msRun = msRunMap.get(i);
            if (msRun == null)
                break;
            idToProcessor.put(i, new MSRunProcessor(this));
            final URL location = msRun.getLocation();
            fileName = location.getPath();
            fileName = fileName.substring(fileName.lastIndexOf("/"));
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            fileName = fileName.replace(".xml", ""); // drop .xml
            final File baseDirectory = getExporter().getActiveDirectory();
            final File generatedDirectory = new File(baseDirectory, "generated");
            final File e1 = new File(generatedDirectory, fileName + ".pride.mgf");
            msRunFiles.put(msRun,e1);
        }

    }

    @SuppressWarnings("UnusedDeclaration")
    public Protein getProtein(String accession) {
        return idToProtein.get(accession);
    }

    public void handleCorrespondingMGFs(Appendable out) {
        for (MsRun run : msRunFiles.keySet()) {
            handleMFGFile(run, out);
        }

    }

    protected void handleMFGFile(MsRun run , Appendable out) {
        try {
            int numberProcessed = 0;
            MSRunProcessor processor = idToProcessor.get(run.getId());
            File file = msRunFiles.get(run);
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
             IPeptideSpectrumMatch psm = ParserUtilities.readMGFScan(rdr);
            while (psm != null) {
                processPSM(psm,processor, out);
                psm = ParserUtilities.readMGFScan(rdr);
                numberProcessed++;
            }
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    protected PSM getPSM(String peptideId,MSRunProcessor processor) {
        int index = peptideId.indexOf("spectrum=");
        if (index < 0)
            return null;
        String number = peptideId.substring(index + "spectrum=".length());
        final PSM psm = processor.getPSM(number);
        return psm;
    }

    protected void processPSM(IPeptideSpectrumMatch spectrum,MSRunProcessor processor, Appendable out) {
        final String id = spectrum.getId();

        PSM peptide = getPSM(id,processor);
        if(peptide == null)
            return;

        final TypedFilterCollection filters = getExporter().getFilters();
       ISpectrum passed = (ISpectrum)filters.passes(spectrum);
        if(passed != null)
            spectrum.appendMGF(out);
      }

    protected void addTabHandler(MZTabhandler tab) {
        final MZTabFile mzTabs = tab.getMzTabs();
        buildMSRunFiles(tab);
        final Collection<PSM> psMs = mzTabs.getPSMs();
        handleProteins(mzTabs);
        handlePSMs(psMs);

    }

    protected void handlePSMs(Collection<PSM> psMs) {
        for (PSM psM : psMs) {
            final String psm_id = psM.getPSM_ID();
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            final String sequence = psM.getSequence();
            final SplitList<SpectraRef> spectraRef = psM.getSpectraRef();
            for (SpectraRef ref : spectraRef) {
                final MsRun msRun = ref.getMsRun();
                final MSRunProcessor msRunProcessor = idToProcessor.get(msRun.getId());
                //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                final String reference = ref.getReference();
                msRunProcessor.addPSM(psm_id, psM);
            }

        }
    }

    protected void handleProteins(MZTabFile mzTabs) {
        final Collection<Protein> proteins = mzTabs.getProteins();
        for (Protein protein : proteins) {
            final String accession = protein.getAccession();
            idToProtein.put(accession, protein);
        }
    }
}
