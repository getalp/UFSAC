package getalp.wsd.ufsac.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

import org.xml.sax.helpers.DefaultHandler;

public class Semeval2013Task12Converter extends DefaultHandler implements UFSACConverter
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
		else if (localName.equals("instance"))
		{
			saveCharacters = true;
			currentCharacters = "";
			currentPos = atts.getValue("pos");
			currentLemma = atts.getValue("lemma").toLowerCase();
			currentWordId = atts.getValue("id");
		}
		else if (localName.equals("wf"))
		{
		    saveCharacters = true;
		    currentCharacters = "";
		    currentPos = atts.getValue("pos");
		    currentLemma = atts.getValue("lemma").toLowerCase();
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
			currentWord.setValue(currentCharacters);
			currentWord.setAnnotation("lemma", currentLemma);
			currentWord.setAnnotation("pos", currentPos);
			currentWord.setAnnotation("id", currentWordId);
            currentWord.setAnnotation("wn" + wnVersion + "_key", sensesById.get(currentWordId));
			saveCharacters = false;	
		}
		else if (localName.equals("wf"))
		{
            currentWord = new Word(currentSentence);
            currentWord.setValue(currentCharacters);
            currentWord.setAnnotation("lemma", currentLemma);
            currentWord.setAnnotation("pos", currentPos);
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

    public void convert(String inpath, String outpath, int wnVersion)
    {
        this.wnVersion = wnVersion;
        out = new StreamingCorpusWriterDocument();
        out.open(outpath);
        loadSenses(inpath + "/keys/gold/wordnet/wordnet.en.key");
        loadCorpus(inpath + "/data/multilingual-all-words.en.xml");
        out.close();
    }

	public void loadCorpus(String path)
	{
		try
		{
			XMLReader saxReader = XMLReaderFactory.createXMLReader();
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
