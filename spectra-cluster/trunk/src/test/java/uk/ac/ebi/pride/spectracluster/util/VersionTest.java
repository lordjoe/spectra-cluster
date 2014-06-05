package uk.ac.ebi.pride.spectracluster.util;

import org.junit.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.VersionTest
 *
 * @author Steve Lewis
 * @date 05/06/2014
 */
public class VersionTest {
    @Test
    public void testVersion()
    {
        Assert.assertFalse("-1".equals(Version.Version) );
    }
}
