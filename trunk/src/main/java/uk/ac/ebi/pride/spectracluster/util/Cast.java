package uk.ac.ebi.pride.spectracluster.util;

/**
 * uk.ac.ebi.pride.spectracluster.util.Cast
 * User: Steve
 * Date: 6/21/13
 */
public class Cast {

    /**
     *
     * @param o
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T it(Object o) {
          return (T)o;
    }

}
