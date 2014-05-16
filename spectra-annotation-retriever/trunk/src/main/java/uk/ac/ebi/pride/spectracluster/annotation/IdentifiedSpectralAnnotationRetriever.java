package uk.ac.ebi.pride.spectracluster.annotation;

import java.util.*;

/**
 * Basic implementation for identify identified spectra
 *
 * @author Rui Wang
 * @version $Id$
 */
public class IdentifiedSpectralAnnotationRetriever implements ISpectralAnnotationRetriever {

    public final static String IDENTIFIED_ANNOTATION = "identified";

    private final Set<String> identifiedSpectraIds;

    public IdentifiedSpectralAnnotationRetriever(String[] identifiedSpectraIds) {
        this(Arrays.asList(identifiedSpectraIds));
    }

    public IdentifiedSpectralAnnotationRetriever(Collection<String> identifiedSpectraIds) {
        this.identifiedSpectraIds = new HashSet<String>(identifiedSpectraIds);
    }

    @Override
    public String getAnnotation(String annotationName, String spectrumId) throws SpectralAnnotationRetrieverException {
        if (!annotationName.equalsIgnoreCase(IDENTIFIED_ANNOTATION)) {
            throw new SpectralAnnotationRetrieverException("Unexpected annotation name: " + annotationName + ", only identified is supported");
        }

        return identifiedSpectraIds.contains(spectrumId) + "";
    }

    @Override
    public Map<String, String> getAnnotations(String spectrumId) throws SpectralAnnotationRetrieverException {
        HashMap<String, String> annotations = new HashMap<String, String>();

        annotations.put(IDENTIFIED_ANNOTATION, getAnnotation(IDENTIFIED_ANNOTATION, spectrumId));

        return annotations;
    }

    @Override
    public Map<String, String> getAnnotations(String spectrumId, Set<String> annotationNames) throws SpectralAnnotationRetrieverException {
        HashMap<String, String> annotations = new HashMap<String, String>();

        for (String annotationName : annotationNames) {
            String annotation = getAnnotation(annotationName, spectrumId);

            if (annotation != null) {
                annotations.put(annotationName,annotation);
            }
        }

        return annotations;
    }
}
