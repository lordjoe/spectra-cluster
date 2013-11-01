package uk.ac.ebi.pride.spectracluster.hadoop.mgf;

import uk.ac.ebi.pride.spectracluster.hadoop.ClusterLauncher;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterLauncherMain {

    public static void main(String[] args) {
        ClusterLauncher.workingMain(args, MgfClusterLancherJobBuilder.FACTORY);
    }
}
