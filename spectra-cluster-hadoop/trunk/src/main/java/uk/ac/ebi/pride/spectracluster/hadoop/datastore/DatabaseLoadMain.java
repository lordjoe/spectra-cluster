package uk.ac.ebi.pride.spectracluster.hadoop.datastore;


import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumIterable;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.DatabaseLoadMain
 * main to use for loading an mgf file or a directory of mgf files into a database
 * User: Steve
 * Date: 7/5/13
 */
public class DatabaseLoadMain {


    private long readTimeMillisec;
    private long storeTimeMillisec;
    private final IMutableSpectrumDataStore database;


    public DatabaseLoadMain(String dbName) {
        DataSource ds = DataSourceDefaults.INSTANCE.getDefaultDataSource();
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

    public IMutableSpectrumDataStore getDatabase() {
        return database;
    }

    public static final int MAX_SPECTRA_TO_PROCESS_AT_ONCE = 10000;

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

        IMutableSpectrumDataStore db = getDatabase();

        MGFSpectrumIterable spcit = new MGFSpectrumIterable(inputFile);
        Iterator<ISpectrum> itr = spcit.iterator();

        while (itr.hasNext()) {
            start = System.currentTimeMillis();
            while (itr.hasNext()) {
                holder.add(itr.next());
                if (holder.size() >= MAX_SPECTRA_TO_PROCESS_AT_ONCE)
                    break;
            }
            end = System.currentTimeMillis();
            long del = end - start;
            addReadTimeMillisec(del);
            readTime += del;
            // store any spectra
            if (!holder.isEmpty()) {
                seconds = (del / 1000);
                min = seconds / 60;
                System.out.println("read " + inputFile + " with " + holder.size() + " spectra in " + String.format("%10.3f", seconds).trim());
                start = System.currentTimeMillis();
                db.storeSpectra(holder);
                processedSpectra += holder.size();
                end = System.currentTimeMillis();
                del = end - start;
                storeTime += del;
                addStoreTimeMillisec(del);
                seconds = (del / 1000);
                min = seconds / 60;
                System.out.println("inserted " + inputFile + " with " + holder.size() + " spectra in " + String.format("%10.3f", seconds).trim());
                holder.clear();
            }
        }


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


    protected static void usage() {
        System.out.println("Usage database_name <mgf file or directory> ...");
    }


    public static void main(String[] args) {
        if (args.length < 2) {
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
        double readMin = mainClusterer.getReadTimeMillisec() / (60 * 1000);
        System.out.println("read in " + String.format("%10.2f", readMin) + " min");
        System.out.println("Processed in " + String.format("%10.2f", min - readMin) + " min");
        System.out.println("Total " + String.format("%10.2f", min) + " min");
    }


}
