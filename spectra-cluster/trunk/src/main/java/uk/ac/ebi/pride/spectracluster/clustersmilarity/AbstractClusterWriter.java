package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender;
import uk.ac.ebi.pride.spectracluster.util.OrPredicate;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AbstractClusterWriter implements TypedVisitor<IPeptideSpectralCluster> {
    private final Appendable writer;
    private final OrPredicate<IPeptideSpectralCluster> tests;

    public AbstractClusterWriter(Appendable writer, TypedPredicate<IPeptideSpectralCluster>... testClauses) {
        this.writer = writer;
        tests = new OrPredicate(testClauses);
    }

    @Override
    public void visit(@Nonnull IPeptideSpectralCluster pT) {
        if (tests.apply(pT))
            new DotClusterClusterAppender().appendCluster(writer, pT);
    }

    public Appendable getWriter() {
        return writer;
    }

}
