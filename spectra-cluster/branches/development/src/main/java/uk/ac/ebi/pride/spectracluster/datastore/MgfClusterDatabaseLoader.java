package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.util.SpectrumCreateListener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterDatabaseLoader {

    public static final String MGF_FILE_EXTENSION = "mgf";

    private final SQLDataStore sqlDataStore;

    public MgfClusterDatabaseLoader(SQLDataStore sqlDataStore) {
        this.sqlDataStore = sqlDataStore;
    }

    public void load(File mgfResource) throws IOException, IllegalAccessException {

        // find all the mgf files
        Collection<File> mgfFiles = findMGFFiles(mgfResource);

        // create spectrum create listener to load into database
        SpectrumCreateListener spectrumCreateListener = new LoadSpectrumListener();

        for (File mgfFile : mgfFiles) {
            LineNumberReader mgfLineNumberReader = new LineNumberReader(new FileReader(mgfFile));
            ParserUtilities.readAndProcessSpectra(mgfLineNumberReader, spectrumCreateListener);
        }

    }

    /**
     * Find all the mgf files
     *
     * If it is a directory, it will only look for mgf files at the root level
     */
    private Collection<File> findMGFFiles(File mgfResource) throws IllegalAccessException {
        Collection<File> files = new LinkedHashSet<File>();

        if (mgfResource.isFile()) {
            if (mgfResource.getName().endsWith(MGF_FILE_EXTENSION)) {
                files.add(mgfResource);
            }
        } else if (mgfResource.isDirectory()) {
            for (File file : mgfResource.listFiles()) {
                if (file.getName().endsWith(MGF_FILE_EXTENSION)) {
                    files.add(file);
                }
            }
        } else {
            throw new IllegalAccessException("Input mgf resource must be either a mgf file or a folder contains multiple mgf files");
        }

        return files;
    }

    /**
     * Listener to load spectrum into sql data store once it is created
     */
    private class LoadSpectrumListener implements SpectrumCreateListener {

        @Override
        public void onSpectrumCreate(ISpectrum spectrum) {
            sqlDataStore.addSpectrum(spectrum);
        }

        @Override
        public void onSpectrumStarted() {
        }

        @Override
        public void onSpectrumCreateFinished() {
        }
    }

//    public static void main(String[] args) throws IOException, IllegalAccessException {
//        if (args.length != 0) {
//            System.out.println("Usage: [MGF file or directory]");
//            System.exit(1);
//        }
//
//        DataSource source = HBaseUtilities.getHBaseDataSource();
//        Defaults.INSTANCE.setDefaultDataSource(source);
//        Defaults.INSTANCE.setDatabaseFactory(PhoenixWorkingClusterDatabase.FACTORY);
//
//        SQLDataStore db = new SQLDataStore("test", source);
//
//        MgfClusterDatabaseLoader mgfClusterDatabaseLoader = new MgfClusterDatabaseLoader(db);
//        mgfClusterDatabaseLoader.load(new File(args[0]));
//    }

}
