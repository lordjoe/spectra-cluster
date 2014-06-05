package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.filters.*;
import uk.ac.ebi.pride.spectracluster.filter.*;
import uk.ac.ebi.pride.spectracluster.retrievers.*;
import uk.ac.ebi.pride.spectracluster.filters.*;

import java.io.*;
import java.util.*;
/**
 * uk.ac.ebi.pride.spectracluster.export.Exporter
 *
 * @author Steve Lewis
 * @date 21/05/2014
 */
public class Exporter {

    private static String onlyExportedTaxonomy;

    public static String getOnlyExportedTaxonomy() {
        return onlyExportedTaxonomy;
    }

    public static void setOnlyExportedTaxonomy(String onlyExportedTaxonomy) {
        if(onlyExportedTaxonomy== null){
            Exporter.onlyExportedTaxonomy = null;
            return;
          }
        if(onlyExportedTaxonomy.equals( Exporter.onlyExportedTaxonomy))
            return;
        if(Exporter.onlyExportedTaxonomy != null)   {
            Exporter.onlyExportedTaxonomy = null;
        }
        else {
            Exporter.onlyExportedTaxonomy = onlyExportedTaxonomy;
        }
    }

    /**
     * @param directory
     * @return given a project
     */
    public static File fromDirectory(File baseDirectory,File directory) {
            if(!baseDirectory.exists() && !baseDirectory.mkdirs())
               throw new IllegalStateException("bad base directory");
            final String child = directory.getName() + ".mgf";
            File outFile = new File(baseDirectory, child);
            return outFile;
       }

    private final TypedFilterCollection filters;
    private final File outputDirectory;
    private final File activeDirectory;
    private final String experimentId;

    public Exporter(File outputDirectory,File activeDirectory,TypedFilterCollection filt) {
        filters = filt;
        this.outputDirectory = outputDirectory;
        this.activeDirectory = activeDirectory;
        String name = activeDirectory.getAbsolutePath();
        name = name.replace("\\", "/");
        name = name.replace("/generated", "");
        experimentId = name.substring(name.lastIndexOf("/") + 1);

    }

    public TypedFilterCollection getFilters() {
        return filters;
    }

    public File getActiveDirectory() {
        return activeDirectory;
    }

   @SuppressWarnings("UnusedDeclaration")
    public String getExperimentId() {
        return experimentId;
    }


    public void exportDirectory() {
        final File outFile = fromDirectory(outputDirectory,activeDirectory);
        int numberWritten = 0;
        PrintWriter out = null;
        try {
              out =  new PrintWriter(new FileWriter(outFile ));
              final List<File> mzTabFiles = MZTabhandler.getMZTabFiles(activeDirectory);
            if(mzTabFiles.size() == 0)
                throw new IllegalStateException("Bad or Empty input directory " + activeDirectory.getAbsolutePath()); // ToDo change
            for (File mzTabFile : mzTabFiles) {
                System.out.println(mzTabFile.getAbsolutePath());
                MZTabProcessor processor = new MZTabProcessor(this, mzTabFile);
                final String onlyExportedTaxonomy1 = getOnlyExportedTaxonomy();
                final String accession = processor.getAccession();
                if(onlyExportedTaxonomy1 != null) {
                      if(!onlyExportedTaxonomy1.equals(accession))
                        continue; // skip files wrong taxomony
                }
                numberWritten += processor.handleCorrespondingMGFs(out);
              }
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            out.close();
            if(numberWritten == 0)
                outFile.delete();

        }
    }

    private static TypedFilterCollection buildFilters(File file) {
         @SuppressWarnings("UnnecessaryLocalVariable")
        final TypedFilterCollection parse = TypedFilterCollection.parse(file);
        return parse;
    }

    /**
     * usage outputDirectory filterFile directoryToProcess
     * @param args
     */
    public static void main(String[] args) {
        int index = 0;
        File outputDirectory =  new File(args[index++]);
        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        TypedFilterCollection filters = buildFilters(new File(args[index++]));
        for ( ; index < args.length; index++) {
            String arg = args[index];
            File dir = new File(arg);
            Exporter exp = new Exporter(outputDirectory,dir,filters);
            exp.exportDirectory();

        }
     //   MaximialPeakFilter.showStatistics(System.out);

    }

 }
