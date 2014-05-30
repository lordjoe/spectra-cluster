package uk.ac.ebi.pride.spectracluster.export;

import org.springframework.dao.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;
import uk.ac.ebi.pride.spectracluster.hadoop.datastore.*;

import javax.annotation.*;
import javax.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.WorkingClusterDatabase
 * an object representing Spring JDBC interface to a working spectrum database
 * The database may be cleared after clustering runs
 * User: Steve
 * Date: 7/15/13
 */
public class WorkingClusteringDatabase implements IWorkingClusterDatabase {

        //    mysql -upride_q_ro -pPride_Q_RO -hmysql-pride-projects -P4285

    /**
     * @return a data source ot talk to cluster database
     */
    public static @Nonnull DataSource buildClusterDataSource(String database) {
        Map<String, String> holder = new HashMap<String, String>();
        final String host = "mysql-pride-projects";
        holder.put(SpringJDBCUtilities.DATA_HOST_PARAMETER, host);
        holder.put(SpringJDBCUtilities.DATA_PORT_PARAMETER, "4285");
        holder.put(SpringJDBCUtilities.DATA_USER_PARAMETER, "pride_q_ro");
        holder.put(SpringJDBCUtilities.DATA_DATABASE_PARAMETER, database);
         String pw = "Pride_Q_RO";
        holder.put(SpringJDBCUtilities.DATA_PASSWORD_PARAMETER, pw);
        holder.put(SpringJDBCUtilities.DATA_DRIVER_CLASS_PARAMETER, "com.mysql.jdbc.Driver");
        DataSource ds = SpringJDBCUtilities.buildDataSource(holder);
        return ds;
    }

    /**
      * @return a data source ot talk to cluster database
      */
     public static @Nonnull DataSource buildClusterAdminDataSource() {
         Map<String, String> holder = new HashMap<String, String>();
         final String host = "mysql-pride-projects";
         holder.put(SpringJDBCUtilities.DATA_HOST_PARAMETER, host);
         holder.put(SpringJDBCUtilities.DATA_PORT_PARAMETER, "4285");
         holder.put(SpringJDBCUtilities.DATA_USER_PARAMETER, "admin");
         String pw = "fHfW1mT7";
         holder.put(SpringJDBCUtilities.DATA_PASSWORD_PARAMETER, pw);
         holder.put(SpringJDBCUtilities.DATA_DRIVER_CLASS_PARAMETER, "com.mysql.jdbc.Driver");
         DataSource ds = SpringJDBCUtilities.buildDataSource(holder);
         return ds;
     }

    public static WorkingDatabaseFactory FACTORY = new WorkingDatabaseFactory() {
        /**
         * return a working database - needed so we can subclass to handle Phoenix
         *
         * @param databaseName - name of the database - tables will have <databasename> . prepended
         * @param ds           !null data source - this provides the connection
         * @return !null   IWorkingClusterDatabase
         */
        @Override
        public IWorkingClusterDatabase buildWorkingDatabase(String databaseName, DataSource ds) {
            WorkingClusteringDatabase ret = new WorkingClusteringDatabase(databaseName, ds);
            return ret;

        }
    };

    public static final int MAX_ID_LENGTH = 256;

      /**
        * statement to create a spectrums table in the database <database>  every
        * group of clustered spectra is in its own database
        */
       public static final String PROCESSING_SPECTRUM_CREATE_STATEMENT =
               "CREATE TABLE IF NOT EXISTS <database>.project_directories  ( \n" +
                       "  `id` VARCHAR(" + MAX_ID_LENGTH + ")   NOT NULL PRIMARY KEY  , \n" +
                           "  `state` VARCHAR(12) NOT   NULL\n" +
                              ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n"

                ;      // index mz



    public static final String CLUSTER_CREATE_STATEMENT = "CREATE TABLE `<database>.cluster` (\n" +
            "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `clustering_method_id` bigint(20) unsigned NOT NULL,\n" +
            "  `uid` varchar(25) NOT NULL,\n" +
            "  `av_precursor_mz` decimal(7,3) unsigned NOT NULL,\n" +
            "  `consensus_spec_mz` text NOT NULL,\n" +
            "  `consensus_spec_intens` text NOT NULL,\n" +
            "  `threshold` decimal(4,3) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `unique_uid` (`uid`),\n" +
            "  KEY `fk_cluster_clustering_method1` (`clustering_method_id`),\n" +
            "  KEY `index_precursor_mz` (`av_precursor_mz`),\n" +
            "  CONSTRAINT `fk_cluster_clustering_method1` FOREIGN KEY (`clustering_method_id`) REFERENCES `clustering_method` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";

