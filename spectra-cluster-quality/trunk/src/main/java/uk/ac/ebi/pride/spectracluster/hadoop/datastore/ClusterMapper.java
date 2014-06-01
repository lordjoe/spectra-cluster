package uk.ac.ebi.pride.spectracluster.hadoop.datastore;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * uk.ac.ebi.pride.spectracluster.datastore.ClusterMapper
 * User: Steve
 * Date: 7/15/13
 */
public class ClusterMapper implements ParameterizedRowMapper<ICluster> {
    ClusterMapper() {
    }

    public static final int MAX_ID_LENGTH = 16;

    public static final String TABLE_CREATE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS `mono_mz_to_fragments`  (\n" +
                    "  `id` varchar(" + MAX_ID_LENGTH + ")   NOT NULL UNIQUE,\n" +
                    "  `precursor_charge` int(11) NOT NULL,\n" +
                    "  `precursor_mz` double NOT  NULL,\n" +
                    ")";


    @Override
    public ICluster mapRow(final ResultSet rs, final int i) throws SQLException {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        String id = rs.getString("id");
//        int precursor_charge = rs.getInt("precursor_charge");
//        float precursor_mz = (float)rs.getDouble("precursor_mz");
//        String peaksStr = rs.getString("peaks");
//        String annotation = SpringJDBCUtilities.trimIfNotNUll(rs.getString("annotation"));
//        String peptide = SpringJDBCUtilities.trimIfNotNUll(rs.getString("peptide"));
//        List<IPeak> peaks = SpectrumUtilities.dataStringToPeaks(peaksStr);
//
//        ISpectrum ret = new PeptideSpectrumMatch(id, peptide, precursor_charge, precursor_mz, peaks);
//        return ret;
    }
}
