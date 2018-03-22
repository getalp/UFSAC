package getalp.wsd.ufsac.streaming.reader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.xml.XMLHelper;
import getalp.wsd.ufsac.core.*;

import org.xml.sax.helpers.DefaultHandler;

public class StreamingCorpusReader
{
    public void readBeginCorpus(Corpus corpus)
    {
        
    }

    public void readBeginDocument(Document document)
    {
        
    }

    public void readBeginParagraph(Paragraph paragraph)
    {
        
    }

    public void readBeginSentence(Sentence sentence)
    {
        
    }

    public void readWord(Word word)
    {
        
    }

    public void readEndSentence()
    {
        
    }

    public void readEndParagraph()
    {
        
    }

    public void readEndDocument()
    {
        
    }

    public void readEndCorpus()
    {
        
    }

    public void load(String path) 
    {
        try 
        {
            XMLReader saxReader = XMLReaderFactory.createXMLReader();
            SAXHandler handler = new SAXHandler();
            saxReader.setContentHandler(handler);
            saxReader.parse(path);
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }

	private class SAXHandler extends DefaultHandler
	{
	    @Override
		public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException
		{
	        if (localName.equals("corpus"))
	        {
	            readBeginCorpus(loadEntity(new Corpus(), atts));
	        }
            else if (localName.equals("document"))
            {
                readBeginDocument(loadEntity(new Document(), atts));
            }
            else if (localName.equals("paragraph"))
            {
                readBeginParagraph(loadEntity(new Paragraph(), atts));
            }
            else if (localName.equals("sentence"))
            {
                readBeginSentence(loadEntity(new Sentence(), atts));
            }
            else if (localName.equals("word"))
            {
                readWord(loadEntity(new Word(), atts));
            }
		}

	    @Override
		public void endElement(String uri, String localName, String qname) throws SAXException
		{
            if (localName.equals("corpus"))
            {
                readEndCorpus();
            }
            else if (localName.equals("document"))
            {
                readEndDocument();
            }
            else if (localName.equals("paragraph"))
            {
                readEndParagraph();
            }
            else if (localName.equals("sentence"))
            {
                readEndSentence();
            }
		}
	}
	
	private <T extends LexicalEntity> T loadEntity(T entity, Attributes atts)
	{
        for (int i = 0 ; i < atts.getLength() ; i++)
        {
            String attName = XMLHelper.fromValidXMLEntity(atts.getLocalName(i));
            String attValue = XMLHelper.fromValidXMLEntity(atts.getValue(i));
            entity.setAnnotation(attName, attValue);
        }
        return entity;
	}

}
