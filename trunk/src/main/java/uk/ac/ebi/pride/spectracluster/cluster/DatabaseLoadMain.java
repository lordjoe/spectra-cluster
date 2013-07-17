package uk.ac.ebi.pride.spectracluster.cluster;


import uk.ac.ebi.pride.spectracluster.datastore.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.sql.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.DatabaseLoadMain
 * main to use for loading an mgf file or a directory of mgf files into a database
 * User: Steve
 * Date: 7/5/13
 */
public class DatabaseLoadMain {



    private long readTimeMillisec;
    private final IMutableClusterDataStore database;


    public DatabaseLoadMain(String dbName) {
        DataSource ds = Defaults.INSTANCE.getDefaultDataSource();
        database = new SQLDataStore(dbName, ds);
    }

    public long getReadTimeMillisec() {
        return readTimeMillisec;
    }

    public void addReadTimeMillisec(final long pReadTimeMillisec) {
        readTimeMillisec += pReadTimeMillisec;
    }

    public IMutableClusterDataStore getDatabase() {
        return database;
    }

    /**
     * do the work of inserting all clusters in a file into a working database
     * @param inputFile  !null existing readable mgf file
     */
    protected void processFile(final File inputFile) {
        if(!inputFile.getName().toLowerCase().endsWith(".mgf"))
            return; // not an mgf

        long start = System.currentTimeMillis();
        final ISpectrum[] spectrums = ParserUtilities.readMGFScans(inputFile);
        IMutableClusterDataStore db = getDatabase();

        /**
         * Add your favorite clustering engine here
         */
     //     IClusteringEngine engine = new PRideC();
         long end = System.currentTimeMillis();
        final long readTIme = end - start;
        addReadTimeMillisec(readTIme);
        double seconds = (readTIme / 1000);
        double min = seconds / 60;
        System.out.println("read " + inputFile + " with " + spectrums.length + " spectra in " + String.format("%10.3f", seconds).trim());

        if (spectrums.length  == 0)     // nothing there
            return;
         start = System.currentTimeMillis();

        // Store all in the database
        List<ISpectrum> stored = Arrays.asList(spectrums);
        db.storeSpectra( stored);


        end = System.currentTimeMillis();
        seconds = ((end - start) / 1000);
        min = seconds / 60;
        System.out.println("inserted " + inputFile + " with " + spectrums.length + " spectra in " + String.format("%10.3f sec", seconds).trim());
    }


    /**
     * process every file in a directory containing mgf files
     * @param pF     !null existing directory
     */
    protected void processDirectory(final File pF) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min ;
        final File[] files = pF.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile())
                    processFile(file); // todo better directory handling
                end = System.currentTimeMillis();
                int seconds = (int) ((end - start) / 1000);
                min = seconds / 60;
            }
        }
    }


    protected static void usage() {
        System.out.println("Usage database_name <mgf file or directory> ...");
    }


    public static void main(String[] args) {
        if (args.length <  2) {
            usage();
            return;
        }


        DatabaseLoadMain mainClusterer = new DatabaseLoadMain(args[0]);
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min = 0;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg);
            if (!f.exists())
                throw new IllegalArgumentException("File " + arg + " does not exist");
            if (f.isDirectory())
                mainClusterer.processDirectory(f);
            else
                mainClusterer.processFile(f);

            end = System.currentTimeMillis();
            int seconds = (int) ((end - start) / 1000);
            min = seconds / 60;
        }
        double readMin = mainClusterer.getReadTimeMillisec()  / (60 * 1000 );
        System.out.println("read in " + String.format("%10.2f", readMin) + " min");
        System.out.println("Processed in " + String.format("%10.2f", min - readMin) + " min");
        System.out.println("Total " + String.format("%10.2f", min  ) + " min");
    }


}
