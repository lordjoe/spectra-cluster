package uk.ac.ebi.pride.spectracluster.hadoop;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraHadoopMain
 * User: Steve
 * Date: 8/14/13
 */
public class SpectraHadoopMain {
    protected static void usage() {
        System.out.println("Usage <input file or directory> <output directory>");
    }


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            return;
        }


        String[] pass1Args = {args[0], "output1"};
        SpectraPeakClustererPass1.main(pass1Args);

        String[] pass2Args = {"output1", "output2"};
        SpectraClustererMerger.main(pass2Args);

        String[] pass3Args = {"output2", "output3"};
        ClusterConsolidator.main(pass3Args);
    }

}
