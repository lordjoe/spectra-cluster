package com.lordjoe.hadoop;

/**
 * com.lordjoe.hadoop.TextKeyValue
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public class TextKeyValue {
    public static TextKeyValue[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TextKeyValue.class;

    private final String m_Key;
    private final String m_Value;

    public TextKeyValue(String key, String value) {
        m_Key = key;
        m_Value = value;
    }

    public String getKey() {
        return m_Key;
    }

    public String getValue() {
        return m_Value;
    }

    @Override
    public String toString() {
        return getKey() + "->" + getValue();
      }
}
