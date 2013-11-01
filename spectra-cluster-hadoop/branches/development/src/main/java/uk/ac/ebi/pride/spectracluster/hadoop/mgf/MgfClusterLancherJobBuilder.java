package uk.ac.ebi.pride.spectracluster.hadoop.mgf;

import org.systemsbiology.hadoop.IJobBuilder;
import org.systemsbiology.hadoop.IJobBuilderFactory;
import org.systemsbiology.hadoop.IStreamOpener;
import uk.ac.ebi.pride.spectracluster.hadoop.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterLancherJobBuilder extends AbstractClusterLauncherJobBuilder {

    public static final IJobBuilderFactory FACTORY = new IJobBuilderFactory() {
        @Override
        public IJobBuilder getJobBuilder(IStreamOpener launcher, Object ... added) {
            return new MgfClusterLancherJobBuilder((ClusterLauncher)launcher);
        }
    };

    public static final Class[] JOB_CLASSES = {
                    MgfClusterDBLoadingJob.class,
    } ;

    public MgfClusterLancherJobBuilder(ClusterLauncher launcher) {
        super(launcher,JOB_CLASSES);
    }

}
