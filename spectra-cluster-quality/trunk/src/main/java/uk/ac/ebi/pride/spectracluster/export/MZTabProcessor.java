package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.filters.*;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.spectracluster.io.*;
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
    private String accession;

    public MZTabProcessor(Exporter exporter, File mzTabFile) {
        this.exporter = exporter;
        tabHandler = new MZTabhandler(mzTabFile);
        if(tabHandler.getMzTabs() == null)
            return;
        addTabHandler(tabHandler);
    }

    public String getAccession() {
        return accession;
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
    public Protein getProtein(String proteinAccession) {
        return idToProtein.get(proteinAccession);
    }

    public int handleCorrespondingMGFs(Appendable out) {
        int totalWritten = 0;
        for (MsRun run : msRunFiles.keySet()) {
            totalWritten += handleMFGFile(run, out);
        }
        return totalWritten;
    }

    protected int handleMFGFile(MsRun run , Appendable out) {
        int numberProcessed = 0;
        int totalWritten = 0;
        try {
             MSRunProcessor processor = idToProcessor.get(run.getId());
            File file = msRunFiles.get(run);
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
             ISpectrum psm = ParserUtilities.readMGFScan(rdr);
            while (psm != null) {
                if(processPSM(psm,processor, out))
                    totalWritten++ ;
                psm = ParserUtilities.readMGFScan(rdr);
                numberProcessed++;
            }
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
        return totalWritten;
    }

    protected PSM getPSM(String peptideId,MSRunProcessor processor) {
        int index = peptideId.indexOf("spectrum=");
        if (index < 0)
            return null;
        String number = peptideId.substring(index + "spectrum=".length());
        final PSM psm = processor.getPSM(number);
        return psm;
    }

    protected boolean processPSM(ISpectrum spectrum,MSRunProcessor processor, Appendable out) {
        final String id = spectrum.getId();

        if(getAccession() != null)
            spectrum.setProperty(KnownProperties.TAXONOMY_KEY,getAccession());

        PSM peptide = getPSM(id,processor);
        if(peptide != null) {
            final String sequence = peptide.getSequence();
            spectrum.setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY,sequence);
            String proteinAccession = peptide.getAccession();
            if(proteinAccession != null)   {
                final Protein protein = idToProtein.get(proteinAccession);
                if(protein != null)  {
                    final String database = protein.getDatabase();
                    spectrum.setProperty(KnownProperties.PROTEIN_KEY,database + ":" + proteinAccession);
                }
            }
          }

        final TypedFilterCollection filters = getExporter().getFilters();
        ISpectrum passed = (ISpectrum)filters.passes(spectrum);
        final boolean ret = passed != null;
        if(ret)
            MGFSpectrumAppender.INSTANCE.appendSpectrum(out,spectrum);

        return ret; // true if appended
      }

    protected void addTabHandler(MZTabhandler tab) {
        final MZTabFile mzTabs = tab.getMzTabs();
        setAccessionFromHeader(mzTabs);
         buildMSRunFiles(tab);
        final Collection<PSM> psMs = mzTabs.getPSMs();
        handleProteins(mzTabs);
        handlePSMs(psMs);

    }

    private void setAccessionFromHeader(MZTabFile mzTabs) {
        final Metadata metadata = mzTabs.getMetadata();
        final SortedMap<Integer, Sample> sampleMap = metadata.getSampleMap();
        if(sampleMap.size() == 1)  {
            for (Integer index : sampleMap.keySet()) {
             Sample s = sampleMap.get(index);
                final List<Param> speciesList = s.getSpeciesList();
                if(speciesList.size() == 1)  {
                    Param specias = speciesList.get(0);
                    accession = specias.getAccession();
                }
            }

        }
        else {
            System.out.println("More than 1 sample");
        }
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