    public static final String CLUSTER_HAS_PEPTIDE_CREATE_STATEMENT = "CREATE TABLE `<database>.cluster_has_peptide` (\n" +
            "  `cluster_id` bigint(19) unsigned NOT NULL,\n" +
            "  `peptide_id` bigint(19) unsigned NOT NULL,\n" +
            "  `rank` smallint(5) unsigned NOT NULL,\n" +
            "  `ratio` decimal(4,3) unsigned NOT NULL,\n" +
            "  PRIMARY KEY (`cluster_id`,`peptide_id`),\n" +
            "  KEY `fk_cluster_has_peptide_peptide1` (`peptide_id`),\n" +
            "  KEY `fk_cluster_has_peptide_cluster1` (`cluster_id`),\n" +
            "  CONSTRAINT `fk_cluster_has_peptide_cluster1` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,\n" +
            "  CONSTRAINT `fk_cluster_has_peptide_peptide1` FOREIGN KEY (`peptide_id`) REFERENCES `peptide` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";

    public static final String CLUSTER_HAS_SPECTRUM_CREATE_STATEMENT = "CREATE TABLE `<database>.cluster_has_spectrum` (\n" +
            "  `cluster_id` bigint(20) unsigned NOT NULL,\n" +
            "  `spectrum_id` bigint(20) unsigned NOT NULL,\n" +
            "  PRIMARY KEY (`cluster_id`,`spectrum_id`),\n" +
            "  KEY `fk_cluster_has_spectrum_spectrum1` (`spectrum_id`),\n" +
            "  KEY `fk_cluster_has_spectrum_cluster1` (`cluster_id`),\n" +
            "  CONSTRAINT `fk_cluster_has_spectrum_cluster1` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,\n" +
            "  CONSTRAINT `fk_cluster_has_spectrum_spectrum1` FOREIGN KEY (`spectrum_id`) REFERENCES `spectrum` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";

    public static final String CLUSTER_METHOD_CREATE_STATEMENT = "CREATE TABLE `<database>.clustering_method` (\n" +
            "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `name` varchar(45) NOT NULL,\n" +
            "  `iterations` smallint(5) unsigned NOT NULL,\n" +
            "  `threshold` double(4,3) unsigned NOT NULL,\n" +
            "  `region` varchar(255) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `index_region` (`region`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";
    public static final String EXPERIMENT_CREATE_STATEMENT = "CREATE TABLE `<database>.experiment` (\n" +
            "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `pride_experiment_accession` varchar(10) NOT NULL,\n" +
            "  `pride_mzdata_id` bigint(20) unsigned NOT NULL,\n" +
            "  `title` varchar(255) NOT NULL,\n" +
            "  `project` varchar(255) NOT NULL,\n" +
            "  `species` varchar(255) DEFAULT NULL,\n" +
            "  `taxid` smallint(5) unsigned DEFAULT NULL,\n" +
            "  `search_engine` varchar(255) DEFAULT NULL,\n" +
            "  `instrument` varchar(255) DEFAULT NULL,\n" +
            "  `source` varchar(255) DEFAULT NULL,\n" +
            "  `instrument_type` varchar(50) NOT NULL DEFAULT 'Unknown',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `unique_accession` (`pride_experiment_accession`),\n" +
            "  UNIQUE KEY `unique_mzdata_id` (`pride_mzdata_id`),\n" +
            "  KEY `index_instrument_type` (`instrument_type`),\n" +
            "  KEY `index_taxid` (`taxid`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=39739556 DEFAULT CHARSET=latin1 COMMENT='Contains the meta-data of a PRIDE experiment';\n";

