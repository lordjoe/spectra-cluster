package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AbstractClusterWriter implements TypedVisitor<ICluster> {
    private final Appendable writer;
    private final OrPredicate<ICluster> tests;

    public AbstractClusterWriter(Appendable writer, TypedPredicate<ICluster>... testClauses) {
        this.writer = writer;
        tests = new OrPredicate(testClauses);
    }

    @Override
    public void visit(@Nonnull ICluster pT) {
        if (tests.apply(pT))
            new DotClusterClusterAppender().appendCluster(writer, pT);
    }

    public Appendable getWriter() {
        return writer;
    }

}
