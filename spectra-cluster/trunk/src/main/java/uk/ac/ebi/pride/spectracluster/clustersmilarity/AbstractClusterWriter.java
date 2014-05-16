package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender;
import uk.ac.ebi.pride.spectracluster.util.OrPredicate;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AbstractClusterWriter implements TypedVisitor<IPeptideSpectrumCluster> {
    private final Appendable writer;
    private final OrPredicate<IPeptideSpectrumCluster> tests;

    public AbstractClusterWriter(Appendable writer, TypedPredicate<IPeptideSpectrumCluster>... testClauses) {
        this.writer = writer;
        tests = new OrPredicate(testClauses);
    }

    @Override
    public void visit(@Nonnull IPeptideSpectrumCluster pT) {
        if (tests.apply(pT))
            new DotClusterClusterAppender().appendCluster(writer, pT);
    }

    public Appendable getWriter() {
        return writer;
    }

}
