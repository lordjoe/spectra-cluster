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

    public static String version = "-1";

    static {
        // get the location of version property file
        URL versionPropertyFileUrl = Version.class.getClassLoader().getResource("version.properties");

        // load version property file
        final Properties versionProperties = new Properties();
        try {
            versionProperties.load(new FileInputStream(new File(versionPropertyFileUrl.toURI())));
        } catch (URISyntaxException e) {
            throw new VersionException(e);
        } catch (FileNotFoundException e) {
            throw new VersionException(e);
        } catch (IOException e) {
            throw new VersionException(e);
        }

        version = versionProperties.getProperty("algorithm.version");
    }

    public static class VersionException extends RuntimeException {
        public VersionException() {
        }

        public VersionException(String message) {
            super(message);
        }

        public VersionException(String message, Throwable cause) {
            super(message, cause);
        }

        public VersionException(Throwable cause) {
            super(cause);
        }
    }

}