    public static final String PEPTIDE_CREATE_STATEMENT = "CREATE TABLE `<database>.peptide` (\n" +
            "  `id` bigint(20) unsigned NOT NULL,\n" +
            "  `experiment_id` bigint(20) unsigned NOT NULL,\n" +
            "  `spectrum_id` bigint(20) unsigned NOT NULL,\n" +
            "  `pride_identification_id` bigint(20) unsigned NOT NULL,\n" +
            "  `sequence` varchar(150) NOT NULL,\n" +
            "  `modifications` varchar(255) DEFAULT NULL,\n" +
            "  `charge` smallint(5) unsigned NOT NULL,\n" +
            "  `theoretical_mass` decimal(10,4) unsigned DEFAULT NULL,\n" +
            "  `delta` decimal(10,4) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `fk_peptide_spectrum` (`spectrum_id`),\n" +
            "  KEY `fk_peptide_experiment1` (`experiment_id`),\n" +
            "  KEY `index_sequence` (`sequence`),\n" +
            "  CONSTRAINT `fk_peptide_experiment1` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,\n" +
            "  CONSTRAINT `fk_peptide_spectrum` FOREIGN KEY (`spectrum_id`) REFERENCES `spectrum` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Contains the extracted metadata of a PRIDE peptide';\n";
    public static final String SPECTRUM_CREATE_STATEMENT = "CREATE TABLE `<database>.spectrum` (\n" +
            "  `id` bigint(20) unsigned NOT NULL,\n" +
            "  `spec_ref` mediumint(8) unsigned NOT NULL DEFAULT '0',\n" +
            "  `precursor_mz` decimal(7,3) unsigned NOT NULL,\n" +
            "  `ms_level` smallint(5) unsigned NOT NULL,\n" +
            "  `exported` tinyint(1) NOT NULL DEFAULT '0',\n" +
            "  `experiment_id` bigint(19) unsigned NOT NULL,\n" +
            "  `identified` tinyint(1) NOT NULL DEFAULT '0',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `index_mz` (`precursor_mz`),\n" +
            "  KEY `fk_spectrum_experiment1` (`experiment_id`),\n" +
            "  KEY `index_identified` (`identified`),\n" +
            "  CONSTRAINT `fk_spectrum_experiment1` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";

     public static final String[] TABLES =
            {
                    "cluster",
                    "cluster_has_peptide",
                    "cluster_has_spectrum",
                    "clustering_method",
                    "experiment",
                    "peptide",
                    "spectrum",
                    "project_directories",
                      //             "clusters",
            };

    public static final String[] CREATE_STATEMENTS =
            {
                    CLUSTER_CREATE_STATEMENT,
                    CLUSTER_HAS_PEPTIDE_CREATE_STATEMENT,
                    CLUSTER_HAS_SPECTRUM_CREATE_STATEMENT,
                    CLUSTER_METHOD_CREATE_STATEMENT,
                    EXPERIMENT_CREATE_STATEMENT,
                    PEPTIDE_CREATE_STATEMENT,
                    SPECTRUM_CREATE_STATEMENT,
                    PROCESSING_SPECTRUM_CREATE_STATEMENT,
                     //           ClusterMapper.TABLE_CREATE_STATEMENT,
            };


 //   public static final String SELECT_ALL_CLUSTERS_STATEMENT = "SELECT * FROM clusters  ";
    @SuppressWarnings("UnusedDeclaration")
 //   public static final String SELECT_CLUSTER_STATEMENT = SELECT_ALL_CLUSTERS_STATEMENT + " WHERE id = ?";


    private final String databaseName;
    private final JdbcTemplate m_OldTemplate;
    private final DataSource dataSource;
    private final SimpleJdbcTemplate m_Template;
    private final Map<String, String> m_NameToCreateStatement = new HashMap<String, String>();

    /**
     * PRIVATE - use the factory
     *
     * @param databaseName - name of the database - tables will have <databasename> . prepended
     * @param ds           !null data source - this provides the connection
     * @return !null   IWorkingClusterDatabase
     */
    protected WorkingClusteringDatabase(String databaseName, final DataSource ds) {
        dataSource = ds;
        m_Template = new SimpleJdbcTemplate(ds);
        m_OldTemplate = new JdbcTemplate(ds);
        this.databaseName = databaseName;

        for (int i = 0; i < TABLES.length; i++) {
            m_NameToCreateStatement.put(TABLES[i], CREATE_STATEMENTS[i]);

        }
        guaranteeDatabase();

    }


    /**
     * make the query for the current database
     *
     * @param query !null original query with "<database>" as a place holder
     * @return query with the proper database
     */
    @Override
    public String queryForDatabase(String query, String dbName) {
        return query.replace("<database>", dbName);

    }

