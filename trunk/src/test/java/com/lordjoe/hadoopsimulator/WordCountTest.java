package com.lordjoe.hadoopsimulator;

import java.util.*;

import com.lordjoe.hadoop.*;
import org.junit.*;

/**
 * com.lordjoe.hadoopsimulator.WordCountTest
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public class WordCountTest {
    public static WordCountTest[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = WordCountTest.class;

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

    /**
     * count the worde in the Gettysberg address - make sure we have all nultuword uses
     */
    @Test
    public void testWordCount()
    {
        String[] lines = GETTYSBERG_ADDRESS.split("\n");
        List<TextKeyValue> holder = new ArrayList<TextKeyValue>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            TextKeyValue tk = new TextKeyValue(Integer.toString(i),line);
            holder.add(tk);
        }
        HadoopJob hj = new HadoopJob(new WordMapper(),new WordCountReducer());
        Properties unusedConfig = new Properties();
        List<TextKeyValue> textKeyValues = hj.runJob(holder, unusedConfig);

        Assert.assertEquals(140,textKeyValues.size());

        Set<String>  MultiWordSet = new HashSet<String>(Arrays.asList(MULTI_USE_WORDS)) ;
        for (TextKeyValue tk : textKeyValues) {
               int count = Integer.parseInt(tk.getValue());
            if(count > 1)   {
                String word = tk.getKey();
                Assert.assertTrue(MultiWordSet.contains(word));
                MultiWordSet.remove(word);
            }
        }
        Assert.assertTrue(MultiWordSet.isEmpty());
    }

    public static final String[] MULTI_USE_WORDS =
            {
                    "A","AND","ARE","BE","BUT","CAN","CONCEIVED","DEAD",
                    "DEDICATE","DEDICATED","FAR","FOR","FROM","GAVE","GREAT",
                    "HAVE","HERE","IN","IS","IT","LIVING","LONG","MEN","NATION",
                    "NEW","NOT","OF","ON","OR","OUR","PEOPLE","RATHER","SHALL","SO",
                    "THAT","THE","THESE","THEY",
                    "THIS","TO","US","WAR","WE","WHAT","WHICH","WHO"
            };


    public static final String GETTYSBERG_ADDRESS =
            "Four score and seven years ago our fathers brought forth on this\n" +
                    " continent a new nation, conceived in liberty, and dedicated to the\n" +
                    " proposition that all men are created equal.\n" +
                    "Now we are engaged in a great civil war, testing whether\n" +
                    " that nation, or any nation so" +
                    " conceived and so dedicated, can long endure. We are met on a great\n" +
                    " battlefield of that war." +
                    " We have come to dedicate a portion of that field, as a final resting\n" +
                    " place for those who " +
                    "here gave their lives that that nation might live. It is altogether fitting and proper that we\n" +
                    "should do this.\n" +
                    "But, in a larger sense, we can not dedicate, we can not consecrate, we can not hallow this\n" +
                    "ground. The brave men, living and dead, who struggled here, have consecrated it, far above our\n" +
                    "poor power to add or detract. The world will little note, nor long remember what we say here,\n" +
                    " but it can never forget what they did here. It is for us the living, rather, to be dedicated\n" +
                    "here to the unfinished work which they who fought here have thus far so nobly advanced.\n" +
                    "It is rather for us to be here dedicated to the great task remaining before us—that from\n" +
                    "these honored dead we take increased devotion to that cause for which they gave the last full\n" +
                    "measure of devotion—that we here highly resolve that these dead shall not have died in vain—\n" +
                    "that this nation, under God, shall have a new birth of freedom—and that government of the people,\n" +
                    " by the people," +
                    " for the people, shall not perish from the earth.";
}
