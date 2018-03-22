package getalp.wsd.ufsac.converter;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.xml.SAXEntityResolverIgnoringDTD;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

import org.xml.sax.helpers.DefaultHandler;

public class WNGTConverter extends DefaultHandler implements CorpusConverter
{
	private StreamingCorpusWriterSentence out;

	private Sentence currentSentence;
	
	private Word currentWord;
	
	private String currentPos;
	
	private String currentLemma;
	
	private String currentSenseKey;

	private boolean inWord;

	private boolean saveContent;
	
	private String currentContent;
	
	private int wnVersion;

	@Override
	public void convert(String inPath, String outPath, int wnVersion)
	{
		this.wnVersion = wnVersion;
		out = new StreamingCorpusWriterSentence();
		out.open(outPath);
		try
		{
			XMLReader saxReader = XMLReaderFactory.createXMLReader();
			saxReader.setContentHandler(this);
			saxReader.setEntityResolver(new SAXEntityResolverIgnoringDTD());
			saxReader.parse(inPath + "/merged/noun.xml");
			saxReader.parse(inPath + "/merged/adj.xml");
			saxReader.parse(inPath + "/merged/verb.xml");
			saxReader.parse(inPath + "/merged/adv.xml");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		out.close();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (localName.equals("synset"))
		{
			currentSentence = new Sentence();
		}
		else if (localName.equals("sk"))
		{
			saveContent = true;
			currentContent = "";
		}
		else if (localName.equals("wf"))
		{
			currentWord = new Word(currentSentence);
			inWord = true;
			saveContent = true;
			currentContent = "";
			currentPos = atts.getValue("pos");
			currentLemma = atts.getValue("lemma");
			if (currentLemma != null && currentLemma.contains("%"))
			{
				currentLemma = currentLemma.substring(0, currentLemma.indexOf("%"));
			}
			currentSenseKey = "";
		}
		else if (localName.equals("glob"))
		{
			currentWord = new Word(currentSentence);
			currentPos = "";
			currentLemma = atts.getValue("lemma");
			if (currentLemma != null && currentLemma.contains("%"))
			{
				currentLemma = currentLemma.substring(0, currentLemma.indexOf("%"));
			}
			currentSenseKey = "";
		}
		else if (localName.equals("id"))
		{
			if (!currentWord.hasAnnotation("wn" + wnVersion + "_key"))
			{
				currentSenseKey = atts.getValue("sk");
			}
			if (inWord)
			{
				currentLemma = atts.getValue("lemma");
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
        if (localName.equals("synset"))
        {
            out.writeSentence(currentSentence);
        }
        else if (localName.equals("sk"))
		{
			saveContent = false;
			if (currentSentence.hasAnnotation("wn" + wnVersion + "_key"))
			{
				String currentKey = currentSentence.getAnnotationValue("wn" + wnVersion + "_key");
				currentKey += ";" + currentContent;
				currentSentence.setAnnotation("wn" + wnVersion + "_key", currentKey);
			}
			else
			{
				currentSentence.setAnnotation("wn" + wnVersion + "_key", currentContent);
			}
		}
		else if (localName.equals("wf"))
		{
			currentWord.setValue(currentContent);
			currentWord.setAnnotation("pos", currentPos);
			if (currentLemma != null && !currentLemma.equals("purposefully ignored"))
			{
			    currentWord.setAnnotation("lemma", currentLemma);
			}
			if (!currentSenseKey.equals("purposefully_ignored%0:00:00::"))
			{
			    currentWord.setAnnotation("wn" + wnVersion + "_key", currentSenseKey);
			}
			saveContent = false;
			inWord = false;
		}
		else if (localName.equals("glob"))
		{
			currentWord.setValue(currentLemma);
	        if (currentLemma != null && !currentLemma.equals("purposefully ignored"))
	        {
	            currentWord.setAnnotation("lemma", currentLemma);
	        }
            if (!currentSenseKey.equals("purposefully_ignored%0:00:00::"))
            {
                currentWord.setAnnotation("wn" + wnVersion + "_key", currentSenseKey);
            }
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (saveContent)
		{
			currentContent += new String(ch, start, length);
		}
	}
}