    /**
     * true of batch operations are allowed
     *
     * @return
     */
    @Override
    public boolean isBatchSupported() {
        return true;
    }


    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public JdbcTemplate getOldTemplate() {
        return m_OldTemplate;
    }

    @Override
    public SimpleJdbcTemplate getTemplate() {
        return m_Template;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * crate a table if it does not exist
     *
     * @param tableName name of a known table
     */
    @Override
    public void guaranteeTable(String tableName) {
        SimpleJdbcTemplate template = getTemplate();
        try {
            String tableFullName = getDatabaseName() + "." + tableName;
            List<SpringJDBCUtilities.FieldDescription> fields = template.query("describe " + tableFullName, SpringJDBCUtilities.FIELD_MAPPER);

            if (fields.size() > 0)
                return;
        } catch (DataAccessException ignored) {

        }
        guaranteeDatabaseExists();
        doCreateTable(tableName);
        //   SpringJDBCUtilities.guaranteeTable(template, tableName, creator);
    }

    protected void doCreateTable(String tableName) {
        String creator = m_NameToCreateStatement.get(tableName);
        if (creator == null)
            throw new IllegalArgumentException("cannot create table " + tableName);
        creator = queryForDatabase(creator, getDatabaseName());
        final int update = getOldTemplate().update(creator);
    }


    /**
     * make sure the tables exist
     */
    protected void guaranteeDatabaseExists() {
        SimpleJdbcTemplate template = getTemplate();

        final String dmyDB = getDatabaseName();
        String[] dbMames = SpringJDBCUtilities.queryForStrings(template, "SHOW DATABASES");
        for (int i = 0; i < dbMames.length; i++) {
            if (dmyDB.equalsIgnoreCase(dbMames[i]))
                return;  // already exists
        }
        final int update = template.update("CREATE DATABASE IF NOT EXISTS " + dmyDB);
        if (update == 0)
            //noinspection UnnecessaryReturnStatement
            return; // no change
        else
            //noinspection UnnecessaryReturnStatement
            return;
    }

    /**
     * make sure the tables exist
     */
    protected void guaranteeDatabase() {


        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            guaranteeTable(table);

        }
        // guaranteeIndices();
    }


    public static final String FIND_INDEX_STATEMENT = "SELECT COUNT(1) IndexIsThere FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE table_schema=? AND table_name=? AND index_name=?";

    public static final String BUILD_INDEX_STATEMENT = "CREATE INDEX <index_name> on <full_table_name>(<column_to_index>);";

//
//    protected void guaranteeIndices() {
//        final String db1DatabaseName = getDatabaseName();
//        final SimpleJdbcTemplate template = getTemplate();
//        String tableName = "spectrums";
//        String columnName = "precursor_charge";
//        indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
//        columnName = "precursor_mz";
//        indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
//        //     columnName = "peptide";
//        //     indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
//    }
//
//    protected void indexColumnIfNeeded(final SimpleJdbcTemplate template, final String pDb1DatabaseName, final String pTableName, final String pColumnName) {
//        String indexName = pTableName + "_" + pDb1DatabaseName + "_" + pColumnName;
//        final int indexCount = template.queryForInt(FIND_INDEX_STATEMENT, pDb1DatabaseName, pTableName, indexName);
//        if (indexCount == 0) {
//            String fullTable = pDb1DatabaseName + "." + pTableName;
//            String createIndexUpdate = BUILD_INDEX_STATEMENT.replace("<full_table_name>", fullTable).replace("<column_to_index>", pColumnName).replace("<index_name>", indexName);
//            int done = template.update(createIndexUpdate);
//        }
//    }
//

    /**
     * drop all data
     */
    @Override
    public void clearDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("delete from  " + getDatabaseName() + "." + table);

        }
    }

    /**
     * drop all tables
     */
    @Override
    public void expungeDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("drop table " + getDatabaseName() + "." + table);

        }
    }

    /**
     * subclasses support different syntax on queries - this code attampts to make
     * 'standard' queries compatible
     *
     * @param originalQuery
     * @return !null patched query
     */
    public String patchQueryString(String originalQuery) {
        // standard SQL works here
        return originalQuery;
    }

}
