package getalp.wsd.ufsac.converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import getalp.wsd.common.utils.PercentProgressDisplayer;
import getalp.wsd.common.xml.SAXEntityResolverIgnoringDTD;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class OMSTIConverter extends DefaultHandler implements CorpusConverter
{
    private Sentence currentSentence;

    private boolean saveCharacters;

    private String currentCharacters;

    private String currentPos;

    private String currentLemma;
    
    private Map<String, String> currentIdToSenseKey;
    
    private String currentSenseKey;
    
    private boolean ignoreCurrentInstance;
    
    private StreamingCorpusWriterSentence out;
    
    private int wnVersion;

    @Override
    public void convert(String inPath, String outPath, int wnVersion)
    {
        this.wnVersion = wnVersion;
        out = new StreamingCorpusWriterSentence();
        out.open(outPath);
        try
        {
            loadPOS(inPath + "/noun", "n");
            loadPOS(inPath + "/verb", "v");
            loadPOS(inPath + "/adj", "a");
            loadPOS(inPath + "/adv", "r");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        out.close();
    }

    private void loadPOS(String path, String pos) throws Exception
    {
        currentPos = pos;
        Set<String> words = new HashSet<>();
        Stream<Path> paths = Files.list(Paths.get(path));
        paths.forEach(filePath -> 
        {
            words.add(filePath.toString().substring(0, filePath.toString().lastIndexOf(".")));
        });
        paths.close();
        List<String> wordsList = new ArrayList<>(words);
        Collections.sort(wordsList);
        PercentProgressDisplayer progress = new PercentProgressDisplayer(wordsList.size());
        for (int i = 0 ; i < wordsList.size() ; i++)
        {
        	progress.refresh("Info : OMSTI loading " + pos + "... ", i);
            loadKeys(wordsList.get(i) + ".key");
            loadFile(wordsList.get(i) + ".xml");
        }
        System.out.println();
    }
    
    private void loadKeys(String path) throws Exception
    {
        currentIdToSenseKey = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) 
        {
           String[] tokens = line.split("\\s+");
           currentIdToSenseKey.put(tokens[1], tokens[2].replaceAll("%5", "%3"));
        }
        br.close();
    }

    public void loadFile(String path) throws Exception
    {
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(this);
        saxReader.setEntityResolver(new SAXEntityResolverIgnoringDTD());
        saxReader.parse(path);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (localName.equals("instance"))
        {
            String currentDocSrc = atts.getValue("docsrc");
            if (currentDocSrc.contains("br-"))
            {
                ignoreCurrentInstance = true;
            }
            else
            {
                ignoreCurrentInstance = false;
                String currentId = atts.getValue("id");
                currentSenseKey = currentIdToSenseKey.get(currentId);
                currentLemma = currentSenseKey.substring(0, currentSenseKey.indexOf("%"));
                currentSentence = new Sentence();
                saveCharacters = true;
                currentCharacters = "";
            }
        }
        else if (localName.equals("head") && !ignoreCurrentInstance)
        {
            String[] wordsBefore = currentCharacters.split("\\s+");
            int indexOfLastDot = -1;
            for (int i = 0 ; i < wordsBefore.length ; i++)
            {
                if (wordsBefore[i].equals("."))
                {
                    indexOfLastDot = i;
                }
            }
            for (int i = indexOfLastDot + 1 ; i < wordsBefore.length ; i++)
            {
                if (wordsBefore[i].isEmpty()) continue;
                Word w = new Word(currentSentence);
                w.setValue(cleanWord(wordsBefore[i]));
            }
            currentCharacters = "";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (localName.equals("instance") && !ignoreCurrentInstance)
        {
            String[] wordsAfter = currentCharacters.split("\\s+");
            int indexOfFirstDot = wordsAfter.length;
            for (int i = wordsAfter.length - 1 ; i >= 0 ; i--)
            {
                if (wordsAfter[i].equals("."))
                {
                    indexOfFirstDot = i;
                }
            }
            for (int i = 0 ; i < indexOfFirstDot ; i++)
            {
                if (wordsAfter[i].isEmpty()) continue;
                Word w = new Word(currentSentence);
                w.setValue(cleanWord(wordsAfter[i]));
            }
            Word w = new Word(currentSentence);
            w.setValue(".");
            currentCharacters = "";
            saveCharacters = false;
            out.writeSentence(currentSentence);
        }
        else if (localName.equals("head") && !ignoreCurrentInstance)
        {
            Word w = new Word(currentSentence);
            w.setValue(cleanWord(currentCharacters));
            w.setAnnotation("lemma", currentLemma);
            w.setAnnotation("pos", currentPos);
            w.setAnnotation("wn" + wnVersion + "_key", currentSenseKey);
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
    
    public static String cleanWord(String wordSurfaceForm)
    {
        wordSurfaceForm = wordSurfaceForm.trim();
        if (wordSurfaceForm.equals("-LCB-"))
        {
            wordSurfaceForm = "{";
        } 
        else if (wordSurfaceForm.equals("-LRB-"))
        {
            wordSurfaceForm = "(";
        } 
        else if (wordSurfaceForm.equals("-LSB-"))
        {
            wordSurfaceForm = "[";
        } 
        else if (wordSurfaceForm.equals("-RCB-"))
        {
            wordSurfaceForm = "}";
        } 
        else if (wordSurfaceForm.equals("-RRB-"))
        {
            wordSurfaceForm = ")";
        } 
        else if (wordSurfaceForm.equals("-RSB-"))
        {
            wordSurfaceForm = "]";
        }
        return wordSurfaceForm;
    }

}
