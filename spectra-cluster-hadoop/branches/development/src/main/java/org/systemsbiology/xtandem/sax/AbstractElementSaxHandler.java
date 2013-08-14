package org.systemsbiology.xtandem.sax;

import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.AbstractElementSaxHandler
 * User: steven
 * Date: Jan 3, 2011
 */
public abstract class AbstractElementSaxHandler<T> extends AbstractSaxParser implements
        IElementHandler<T>
{
    public static final AbstractElementSaxHandler[] EMPTY_ARRAY = {};

    public   DelegatingSaxHandler m_Handler;
    public   IElementHandler m_Parent;

    private final String m_InitiatingTag;
    private final StringBuilder m_IncludedText = new StringBuilder();
    private final Map<String, String> m_Notes = new HashMap<String, String>();
    private T m_ElementObject;  // Object represented by this element


    protected AbstractElementSaxHandler(String initTag, IElementHandler pParent)
    {
        m_InitiatingTag = initTag;
        m_Parent = pParent;
        if (pParent != null) {
            if (pParent instanceof AbstractElementSaxHandler) {
                m_Handler = ((AbstractElementSaxHandler) pParent).getHandler();

            }
            else {
                m_Handler = null;
            }
        }
        else {
            m_Handler = null;
        }
    }

    protected AbstractElementSaxHandler(String initTag, DelegatingSaxHandler pParent)
    {
        m_InitiatingTag = initTag;
        m_Parent = null;
        m_Handler = pParent;
    }

    /**
     * todo make a more specialized class for this method
     * @return
     */
    public IMainData getMainData()
    {
        IElementHandler parent = getParent();
        if(parent != null && parent instanceof AbstractElementSaxHandler)
            return ((AbstractElementSaxHandler)parent).getMainData();
        return null; // not present
    }

    public static final String[] FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB = {
            "refine, point mutations",
            "scoring, cyclic permutation",
            "scoring, include reverse",
    };
    public void addNote(String key, String value)
    {
        boolean  forgiven = false;
        if(m_Notes.containsKey(key) ) {
            for (int i = 0; i < FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB.length; i++) {
                String test = FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB[i];
                if(test.equals(key)) {
                     forgiven = true;
                    break;
                }


            }
            String duplicateValue = m_Notes.get(key);
            if(!forgiven && duplicateValue.length() > 0 && !duplicateValue.equals(m_Notes.get(key)))
                throw new IllegalStateException("duplicate key in Notes file key=" + key +  " old value is " + m_Notes.get(key));
        }

        m_Notes.put(key, value);
        // use for generating tests
       // XTandemUtilities.outputLine("FOO.put(\"" + key + "\",\"" + value + "\");");
    }

    public String getNote(String key )
    {
       return m_Notes.get(key );
     }

    public void setParent(final IElementHandler pParent) {
        if(m_Parent == pParent)
            return;
        if(m_Parent != null)
            throw new IllegalStateException("m_ParentStream can only be set once");
        m_Parent = pParent;
    }

    public Map<String, String> getNotes()
    {
        return m_Notes;
    }

    public String getInitiatingTag()
    {
        return m_InitiatingTag;
    }

    public DelegatingSaxHandler getHandler()
    {
        return m_Handler;
    }

    public void setHandler(DelegatingSaxHandler pHandler)
    {
        if(m_Handler == pHandler)
            return;
       if(m_Handler != null)
           throw new IllegalStateException("handler can only be set once");
        m_Handler = pHandler;
    }

    public T getElementObject()
    {
        return m_ElementObject;
    }

    public void setElementObject(final T pElementObject)
    {
        m_ElementObject = pElementObject;
    }

    public IElementHandler getParent()
    {
        return m_Parent;
    }

    /**
     * return the file responsible
     */
    @Override
    public String getURL()
    {
        if (getParent() != null)
            return getParent().getURL();
        return null;
    }

    @Override
    public void characters(char[] s, int start, int length) throws SAXException
    {
        for (int i = 0; i < length; i++) {
            m_IncludedText.append(s[start + i]);
        }
    }

    public final String getIncludedText()
    {
        return m_IncludedText.toString();
    }

    public void clearIncludedText()
    {
        m_IncludedText.setLength(0);
    }


    @Override
    public void endElement(String elx, String localName, String el) throws SAXException
    {
        if ("note".equals(el)) {
            NoteSaxHandler handler = (NoteSaxHandler) getHandler().popCurrentHandler();
            final KeyValuePair valuePair = handler.getElementObject();
            if (valuePair != null)
                addNote(valuePair.getKey(), valuePair.getValue());
            return;
        }
        // added slewis
        String initiatingTag = getInitiatingTag();
        if (initiatingTag.equals(el)) {
            finishProcessing();

            final IElementHandler parent = getParent();
            if (parent != null)
                parent.endElement(elx, localName, el);
            return;
        }
        throw new UnsupportedOperationException("Cannot handle end tag " + el);
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        if ("note".equals(qName)) {
            NoteSaxHandler handler = new NoteSaxHandler(this);
            handler.handleAttributes(uri, localName, qName, attributes);
            getHandler().pushCurrentHandler(handler);
            return;
        }
        super.startElement(uri, localName, qName, attributes);

    }

    /**
      * Ignore this tag and all sub tags
      * @param uri
      * @param localName
      * @param qName
      * @param attributes
      */
     protected void setIgnoreTagContents(final String uri, final String localName, final String qName, final Attributes attributes)
     {
         AbstractElementSaxHandler handler = new DiscardingSaxParser(qName, this);
         getHandler().pushCurrentHandler(handler);
         try {
             handler.handleAttributes(uri, localName, qName, attributes);
         }
         catch (SAXException e) {
             throw new RuntimeException(e);

         }

     }

}
