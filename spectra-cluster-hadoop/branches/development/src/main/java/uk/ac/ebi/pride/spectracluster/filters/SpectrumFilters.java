package uk.ac.ebi.pride.spectracluster.filters;

import com.lordjoe.filters.*;
import org.xml.sax.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters
 *
 * @author Steve Lewis
 * @date 16/05/2014
 */
public class SpectrumFilters {

    public static final String TAG = "SpectrumFilter";

    static {
        TypedFilterCollection.registerHandler(TAG, new SpectrumFilterSaxHandler(null));
    }

    /**
     * filter of type file
     */
    protected static abstract class AbstractSpectrumTypedFilter extends AbstractTypedFilter<ISpectrum> {
        public AbstractSpectrumTypedFilter() {
            super(ISpectrum.class);
        }
    }


    /**
     * return true of a ISpectrum a length greater than maxlength
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getMinimumLengthFilter(final long length) {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                if (testObject.getPeaksCount() >= length)
                    return testObject;
                return null;
            }
        };
    }


    /**
     * return true of a ISpectrum a length greater than maxlength
     *
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getIdentifiedFilter() {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                if (testObject instanceof IPeptideSpectrumMatch) {
                    IPeptideSpectrumMatch ps = (IPeptideSpectrumMatch) testObject;
                    if (ps.getPeptide() != null)
                        return testObject; // passes
                    return null; // fails

                }
                throw new UnsupportedOperationException("Fix This"); // ToDo
            }
        };
    }


    public static final float MINIMUM_PRECURSOR_CHARGE = 10F;
    /**
     * return true of a ISpectrum  has known precursor mz and charge
     * @param length max allowed length
     * @return
     */
    public static ITypedFilter<ISpectrum> getWithPrecursorsFilter() {
        return new AbstractSpectrumTypedFilter() {
            /**
             * return 0 if it passes the filter otherwise return null
             *
             * @param testObject
             * @return as above
             */
            @Override
            public ISpectrum passes(@Nonnull ISpectrum testObject) {
                if (testObject instanceof IPeptideSpectrumMatch) {
                    IPeptideSpectrumMatch ps = (IPeptideSpectrumMatch) testObject;
                    if (ps.getPrecursorCharge() == 0)
                        return null; // fails
                    // really testing 0 but this works
                    if (ps.getPrecursorMz() < MINIMUM_PRECURSOR_CHARGE )
                          return null; // fails
                      return testObject; // passes

                }
                throw new UnsupportedOperationException("Fix This"); // ToDo
            }
        };
    }


    /**
     * com.lordjoe.filters.FilterCollectionSaxHandler
     * reads xml document <Filters></Filters>
     *
     * @author Steve Lewis
     * @date 16/05/2014
     */
    public static class SpectrumFilterSaxHandler extends AbstractFilterCollectionSaxHandler<ISpectrum> {


        public SpectrumFilterSaxHandler(FilterCollectionSaxHandler parent) {
            super(TAG, parent);
        }


        @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
        @Override
        public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use ISpectrum | Settings | ISpectrum Templates.
            String value;

            value = attributes.getValue("identified");
            if (value != null) {
                setElementObject(getIdentifiedFilter());
                return;
            }

            value = attributes.getValue("withPrecursors");
            if ("true".equals(value)) {
                setElementObject(getWithPrecursorsFilter());
                return;
            }

            value = attributes.getValue("minimumLength");
            if (value != null) {
                int length = Integer.parseInt(value);
                setElementObject(getMinimumLengthFilter(length));
                return;
            }

            StringBuilder sb = new StringBuilder();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < attributes.getLength(); i++) {
                sb.append(attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\" ");

            }
            throw new UnsupportedOperationException("no ISpectrum filters we understand " + sb);
        }

        @Override
        public void endElement(String elx, String localName, String el) throws SAXException {
            super.endElement(elx, localName, el);

        }


    }
}
