package uk.ac.ebi.spectracluster.retrievers;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.hadoop.*;
import uk.ac.ebi.pride.spectracluster.retrievers.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.spectracluster.retrievers.IdentifiedSpectrumRetrieversTests
 *
 * @author Steve Lewis
 * @date 20/05/2014
 */
public class IdentifiedSpectrumRetrieversTests {

    public static final Random RND = new Random();
    private Set<String> identieiedIDs;
    private List<IPeptideSpectrumMatch> matches;
    private IdentifiedSpectrumRetriever retriever;

    @Before
    public void buildRetriever() {
        identieiedIDs = new HashSet<String>();
        retriever = new IdentifiedSpectrumRetriever();
        matches = ClusteringDataUtilities.readISpectraFromResource();
        for (IPeptideSpectrumMatch o : matches) {
            if (RND.nextDouble() < 0.5) {
                final String id = o.getId();
                retriever.addIdentifiedSpectrumId(id);
                identieiedIDs.add(id);
            }
        }
     }

    @Test
    public void testRetriever() {
        for (IPeptideSpectrumMatch match : matches) {
            final String id = match.getId();
            final Object identified = retriever.getProperty(id,"identified");
            if(identified == Boolean.TRUE)
                Assert.assertTrue( identieiedIDs.contains(id));
                
            
        }

    }


}
