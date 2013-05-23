package com.lordjoe.hadoopsimulator;

import com.lordjoe.hadoop.*;

import java.util.*;

/**
 * com.lordjoe.hadoopsimulator.WordCount
 *
 * @author Steve Lewis
 * @date 23/05/13
 */
public class WordCount {

    public static String cleanUpWord(String in)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if(!Character.isLetter(c))
                continue; // ignore space, punctuation, numbers
            sb.append(Character.toUpperCase(c)); // capitalize for consitency

        }

        return sb.toString();
    }

    /**
     * word count mapper - takes lines  - break into words
     * emit real words as upper case count 1
     */
    public static class WordMapper implements ITextMapper {
        @Override
        public TextKeyValue[] map(String key, String value, Properties config) {
            String[] items = value.split(" ");
            List<TextKeyValue> holder = new ArrayList<TextKeyValue>();

            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                item = cleanUpWord(item);
                if (item.length() > 0)
                    holder.add(new TextKeyValue(item, "1"));
            }
            TextKeyValue[] ret = new TextKeyValue[holder.size()];
            holder.toArray(ret);
            return ret;
        }
    }

    /**
     * standard WordCount Reducer for out jobs
     */
    public static class WordCountReducer implements ITextReducer {

        @Override
        public TextKeyValue[] reduce(String key, List<String> values, Properties config) {
            String word = key;
            int count = 0;
            for (String value : values) {
                count += Integer.parseInt(value);
            }
            TextKeyValue sumCount = new TextKeyValue(word,Integer.toString(count));
            TextKeyValue[] ret = { sumCount};
            return ret;
        }
     }



 }
