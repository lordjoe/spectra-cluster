package uk.ac.ebi.pride.spectracluster.hadoop;

import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ClusterLauncherJobBuilder
 *
 * @author Steve Lewis
 * @date 29/10/13
 */
public class ClusterLauncherJobBuilder extends AbstractClusterLauncherJobBuilder {

    public static final IJobBuilderFactory FACTORY = new IJobBuilderFactory() {
        @Override
        public IJobBuilder getJobBuilder(IStreamOpener launcher,Object... otherData) {
            return new  ClusterLauncherJobBuilder((ClusterLauncher)launcher);
        }
    };

    public static final Class[] JOB_CLASSES =
            {
                    SpectraPeakClustererPass1.class,
                    SpectraClustererMergerOffset.class,
                    SpectraClustererMerger.class,
                     ClusterConsolidator.class
            } ;



    public ClusterLauncherJobBuilder(ClusterLauncher launcher) {
        super(launcher,JOB_CLASSES);
    }

    /**
     * if we start at the first job - how many jobs are there in total
     *
     * @return
     */
    @Override
    public int getNumberJobs() {
        // patch for development
        if(true)
            return 4;
        return super.getNumberJobs();

    }
}
