package getalp.wsd.ufsac.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.*;

import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Semeval2015Task13Converter extends DefaultHandler implements UFSACConverter
{
	private StreamingCorpusWriterDocument out;

	private Document currentDocument;

	private Paragraph currentParagraph;

	private Sentence currentSentence;
	
	private Word currentWord;

	private boolean saveCharacters;

	private String currentCharacters;

	private String currentPos;

	private String currentLemma;
	
	private String currentWordId;

    private Map<String, String> sensesById;
    
    private int wnVersion;
    
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (localName.equals("text"))
		{
			currentDocument = new Document();
			currentDocument.setAnnotation("id", atts.getValue("id"));
			currentParagraph = new Paragraph(currentDocument);
		}
		else if (localName.equals("sentence"))
		{
			currentSentence = new Sentence(currentParagraph);
			currentSentence.setAnnotation("id", atts.getValue("id"));
		}
		else if (localName.equals("wf"))
		{
			saveCharacters = true;
			currentCharacters = "";
			currentPos = atts.getValue("pos");
			currentLemma = atts.getValue("lemma");
			currentWordId = atts.getValue("id");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
	    if (localName.equals("text"))
        {
            out.writeDocument(currentDocument);
        }
        else if (localName.equals("wf"))
		{
			currentWord = new Word(currentSentence);
			currentWord.setValue(currentCharacters.replaceAll(" ", "_"));
			if (currentLemma != null)
			{
			    currentLemma = currentLemma.toLowerCase();
			    currentLemma = currentLemma.replaceAll(" ", "_");
			    currentWord.setAnnotation("lemma", currentLemma);
			}
			if (currentPos != null)
			{
			    currentPos = POSConverter.toWNPOS(currentPos);
			    currentWord.setAnnotation("pos", currentPos);
			}
			if (currentWordId != null)
			{
			    currentWord.setAnnotation("id", currentWordId);
    			if (sensesById.containsKey(currentWordId))
    			{
    			    String realLemma = currentLemma;
    			    String realPos = currentPos;
    			    String realSenseKey = sensesById.get(currentWordId);
    			    
                    String[] possibleSenseKeys = sensesById.get(currentWordId).split(";");
                    if (possibleSenseKeys.length == 1)
                    {
                        realLemma = possibleSenseKeys[0].substring(0, possibleSenseKeys[0].indexOf("%"));
                        realPos = POSConverter.toWNPOS(Integer.valueOf(possibleSenseKeys[0].substring(possibleSenseKeys[0].indexOf("%") + 1, possibleSenseKeys[0].indexOf("%") + 2)));
                    }
                    else
                    {
                        List<String> realSenseKeys = new ArrayList<>();
                        boolean found = false;
                        for (String possibleSenseKey : possibleSenseKeys)
                        {
                            if (possibleSenseKey.substring(0, possibleSenseKey.indexOf("%")).equals(currentLemma))
                            {
                                realSenseKeys.add(possibleSenseKey);
                                found = true;
                            }
                        }
                        for (String possibleSenseKey : possibleSenseKeys)
                        {
                            if (!found && currentLemma != null && possibleSenseKey.substring(0, possibleSenseKey.indexOf("%")).contains(currentLemma))
                            {
                                realSenseKeys.add(possibleSenseKey);
                                realLemma = possibleSenseKey.substring(0, possibleSenseKey.indexOf("%"));
                                realPos = POSConverter.toWNPOS(Integer.valueOf(possibleSenseKey.substring(possibleSenseKey.indexOf("%") + 1, possibleSenseKey.indexOf("%") + 2)));
                                found = true;
                            }
                        }
                        for (String possibleSenseKey : possibleSenseKeys)
                        {
                            if (!found && currentCharacters != null && possibleSenseKey.substring(0, possibleSenseKey.indexOf("%")).contains(currentCharacters))
                            {
                                realSenseKeys.add(possibleSenseKey);
                                realLemma = possibleSenseKey.substring(0, possibleSenseKey.indexOf("%"));
                                realPos = POSConverter.toWNPOS(Integer.valueOf(possibleSenseKey.substring(possibleSenseKey.indexOf("%") + 1, possibleSenseKey.indexOf("%") + 2)));
                            }
                        }
                        realSenseKey = StringUtils.join(realSenseKeys, ";");
                    }

                    currentWord.setAnnotation("lemma", realLemma);
                    currentWord.setAnnotation("pos", realPos);
                    currentWord.setAnnotation("wn" + wnVersion + "_key", realSenseKey);
    			}
			}
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
    public void convert(String inpath, String outpath, int wnVersion)
    {
        this.wnVersion = wnVersion;
        out = new StreamingCorpusWriterDocument();
        out.open(outpath);
        loadSenses(inpath + "/keys/gold_keys/EN/semeval-2015-task-13-en.key");
        loadCorpus(inpath + "/data/semeval-2015-task-13-en.xml");
        out.close();
    }

	public void loadCorpus(String path)
	{
		try
		{
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            SAXParser parser = parserFactory.newSAXParser();
            XMLReader saxReader = parser.getXMLReader();
			saxReader.setContentHandler(this);
			saxReader.parse(path);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	   
    private void loadSenses(String path)
    {
        sensesById = new HashMap<>();
        try
        {
            Scanner sc = new Scanner(new File(path));
            while (sc.hasNextLine())
            {
                String line = sc.nextLine();
                String[] tokens = line.split("\\s+");
                String id = tokens[1];
                List<String> senses = new ArrayList<>();
                for (int i = 3 ; i < tokens.length ; i++)
                {
                    if (tokens[i].startsWith("wn:"))
                    {
                        senses.add(tokens[i].substring(3).replaceAll("%5", "%3").toLowerCase());
                    }
                }
                if (!senses.isEmpty())
                {
                    String sense = StringUtils.join(senses, ";");
                    sensesById.put(id, sense);
                }
            }
            sc.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
