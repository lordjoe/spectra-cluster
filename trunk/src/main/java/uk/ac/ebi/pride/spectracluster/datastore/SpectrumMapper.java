package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.simple.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SpectrumMapper
 * User: Steve
 * Date: 7/15/13
 */
public class SpectrumMapper implements ParameterizedRowMapper<ISpectrum> {
    SpectrumMapper() {
    }

    public static final int MAX_ANNOTATION_LENGTH = 256;
    public static final int MAX_PEPTIDE_LENGTH = 100;
    public static final int MAX_ID_LENGTH = 16;

    public static final String TABLE_CREATE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS `spectrums`  (\n" +
                    "  `id` varchar(" + MAX_ID_LENGTH + ")   NOT NULL UNIQUE,\n" +
                    "  `precursor_charge` int(11) NOT NULL,\n" +
                    "  `precursor_mz` double NOT  NULL,\n" +
                    "  `peptide` varchar(" + MAX_PEPTIDE_LENGTH + ")   NULL,\n" +
                    "  `annotation` varchar(" + MAX_ANNOTATION_LENGTH + ")   NULL,\n" +
                    "  `peaks` varchar(" + WorkingClusterDatabase.MAX_PEAKS_STRING_LENGTH + ") NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    ")  ";


    @Override
    public ISpectrum mapRow(final ResultSet rs, final int i) throws SQLException {
        String id = rs.getString("id");
        int precursor_charge = rs.getInt("precursor_charge");
        float precursor_mz = (float)rs.getDouble("precursor_mz");
        String peaksStr = rs.getString("peaks");
        String annotation = SpringJDBCUtilities.trimIfNotNUll(rs.getString("annotation"));
        String peptide = SpringJDBCUtilities.trimIfNotNUll(rs.getString("peptide"));
        List<IPeak> peaks = SpectrumUtilities.dataStringToPeaks(peaksStr);

        ISpectrum ret = new PeptideSpectrumMatch(id, peptide, precursor_charge, precursor_mz, peaks);
        return ret;
    }

}
