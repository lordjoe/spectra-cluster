package uk.ac.ebi.pride.spectracluster.annotation;

import org.junit.*;

import java.util.*;

public class IdentifiedSpectralAnnotationRetrieverTest {


    public static final String[] IDENTIFIED_STRINGS = {
            "fee", "fie", "foe", "fum"
    };


    public static final String[] ALL_STRINGS = {
            "fee", "fie", "foe", "fum",
            "feeX", "fieX", "foeX", "fumX"
    };


    @Test
    public void testGetAnnotation() throws Exception {
        ISpectralAnnotationRetriever retr = new IdentifiedSpectralAnnotationRetriever(IDENTIFIED_STRINGS);
        Set<String> identified = new HashSet<String>(Arrays.asList(IDENTIFIED_STRINGS));

        for (int i = 0; i < ALL_STRINGS.length; i++) {
            String id = ALL_STRINGS[i];
            final String annotation = retr.getAnnotation(IdentifiedSpectralAnnotationRetriever.IDENTIFIED_ANNOTATION, id);
            if (identified.contains(id))
                Assert.assertEquals("true", annotation);
            else
                Assert.assertEquals("false", annotation);
        }


        for (int i = 0; i < ALL_STRINGS.length; i++) {
            String id = ALL_STRINGS[i];
            final Map<String, String> annotations = retr.getAnnotations(id);
            Assert.assertEquals(1, annotations.size());
        }

        Set<String> askedFor = new HashSet<String>();
        askedFor.add(IdentifiedSpectralAnnotationRetriever.IDENTIFIED_ANNOTATION);


        for (int i = 0; i < ALL_STRINGS.length; i++) {
            String id = ALL_STRINGS[i];
            final Map<String, String> annotations = retr.getAnnotations(id,askedFor);
            Assert.assertEquals(1, annotations.size());
        }
        askedFor.add("Species");
        try {
            retr.getAnnotations(ALL_STRINGS[0],askedFor);
            Assert.fail();
        } catch (SpectralAnnotationRetrieverException e) {
         }
    }


}