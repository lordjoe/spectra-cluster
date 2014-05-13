package uk.ac.ebi.pride.spectracluster.datastore;


import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.ITemplateHolder
 * implemented by a class holding an JdbcTemplate -
 * i.e. one that knows how to talk to a database
 * User: Steve
 * Date: Apr 7, 2011
 */
public interface ITemplateHolder {
    public static final ITemplateHolder[] EMPTY_ARRAY = {};

    /**
     * return the associated template
     *
     * @return
     */
    public SimpleJdbcTemplate getTemplate();

}
