package uk.ac.ebi.pride.spectracluster.annotation;

/**
 * Runtime exception for SpectralAnnotationRetriever
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SpectralAnnotationRetrieverException extends RuntimeException {
    public SpectralAnnotationRetrieverException() {
    }

    public SpectralAnnotationRetrieverException(String message) {
        super(message);
    }

    public SpectralAnnotationRetrieverException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpectralAnnotationRetrieverException(Throwable cause) {
        super(cause);
    }

    public SpectralAnnotationRetrieverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
