package getalp.wsd.ufsac.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.*;

import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class Semeval2013Task12Converter extends SAXBasicHandler implements UFSACConverter
{
	private StreamingCorpusWriterDocument out;

	private Document currentDocument;

	private Paragraph currentParagraph;

	private Sentence currentSentence;
	
	private Word currentWord;

	private String currentPos;

	private String currentLemma;
	
	private String currentWordId;

    private Map<String, String> sensesById;
    
    private int wnVersion;
    
    private String lang;
    
    public Semeval2013Task12Converter() 
    {
		this("en");
	}
    
    public Semeval2013Task12Converter(String lang) 
    {
		this.lang = lang;
	}
    
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
		else if (localName.equals("instance"))
		{
			resetAndStartSaveCharacters();
			currentPos = atts.getValue("pos");
			currentLemma = atts.getValue("lemma").toLowerCase();
			currentWordId = atts.getValue("id");
		}
		else if (localName.equals("wf"))
		{
			resetAndStartSaveCharacters();
		    currentPos = atts.getValue("pos");
		    if (atts.getValue("lemma") != null)
		    {
			    currentLemma = atts.getValue("lemma").toLowerCase();
		    }
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
	    if (localName.equals("text"))
        {
            out.writeDocument(currentDocument);
        }
        else if (localName.equals("instance"))
		{
			currentWord = new Word(currentSentence);
			currentWord.setValue(getAndStopSaveCharacters());
			currentWord.setAnnotation("lemma", currentLemma);
			currentWord.setAnnotation("pos", currentPos);
			currentWord.setAnnotation("id", currentWordId);
			if (sensesById.containsKey(currentWordId))
			{
				currentWord.setAnnotation("wn" + wnVersion + "_key", sensesById.get(currentWordId));
			}
		}
		else if (localName.equals("wf"))
		{
            currentWord = new Word(currentSentence);
            currentWord.setValue(getAndStopSaveCharacters());
            currentWord.setAnnotation("lemma", currentLemma);
            currentWord.setAnnotation("pos", currentPos);
		}
	}

	public void convert(String inpath, String outpath, int wnVersion)
	{
		this.wnVersion = wnVersion;
		out = new StreamingCorpusWriterDocument();
		out.open(outpath);
		loadSenses(inpath + "/keys/gold/wordnet/wordnet." + lang + ".key");
		loadCorpus(inpath + "/data/multilingual-all-words." + lang + ".xml");
		out.close();
	}

	public void convert(String inputCorpusPath, String inputKeyPath, String outputCorpusPath, int wnVersion)
	{
		this.wnVersion = wnVersion;
		out = new StreamingCorpusWriterDocument();
		out.open(outputCorpusPath);
		loadSenses(inputKeyPath);
		loadCorpus(inputCorpusPath);
		out.close();
	}

	public void loadCorpus(String path)
	{
		try
		{
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
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
                String[] tokens = line.split(RegExp.anyWhiteSpaceGrouped.pattern());
                String id = tokens[1];
                List<String> senses = new ArrayList<>();
                for (int i = 2 ; i < tokens.length ; i++)
                {
                    senses.add(tokens[i]);
                }
                String sense = StringUtils.join(senses, ";");
                sensesById.put(id, sense);
            }
            sc.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
