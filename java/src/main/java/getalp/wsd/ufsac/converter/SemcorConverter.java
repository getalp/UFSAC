package getalp.wsd.ufsac.converter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SemcorConverter extends DefaultHandler implements UFSACConverter
{
	private StreamingCorpusWriterDocument out;
	
	private Document currentDocument;
	
	private Paragraph currentParagraph;
	
	private Sentence currentSentence;

	private boolean saveCharacters;

	private String currentCharacters;

	private String currentPos;

	private String currentLemma;

	private String currentSenseKey;
	
	private int wnVersion;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
	    if (localName.equals("context"))
	    {
            currentDocument = new Document();
            currentDocument.setAnnotation("id", atts.getValue("filename"));
	    }
	    else if (localName.equals("p"))
	    {
            currentParagraph = new Paragraph(currentDocument);
	    }
	    else if (localName.equals("s"))
		{
			currentSentence = new Sentence(currentParagraph);
		}
		else if (localName.equals("wf"))
		{
			saveCharacters = true;
			currentCharacters = "";
			currentLemma = atts.getValue("lemma");
			currentPos = atts.getValue("pos");
			currentSenseKey = atts.getValue("lexsn");
		}
		else if (localName.equals("punc"))
		{
			saveCharacters = true;
			currentCharacters = "";
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (localName.equals("context"))
		{
			out.writeDocument(currentDocument);
		}
		else if (localName.equals("wf"))
		{
			Word w = new Word(currentSentence);
			w.setValue(currentCharacters);
			w.setAnnotation("lemma", currentLemma);
			w.setAnnotation("pos", currentPos);
			if (currentLemma != null && currentSenseKey != null)
			{
			    String[] senseKeys = currentSenseKey.split(";");
                List<String> newSenseKeys = new ArrayList<>();
			    for (String senseKey : senseKeys)
			    {
			        newSenseKeys.add(currentLemma + "%" + senseKey);
			    }
				w.setAnnotation("wn" + wnVersion + "_key", StringUtils.join(newSenseKeys, ";"));
			}
			saveCharacters = false;
		}
		else if (localName.equals("punc"))
		{
			Word w = new Word(currentSentence);
			w.setValue(currentCharacters);
			saveCharacters = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (saveCharacters)
		{
			currentCharacters += new String(ch, start, length);
		}
	}

	@Override
	public void convert(String inPath, String outPath, int wnVersion)
	{
		try
		{
			this.wnVersion = wnVersion;
			out = new StreamingCorpusWriterDocument();
			out.open(outPath);
			readFolder(inPath + "/brown1/");
			readFolder(inPath + "/brown2/");
			readFolder(inPath + "/brownv/");
			out.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void readFolder(String path) throws Exception
	{
        List<String> wordsList = new ArrayList<>();
        Stream<Path> paths = Files.list(Paths.get(path));
        paths.forEach(filePath -> 
        {
            wordsList.add(filePath.toString());
        });
        paths.close();
        Collections.sort(wordsList);
        for (String filePath : wordsList)
        {
            readFile(filePath);
        }
	}

	private void readFile(String path) throws Exception
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader saxReader = parser.getXMLReader();
		saxReader.setContentHandler(this);
		saxReader.parse(path);
	}
}
