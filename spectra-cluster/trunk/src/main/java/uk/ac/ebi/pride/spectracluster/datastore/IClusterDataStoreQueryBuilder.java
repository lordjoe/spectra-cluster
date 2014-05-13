package uk.ac.ebi.pride.spectracluster.datastore;

/**
 *
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterDataStoreQueryBuilder {

    enum QueryType {
        DELETE_ALL ("DELETE FROM <tablename>"),
        DELETE_CLUSTER_BY_ID ("DELETE FROM <tablename> WHERE id=?"),
        CREATE_TABLE (""),
        STORE_CLUSTER ("INSERT INTO <tablename> (id,precursor_charge,precursor_mz,peaks,spectrum_ids) VALUES (?, ?, ?, ?, ?)"),
        SELECT_CLUSTER_BY_ID ("SELECT * FROM <tablename> WHERE id=?"),
        SELECT_CLUSTER_BY_MZ_RANGE ("SELECT * FROM <tablename> WHERE precursor_mz >= ? AND precursor_mz <= ?"),
        SELECT_CLUSTER_BY_MZ_RANGE_AND_CHARGE (""),
        SELECT_CLUSTER_BY_PEPTIDE ("");

        private String sql;

        private QueryType(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    public String getQuery(QueryType type, String tableName, Object params);

}
