package uk.ac.ebi.pride.spectracluster.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * uk.ac.ebi.pride.spectracluster.util.Version
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 05/06/2014
 */
public class Version {

    public static final String BAD_VERSION = "-1";
    public static String version = BAD_VERSION;

    static {
        // get the location of version property file
        URL versionPropertyFileUrl = Version.class.getClassLoader().getResource("version.properties");

        // load version property file
        final Properties versionProperties = new Properties();
        try {
            versionProperties.load(new FileInputStream(new File(versionPropertyFileUrl.toURI())));
            version = versionProperties.getProperty("algorithm.version");
        } catch (Exception e) {
            version = BAD_VERSION;
        }

    }


}
