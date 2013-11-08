package uk.ac.ebi.pride.spectracluster.util;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class JohannesClusterDbExporter {

    private static final String CLUSTERING_FILE_EXTENSION = ".clustering";

    // clustering file headers
    private static final String ALGORITHM_NAME = "name=";
    private static final String SIMILARITY_METHOD = "similarity_method=";
    private static final String THERSHOLD = "threshold=";
    private static final String FDR = "fdr=";
    private static final String DESCRIPTION = "description=";

    // cluster headers
    private static final String CLUSTER_HEADER = "=Cluster=";
    private static final String AVERAGE_PRECURSOR_MZ = "av_precursor_mz=";
    private static final String AVERAGE_PRECURSOR_INTENSITY = "av_precursor_intens=";
    private static final String PEPTIDE_SEQUENCE = "sequence=";
    private static final String CONSENSUS_MZ = "consensus_mz=";
    private static final String CONSENSUS_INTENSITY = "consensus_intens=";
    private static final String SPECTRUM = "SPEC";

    // separators
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String COMMA = ",";
    private static final String TAB = "\t";
    private static final String SEPARATOR = "_";


    private final NamedParameterJdbcTemplate jdbcTemplate;



    public JohannesClusterDbExporter(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void exportClustersToFile(String clusteringMethod, double threshold, File outputDirectory) throws IOException {

        List<ClusteringMethod> clusteringMethods = getClusteringMethods(clusteringMethod, threshold);
        for (ClusteringMethod method : clusteringMethods) {
            exportClusteringFile(method, outputDirectory);
        }
    }

    private List<ClusteringMethod> getClusteringMethods(String clusteringMethod, double threshold) {
        String sql = "SELECT * FROM clustering_method WHERE name=:clusteringMethod AND threshold=:threshold";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clusteringMethod", clusteringMethod);
        params.addValue("threshold", threshold);

        return jdbcTemplate.query(sql, params, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String similarityMethod = rs.getString("similarity_method");
                double thd = rs.getDouble("threshold");
                double fdr = rs.getDouble("fdr");
                String description = rs.getString("description");

                return new ClusteringMethod(id, name, similarityMethod, thd, fdr, description);
            }
        });
    }

    private void exportClusteringFile(ClusteringMethod clusteringMethod, File outputDirectory) throws IOException {
        // generate file name
        String fileName = clusteringMethod.getName() + SEPARATOR + clusteringMethod.getThreshold() + SEPARATOR + clusteringMethod.getDescription() + CLUSTERING_FILE_EXTENSION;

        System.out.println("Writing file: " + fileName);

        PrintWriter writer = new PrintWriter(new FileWriter(new File(outputDirectory.getCanonicalPath() + FILE_SEPARATOR + fileName)));

        try {
            // write header to file
            exportHeaderToFile(clusteringMethod, writer);

            // write clusters to file
            exportClustersToFile(clusteringMethod, writer);

        } finally {
            writer.close();
        }
    }

    private void exportHeaderToFile(ClusteringMethod clusteringMethod, Appendable appendable) throws IOException {
        appendable.append(ALGORITHM_NAME + clusteringMethod.getName())
                .append(LINE_SEPARATOR)
                .append(SIMILARITY_METHOD + clusteringMethod.getSimilarityMethod())
                .append(LINE_SEPARATOR)
                .append(THERSHOLD + clusteringMethod.getThreshold())
                .append(LINE_SEPARATOR)
                .append(FDR + clusteringMethod.getFdr())
                .append(LINE_SEPARATOR)
                .append(DESCRIPTION + clusteringMethod.getDescription())
                .append(LINE_SEPARATOR).append(LINE_SEPARATOR);
    }

    private void exportClustersToFile(ClusteringMethod clusteringMethod, Appendable appendable) {
        String sql = "SELECT c.*, GROUP_CONCAT(CONCAT(s.spectrum_id,'_',is_target)) AS spectra FROM cluster c JOIN cluster_has_spectrum s ON(c.id=s.cluster_id) WHERE c.clustering_method_id=:clusteringMethodId GROUP BY c.id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clusteringMethodId", clusteringMethod.getId());

        jdbcTemplate.query(sql, params, new ClusterRowCallbackHandler(appendable));
    }

    private static class ClusteringMethod {
        private long id;
        private String name;
        private String similarityMethod;
        private double threshold;
        private double fdr;
        private String description;

        private ClusteringMethod(long id, String name,
                                 String similarityMethod, double threshold,
                                 double fdr, String description) {
            this.id = id;
            this.name = name;
            this.similarityMethod = similarityMethod;
            this.threshold = threshold;
            this.fdr = fdr;
            this.description = description;
        }

        private long getId() {
            return id;
        }

        private String getName() {
            return name;
        }

        private String getSimilarityMethod() {
            return similarityMethod;
        }

        private double getThreshold() {
            return threshold;
        }

        private double getFdr() {
            return fdr;
        }

        private String getDescription() {
            return description;
        }
    }

    private static class ClusterRowCallbackHandler implements RowCallbackHandler {

        private final Appendable appendable;

        private ClusterRowCallbackHandler(Appendable appendable) {
            this.appendable = appendable;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {

            try {
                appendable.append(CLUSTER_HEADER)
                        .append(LINE_SEPARATOR).append(AVERAGE_PRECURSOR_MZ)
                        .append(String.valueOf(rs.getDouble("av_precursor_mz")))
                        .append(LINE_SEPARATOR).append(PEPTIDE_SEQUENCE).append(rs.getString("sequence"))
                        .append(LINE_SEPARATOR).append(CONSENSUS_MZ).append(rs.getString("consensus_mz"))
                        .append(LINE_SEPARATOR).append(CONSENSUS_INTENSITY).append(rs.getString("consensus_intensity"))
                        .append(LINE_SEPARATOR);

                String spectra = rs.getString("spectra");
                String[] parts = spectra.split(COMMA);
                for (String part : parts) {
                    String[] tuple = part.split(SEPARATOR);
                    boolean ident = (Integer.parseInt(tuple[1]) == 1);
                    appendable.append(SPECTRUM + TAB).append(tuple[0]).append(TAB).append(String.valueOf(ident))
                            .append(LINE_SEPARATOR);
                }

                appendable.append(LINE_SEPARATOR);

            } catch (IOException e) {
                throw new IllegalStateException("Failed to write to file", e);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 6) {
            System.err.printf("Usage: [database] [database user name] [database password] [algorithm name] [threshold] [output directory]");
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://mysql-pride-projects.ebi.ac.uk:4285/" + args[0]);
        dataSource.setUsername(args[1]);
        dataSource.setPassword(args[2]);

        JohannesClusterDbExporter clusterDbExporter = new JohannesClusterDbExporter(dataSource);
        clusterDbExporter.exportClustersToFile(args[3], Double.parseDouble(args[4]), new File(args[5]));
    }
}
