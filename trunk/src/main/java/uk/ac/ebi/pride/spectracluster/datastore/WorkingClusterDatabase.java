package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.simple.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.WorkingClusterDatabase
 * an object representing Spring JDBC interface to a working spectrum database
 * The database may be cleared after clustering runs
 * User: Steve
 * Date: 7/15/13
 */
public class WorkingClusterDatabase {

    public static final int MAX_PEAKS_PER_SPECTRUM = 250;
    public static final int MAX_PEAKS_STRING_LENGTH = MAX_PEAKS_PER_SPECTRUM * 8;

    public static final String[] TABLES =
            {
                    "spectrums",
                    "cluster_to_spectrums",
                    "clusters",
                    "spectrum_properties",
                     };

    public static final String[] CREATE_STATEMENTS =
            {
                    SpectrumMapper.TABLE_CREATE_STATEMENT,
                      "CREATE TABLE  IF NOT EXISTS `average_mz_to_fragments` (\n" +
                            "  `mz` int(11) NOT NULL,\n" +
                            "  `sequence` varchar(" + MAX_PEAKS_STRING_LENGTH + ") NOT NULL,\n" +
                            "  `real_mass` double default NULL,\n" +
                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
                            ")",
                    ClusterMapper.TABLE_CREATE_STATEMENT,
                         "CREATE TABLE IF NOT EXISTS `fragment_protein` (\n" +
                            "  `sequence` varchar(255) NOT NULL,\n" +
                            "  `protein_id` int(11) NOT NULL,\n" +
                            "  PRIMARY KEY (`sequence`,`protein_id`)\n" +
                            ")",
                    "CREATE TABLE  IF NOT EXISTS `load_fragments` (\n" +
                            "  `id` int(11) NOT NULL,\n" +
                            "  `sequence` varchar(" + MAX_PEAKS_STRING_LENGTH + ") NOT NULL,\n" +
                            "  `protein_id` int(11) NOT NULL,\n" +
                            "  `start_location` int(11) NOT NULL,\n" +
                            "  `average_mass` double DEFAULT NULL,\n" +
                            "  `iaverage_mass` int(11) DEFAULT NULL,\n" +
                            "  `mono_mass` double DEFAULT NULL,\n" +
                            "  `imono_mass` int(11) DEFAULT NULL,\n" +
                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`)\n" +
                            ")",
              };


    private final SimpleJdbcTemplate m_Template;
    private final Map<String, String> m_NameToCreateStatement = new HashMap<String, String>();

    public WorkingClusterDatabase(final SimpleJdbcTemplate pTemplate) {
        m_Template = pTemplate;
        for (int i = 0; i < TABLES.length; i++) {
            m_NameToCreateStatement.put(TABLES[i], CREATE_STATEMENTS[i]);

        }
    }

    public SimpleJdbcTemplate getTemplate() {
        return m_Template;
    }

    /**
     * crate a table if it does not exist
     *
     * @param tableName name of a known table
     */
    public void guaranteeTable(String tableName) {
        String creator = m_NameToCreateStatement.get(tableName);
        if (creator == null)
            throw new IllegalArgumentException("cannot create table " + tableName);
        SimpleJdbcTemplate template = getTemplate();
        SpringJDBCUtilities.guaranteeTable(template, tableName, creator);
    }


    /**
     * drop all data
     */
    public void createDatabase() {

        SimpleJdbcTemplate template = getTemplate();
        //    template.update("DROP SCHEMA IF EXISTS " + SCHEMA_NAME);
        //     template.update("CREATE SCHEMA  " + SCHEMA_NAME);

        for (int i = 0; i < CREATE_STATEMENTS.length; i++) {
            String stmt = CREATE_STATEMENTS[i];
            template.update(stmt);

        }
    }


    /**
     * drop all data
     */
    public void clearDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("delete from  " + table);

        }
    }

    /**
     * drop all tables
     */
    public void expungeDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("drop table " + table);

        }
    }

}
