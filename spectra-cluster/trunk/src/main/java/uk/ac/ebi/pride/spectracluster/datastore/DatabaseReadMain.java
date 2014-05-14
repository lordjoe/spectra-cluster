package uk.ac.ebi.pride.spectracluster.datastore;


import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.DatabaseReadMain
 * main to use to test reading the database
 * User: Steve
 * Date: 7/5/13
 */
public class DatabaseReadMain {


    private long readTimeMillisec;
    private long storeTimeMillisec;
    private final ISpectrumDataStore database;
    private final List<SpectrumClusteringData> spectraData = new ArrayList<SpectrumClusteringData>();


    public DatabaseReadMain(String dbName) {
        DataSource ds = Defaults.INSTANCE.getDefaultDataSource();
        database = new SpectrumDataStore(dbName, ds);
    }

    public long getReadTimeMillisec() {
        return readTimeMillisec;
    }

    public void addReadTimeMillisec(final long pReadTimeMillisec) {
        readTimeMillisec += pReadTimeMillisec;
    }


    public long getStoreTimeMillisec() {
        return storeTimeMillisec;
    }

    public void addStoreTimeMillisec(final long pReadTimeMillisec) {
        storeTimeMillisec += pReadTimeMillisec;
    }

    public ISpectrumDataStore getDatabase() {
        return database;
    }


    /**
     * do the work of inserting all clusters in a file into a working database
     * only stores MAX_SPECTRA_TO_PROCESS_AT_ONCE ( 10,000 in memory)
     *
     * @param inputFile !null existing readable mgf file
     */
    protected void processFile(final File inputFile) {
        if (!inputFile.getName().toLowerCase().endsWith(".mgf"))
            return; // not an mgf

        int processedSpectra = 0;
        long readTime = 0;
        long storeTime = 0;
        double seconds = (readTime / 1000);
        double min = seconds / 60;
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        List<ISpectrum> holder = new ArrayList<ISpectrum>();

        ISpectrumDataStore db = getDatabase();

        MGFSpectrumIterable spcit = new MGFSpectrumIterable(inputFile);
        Iterator<ISpectrum> itr = spcit.iterator();

        while (itr.hasNext()) {
            start = System.currentTimeMillis();
            while (itr.hasNext()) {
                final ISpectrum next = itr.next();
                if (next instanceof IPeptideSpectrumMatch) {
                    spectraData.add(new SpectrumClusteringData((IPeptideSpectrumMatch) next));
                }
            }
            end = System.currentTimeMillis();
            long del = end - start;
            addReadTimeMillisec(del);
            readTime += del;
        }


        seconds = (readTime / 1000);
        min = seconds / 60;
        System.out.println("read " + inputFile + " with " + processedSpectra + " spectra in " + String.format("%10.3f", seconds).trim());

        start = System.currentTimeMillis();


        seconds = (storeTime / 1000);
        min = seconds / 60;
        System.out.println("inserted " + inputFile + " with " + processedSpectra + " spectra in " + String.format("%10.3f sec", seconds).trim());
    }


    /**
     * process every file in a directory containing mgf files
     *
     * @param pF !null existing directory
     */
    protected void processDirectory(final File pF) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min;
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

    protected List<SpectrumClusteringData> internalGetSpectraData() {
        return spectraData;
    }

    protected static void usage() {
        System.out.println("Usage database_name <mgf file or directory> ...");
    }


    /**
     * try to see if a medium suzed 200K spectrum can be characterized in memory
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            return;
        }


        DatabaseReadMain mainClusterer = new DatabaseReadMain(args[0]);
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min = 0;

        final ISpectrumDataStore db = mainClusterer.getDatabase();

        int nSpectra = 0;
        for (ISpectrum spc : db.getAllSpectra()) {
            SpectrumClusteringData clus = new SpectrumClusteringData((IPeptideSpectrumMatch) spc);
            mainClusterer.internalGetSpectraData().add(clus);
            nSpectra++;
        }
        end = System.currentTimeMillis();

        double readMin = (end - start) / (60 * 1000);
        System.out.println("read in " + String.format("%10.2f", readMin) + " min");
        System.out.println("Processed in " + String.format("%10.2f", min - readMin) + " min");
        System.out.println("Total " + String.format("%10.2f", min) + " min");
    }


}
