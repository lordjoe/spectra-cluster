package uk.ac.ebi.pride.spectracluster.retrievers;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.retrievers.IdentifiedSpectrumRetriever
 *
 * @author Steve Lewis
 * @date 20/05/2014
 */
public class IdentifiedSpectrumRetriever implements ISpectralPropertiesRetriever {
    public static final String IDENTIFIED_PROPERTY = "identified";

    private final Set<String> identifiedIds = new HashSet<String>();


    public void addIdentifiedSpectrumId(String id)
    {
        identifiedIds.add(id);
    }
    /**
     * return property value or null of it does not exist
     *
     * @param spectralId id of the spectrum
     * @param propName   name of the property
     * @return value or null
     */
    @Override
    public Object getProperty(@Nonnull String spectralId, @Nonnull String propName) {
        if (IDENTIFIED_PROPERTY.equalsIgnoreCase(propName))
            return identifiedIds.contains(spectralId) ? Boolean.TRUE : null;
        return null;
    }

    /**
     * return a map of requested property values
     *
     * @param spectralId id of the spectrum
     * @param properties names of the properties
     * @return map    any property which can be fetched will be present or represented by NULL_OBJECT
     */
    @Nonnull @Override
    public Map<String, Object> getProperties(@Nonnull String spectralId, @Nonnull Collection<String> properties) {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (String property : properties) {
            if (IDENTIFIED_PROPERTY.equalsIgnoreCase(property))
                ret.put(IDENTIFIED_PROPERTY, identifiedIds.contains(spectralId) ? Boolean.TRUE : Boolean.FALSE);
        }
        return ret;
    }

    /**
     * return a map of requested all retrievable property values
     *
     * @param spectralId id of the spectrum
     * @return map    any property which can be fetched will be present or represented by NULL_OBJECT
     */
    @Nonnull @Override
    public Map<String, Object> getProperties(@Nonnull String spectralId) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put(IDENTIFIED_PROPERTY, identifiedIds.contains(spectralId) ? Boolean.TRUE : Boolean.FALSE);
        return ret;
    }

    /**
     * return a map of requested property values
     *
     * @param ids ids of the spectrum to bulk load
     * @return a retriever to use with the listed spectra
     */
    @Nonnull @Override
    public ISpectralPropertiesRetriever bulkLoad(@Nonnull Collection<String> ids) {
          return this; // bulk load not supported
    }
}
