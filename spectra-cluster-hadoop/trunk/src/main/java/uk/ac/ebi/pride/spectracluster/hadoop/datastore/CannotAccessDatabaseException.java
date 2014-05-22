package uk.ac.ebi.pride.spectracluster.hadoop.datastore;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.CannotAccessDatabaseException
 * User: Steve
 * Date: 7/15/13
 */
public class CannotAccessDatabaseException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public CannotAccessDatabaseException(final String message) {
        super(message);
    }
}
