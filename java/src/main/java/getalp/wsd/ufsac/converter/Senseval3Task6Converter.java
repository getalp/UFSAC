package getalp.wsd.ufsac.converter;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import getalp.wsd.ufsac.core.Corpus;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import org.xml.sax.helpers.DefaultHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Senseval3Task6Converter extends DefaultHandler implements CorpusConverter
{
    private Corpus currentCorpus;

    private Document currentDocument;

    private Paragraph currentParagraph;

    private Sentence currentSentence;

    private boolean saveCharacters;

    private String currentCharacters;

    private String currentPos;

    private String currentLemma;

    private String currentContextKey;

    private Map<String, String> currentIdToContextKey;

    private Map<String, String> sensesById;
    
    //private int wnVersion;

    @Override
    public void convert(String inpath, String outpath, int wnVersion)
    {
        try
        {
            //this.wnVersion = wnVersion;
            sensesById = new HashMap<>();
            loadKeys(inpath + "/test/EnglishLS.test.key");
            loadMappingForVerbs(inpath + "/test/EnglishLS.dictionary.mapping.xml");
            loadCorpus(inpath + "/test/EnglishLS.test");
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (localName.equals("instance"))
        {
            String currentId = atts.getValue("id");
            currentContextKey = currentIdToContextKey.get(currentId);
            currentLemma = currentContextKey.substring(0, currentContextKey.indexOf("."));
            currentSentence = new Sentence(currentParagraph);
            saveCharacters = true;
            currentCharacters = "";
        } 
        else if (localName.equals("head"))
        {
            String[] wordsBefore = currentCharacters.split("\\s+");
            int indexOfLastDot = -1;
            for (int i = 0; i < wordsBefore.length; i++)
            {
                if (wordsBefore[i].equals("."))
                {
                    indexOfLastDot = i;
                }
            }
            for (int i = indexOfLastDot + 1; i < wordsBefore.length; i++)
            {
                if (!wordsBefore[i].isEmpty())
                {
                    Word w = new Word(currentSentence);
                    w.setValue(wordsBefore[i]);
                }
            }
            currentCharacters = "";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (localName.equals("instance"))
        {
            String[] wordsAfter = currentCharacters.split("\\s+");
            int indexOfFirstDot = wordsAfter.length;
            for (int i = wordsAfter.length - 1; i >= 0; i--)
            {
                if (wordsAfter[i].equals("."))
                {
                    indexOfFirstDot = i;
                }
            }
            for (int i = 0; i < indexOfFirstDot; i++)
            {
                if (!wordsAfter[i].isEmpty())
                {
                    Word w = new Word(currentSentence);
                    w.setValue(wordsAfter[i]);
                }
                Word w = new Word(currentSentence);
                w.setValue(".");
                currentCharacters = "";
                saveCharacters = false;

            }
        } 
        else if (localName.equals("head"))
        {
            Word w = new Word(currentSentence);
            w.setValue(currentCharacters.trim());
            w.setAnnotation("lemma", currentLemma);
            w.setAnnotation("pos", currentPos);
            currentCharacters = "";
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

    public void loadKeys(String path) throws Exception
    {
        currentIdToContextKey = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] tokens = line.split("\\s+");
            if (tokens[0].contains(".v"))
            {
                currentIdToContextKey.put(tokens[2], tokens[1]);
            } 
            else if (tokens[0].contains(".n") || tokens[0].contains(".a"))
            { 
                String currentSense = tokens[1];
                sensesById.put(tokens[2], currentSense); 
            }
        }
        br.close();
    }

    public void loadMappingForVerbs(String path) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null)
        {
            if (line.contains("sense"))
            {
                String[] tokens = line.split("\\s+");
                if (tokens[3].contains("wn"))
                {
                    String[] currentSenses = tokens[3].split("\"");
                    String currentSense = currentSenses[1];

                    String[] currentIds = tokens[1].split("\"");
                    String currentId = currentIds[1];
                    
                    sensesById.put(currentIdToContextKey.get(currentId), currentSense);
                }
            }

        }
        br.close();
    }

    private Corpus loadCorpus(String path) throws Exception
    {
        currentCorpus = new Corpus();
        currentDocument = new Document(currentCorpus);
        currentParagraph = new Paragraph(currentDocument);
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(this);
        saxReader.parse(path);
        return currentCorpus;
    }

}