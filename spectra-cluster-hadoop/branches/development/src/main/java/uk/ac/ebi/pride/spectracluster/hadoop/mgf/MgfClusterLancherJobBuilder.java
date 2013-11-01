package uk.ac.ebi.pride.spectracluster.hadoop.mgf;

import uk.ac.ebi.pride.spectracluster.hadoop.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterLancherJobBuilder extends AbstractClusterLauncherJobBuilder {

    public static final Class[] JOB_CLASSES = {
                    MgfClusterDBLoadingJob.class,
    } ;

    public MgfClusterLancherJobBuilder(ClusterLauncher launcher) {
        super(launcher,JOB_CLASSES);
    }

}
