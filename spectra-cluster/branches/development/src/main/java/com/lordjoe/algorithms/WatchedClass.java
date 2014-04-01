package com.lordjoe.algorithms;


import javax.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * com.lordjoe.algorithms.WatchedClass
 * BY making this a superclass a count will be made of where objects
 * are constructed and how many remain which can be used for tracking memory leaks
 * User: Steve
 * Date: 3/19/14
 */
public class WatchedClass {
    public static final long DEFAULT_REPORT_INTERVAl_SEC = 120; // 120 sec
    public static final int SECONDS_PER_MILLISEC = 1000;
    /**
     * how often do we want to look at class uaage
     */
    private static long gReportIntervalSec = DEFAULT_REPORT_INTERVAl_SEC;
    /**
     * if true print reports to stdout
     */
    private static boolean gShowReports = true;

    public static long getReportIntervalSec() {
        return gReportIntervalSec;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void setReportIntervalSec(final long pReportIntervalSec) {
        gReportIntervalSec = pReportIntervalSec;
    }

    public static boolean isShowReports() {
        return gShowReports;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void setShowReports(final boolean pShowReports) {
        gShowReports = pShowReports;
    }

    /**
     * we keep a list of created classes and a  WatchedClassHolder recording use
     */
    private static final Map<Class, WatchedClassHolder> gWatchUsages = new ConcurrentHashMap<Class, WatchedClassHolder>();
    /**
     * interned Strings of the stack trace to where the objects were created -
     * may not tell who is holding a reference but at least adds a point to look
     */
    private static final Set<String> gLocations = new HashSet<String>();

    /**
     * return locations in the code where all instances of WatchedClass were created
     *
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public static @Nonnull List<CreateLocation> getCreateLocations() {
        List<CreateLocation> holder = new ArrayList<CreateLocation>();
        for (WatchedClassHolder wc : gWatchUsages.values()) {
            holder.addAll(wc.getCreateLocations());
        }
        Collections.sort(holder);
        return holder;
    }

    /**
     * build a string describing all classe and their uses
     *
     * @return
     */
    public synchronized @Nonnull static String buildReport() {
        StringBuilder sb = new StringBuilder();
        for (Class aClass : gWatchUsages.keySet()) {
            WatchedClassHolder wc = gWatchUsages.get(aClass);
            sb.append(aClass.getSimpleName());
            //noinspection StringConcatenationInsideStringBufferAppend
            sb.append(" " + wc.getUseCount());
            sb.append("\n");

        }
        return sb.toString();
    }

    /**
     * return the holder associated with a class building it as needed
     *
     * @param item class to look
     * @return associated holder
     */
    protected synchronized static
    @Nonnull WatchedClassHolder getHolder(@Nonnull WatchedClass item) {
        Class<? extends WatchedClass> aClass = item.getClass();
        WatchedClassHolder ret = gWatchUsages.get(aClass);
        if (ret == null) {
            ret = new WatchedClassHolder(aClass);
            gWatchUsages.put(aClass, ret);
        }
        return ret;
    }


    /**
     * base classe which records details of creation and remembers instances still 'alive'
     */
    public WatchedClass() {
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        Class realClass = getClass();
        WatchedClassHolder holder = getHolder(this);
        String codeLocation = buildCreateLocation();
        holder.register(this, codeLocation);
    }

    /**
     * use the stack trace to report where inteh code an object was created
     */
    public static final int NUMBER_CONSTANT_FRAMES = 2; // we know we are in  buildCreateLocation and  WatchedClass<imit>

    private
    @Nonnull String buildCreateLocation() {
        StringBuilder sb = new StringBuilder();
        Throwable t = new RuntimeException();
        StackTraceElement[] stackTrace = t.getStackTrace();
        // drop says known stack frames
        for (int i = NUMBER_CONSTANT_FRAMES; i < stackTrace.length; i++) {
            StackTraceElement se = stackTrace[i];
            sb.append(se);
            sb.append("\n");
        }
        String s = sb.toString();
        return s.intern(); // there better be one copy
    }


    /**
     * remember all instances of a class created using a WeakHashMap
     * so as instances are garbage collected they are forgotten
     */
    public static class WatchedClassHolder {
        private final Class<? extends WatchedClass> m_Target;
        private final WeakHashMap<WeakReference<? extends WatchedClass>, String> m_Instances = new WeakHashMap<WeakReference<? extends WatchedClass>, String>();
        private long m_LastClean = System.currentTimeMillis();

        public WatchedClassHolder(final Class<? extends WatchedClass> pTarget) {
            m_Target = pTarget;
        }

        public int getUseCount() {
            return m_Instances.size();
        }

        public Class<? extends WatchedClass> getTarget() {
            return m_Target;
        }

        /**
         * return a new list of locations in the code (stack traces) where
         * instances of the target class were created
         *
         * @return
         */
        public List<CreateLocation> getCreateLocations() {
            Set<CreateLocation> locs = new HashSet<CreateLocation>();
            synchronized (m_Instances) {
                for (String loc : m_Instances.values()) {
                    locs.add(new CreateLocation(getTarget(), loc));
                }
            }
            List<CreateLocation> holder = new ArrayList<CreateLocation>(locs);
            Collections.sort(holder);

            return holder;

        }

        /**
         * remember the creation of an instance
         *
         * @param x
         * @param location
         */
        public void register(@Nonnull WatchedClass x, @Nonnull String location) {
            synchronized (m_Instances) {
                m_Instances.put(new WeakReference<WatchedClass>(x), location);
            }
            synchronized (gLocations)
            {
                if (!gLocations.contains(location)) {
                                System.out.println("==== New Build Location ====");
                                System.out.println(location);
                                gLocations.add(location);
                            }
            }
            // every report interval look at usage
            if (System.currentTimeMillis() > m_LastClean + getReportIntervalSec() * SECONDS_PER_MILLISEC) {
                clean();
            }

        }

        /**
         * clean up storage and prine a report on usage
         */
        protected void clean() {
            m_LastClean = System.currentTimeMillis();
            if (isShowReports()) {
                String report = buildReport();
                System.err.println(report);
            }
        }
    }

    /**
     * location where an instance of a class was created
     */
    public static class CreateLocation implements Comparable<CreateLocation> {
        private final Class m_TargetClass;
        private final String m_StackTrace;

        public CreateLocation(final Class pTargetClass, final String pStackTrace) {
            m_TargetClass = pTargetClass;
            m_StackTrace = pStackTrace;
        }

        public Class getTargetClass() {
            return m_TargetClass;
        }

        public String getStackTrace() {
            return m_StackTrace;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CreateLocation that = (CreateLocation) o;

            if (!m_TargetClass.equals(that.m_TargetClass)) return false;
            //noinspection RedundantIfStatement
            if (!m_StackTrace.equals(that.m_StackTrace)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = m_TargetClass.hashCode();
            result = 31 * result + m_StackTrace.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return m_TargetClass.toString() + "\n"
                    + m_StackTrace;
        }


        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(final CreateLocation o) {
            Class me = getTargetClass();
            Class otherClass = o.getTargetClass();
            if (me != otherClass)
                return me.toString().compareTo(otherClass.toString());
            return getStackTrace().compareTo(o.getStackTrace());
        }
    }

}
