package com.lordjoe.hadoopsimulator;

import com.lordjoe.hadoop.ITextMapper;
import com.lordjoe.hadoop.ITextReducer;
import com.lordjoe.hadoop.TextKeyValue;

import java.util.*;

/**
 * com.lordjoe.hadoopsimulator.HadoopJob
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public class HadoopJob implements IHadoopJob {
    private final ITextMapper m_Mapper;
    private final ITextReducer m_Reducer;

    public HadoopJob(ITextMapper mapper, ITextReducer reducer) {
        m_Mapper = mapper;
        m_Reducer = reducer;
    }

    public ITextMapper getMapper() {
        return m_Mapper;
    }

    public ITextReducer getReducer() {
        return m_Reducer;
    }


    public List<TextKeyValue> runJob(List<TextKeyValue> input, Properties config) {

        // map step
        Map<String,List<TextKeyValue>> emittedData = new HashMap<String,List<TextKeyValue>>();
        ITextMapper mapper = getMapper();
         for (TextKeyValue kv : input) {
             TextKeyValue[] map = mapper.map(kv.getKey(), kv.getValue(), config);
             for (int i = 0; i < map.length; i++) {
                 TextKeyValue textKeyValue = map[i];
                 addToEmittedData(  textKeyValue,emittedData);
             }
         }

        // Sort step
        String[] keys = emittedData.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // reduce step
        List<TextKeyValue> ret = new ArrayList<TextKeyValue>();
        ITextReducer reducer = getReducer();
        for (String key : keys) {
            List<TextKeyValue> values = emittedData.get(key);
            List<String> holder = new ArrayList<String>();
             for (TextKeyValue tv : values) {
                holder.add(tv.getValue());
            }
            TextKeyValue[] items = reducer.reduce(key, holder, config);
            ret.addAll(Arrays.asList(items));
        }
       return ret;
    }

    protected void addToEmittedData(TextKeyValue textKeyValue, Map<String,List<TextKeyValue>> emittedData) {
        String key = textKeyValue.getKey();
        List<TextKeyValue> list = emittedData.get(key) ;
        if(list == null)    {
            list = new ArrayList<TextKeyValue>();
            emittedData.put(key,list);
        }
        list.add(textKeyValue);

    }
}
