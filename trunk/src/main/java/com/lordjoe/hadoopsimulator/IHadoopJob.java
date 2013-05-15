package com.lordjoe.hadoopsimulator;

import com.lordjoe.hadoop.*;

import java.util.*;

/**
 * com.lordjoe.hadoopsimulator.IHadoopJob
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public interface IHadoopJob {
    public static IHadoopJob[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IHadoopJob.class;

    public List<TextKeyValue>  runJob(List<TextKeyValue> input,Properties config);
}
