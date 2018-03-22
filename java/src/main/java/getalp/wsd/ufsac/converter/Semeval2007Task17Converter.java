package getalp.wsd.ufsac.converter;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.xml.SAXEntityResolverIgnoringDTD;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Semeval2007Task17Converter extends DefaultHandler implements CorpusConverter
{
	private StreamingCorpusWriterDocument out;

	private Document currentDocument;

	private Paragraph currentParagraph;

	private Sentence currentSentence;
	
	private Word currentWord;

	private boolean saveCharacters;

	private String currentCharacters;

	private String currentWordId;

    private Map<String, String> sensesById;
    
    private int wordnetVersion;
    
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (localName.equals("text"))
		{
			currentDocument = new Document();
			currentDocument.setAnnotation("id", atts.getValue("id"));
			currentParagraph = new Paragraph(currentDocument);
			currentSentence = new Sentence();
            saveCharacters = true;
            currentCharacters = "";
		}
		else if (localName.equals("head"))
		{
	        addNonAnnotatedWords();
			saveCharacters = true;
			currentCharacters = "";
			currentWordId = atts.getValue("id");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
	    if (localName.equals("text"))
        {
            addNonAnnotatedWords();
	        out.writeDocument(currentDocument);
        }
        else if (localName.equals("head"))
		{
			currentWord = new Word(currentSentence);
			currentWord.setValue(currentCharacters);
			String senseKey = sensesById.get(currentWordId);
			if (senseKey != null && !senseKey.equals("U"))
			{
			    String lemma = senseKey.substring(0, senseKey.indexOf("%"));
			    String pos = POSConverter.toWNPOS(Integer.valueOf(senseKey.substring(senseKey.indexOf("%") + 1, senseKey.indexOf("%") + 2)));
			    currentWord.setAnnotation("lemma", lemma);
                currentWord.setAnnotation("pos", pos);
                currentWord.setAnnotation("wn" + wordnetVersion +"_key", senseKey);
			}
			currentWord.setAnnotation("id", currentWordId);
			saveCharacters = true;
			currentCharacters = "";
		}
	}
	
	private void addNonAnnotatedWords()
	{
	    String[] words = currentCharacters.split("\\s+");
	    for (String surfaceForm : words)
	    {
	        if (surfaceForm.isEmpty()) continue;
	        Word word = new Word(currentSentence);
	        word.setValue(surfaceForm);
            if (surfaceForm.equals("."))
            {
                currentSentence.setParentParagraph(currentParagraph);
                currentSentence = new Sentence(currentParagraph);
            }
	    }
	    saveCharacters = false;
	    currentCharacters = "";
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
	    this.wordnetVersion = wnVersion;
        out = new StreamingCorpusWriterDocument();
        out.open(outpath);
	    loadSenses(inpath + "/key/english-all-words.test.key");
	    loadCorpus(inpath + "/test/all-words/english-all-words.test.xml");
	    out.close();
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
                String id = tokens[1].substring(tokens[1].indexOf("\"") + 1, tokens[1].lastIndexOf("\""));
                String sense = tokens[2].substring(tokens[2].indexOf("\"") + 1, tokens[2].lastIndexOf("\""));
                sensesById.put(id, sense);
            }
            sc.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

	private void loadCorpus(String path)
	{
        try
        {
            XMLReader saxReader = XMLReaderFactory.createXMLReader();
            saxReader.setContentHandler(this);
            saxReader.setEntityResolver(new SAXEntityResolverIgnoringDTD());
            saxReader.parse(path);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
	}
}
