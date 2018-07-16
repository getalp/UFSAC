package getalp.wsd.ufsac.converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import getalp.wsd.common.utils.PercentProgressDisplayer;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.SenseKeyUtils;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.utils.Utils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.common.xml.SAXEntityResolverIgnoringDTD;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class OMSTIConverter implements UFSACConverter
{
    private Map<String, List<String>> senseKeysById;
    
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
        senseKeysById = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) 
        {
           String[] tokens = line.split(RegExp.anyWhiteSpaceGrouped.pattern());
           List<String> senseKeys = new ArrayList<>();
           for (int i = 2 ; i < tokens.length ; i++)
           {
               senseKeys.add(tokens[i].replaceAll("%5", "%3"));
           }
           senseKeysById.put(tokens[1], senseKeys);
        }
        br.close();
    }

    public void loadFile(String path) throws Exception
    {
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(new SAXBasicHandler()
        {
            private Sentence currentSentence;

            private String currentSenseKey;
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
                if (localName.equals("instance"))
                {
                    currentSenseKey = StringUtils.join(senseKeysById.get(atts.getValue("id")), ";");
                    currentSentence = new Sentence();
                    resetAndStartSaveCharacters();
                }
                else if (localName.equals("head"))
                {
                    List<String> wordsBefore = Arrays.asList(getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern()));
                    wordsBefore = Utils.subListStartingAfterLastEndOfSentenceMarker(wordsBefore);
                    for (String wordBefore : wordsBefore)
                    {
                        Word w = new Word(currentSentence);
                        w.setValue(wordBefore);
                    }
                    resetAndStartSaveCharacters();
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException
            {
                if (localName.equals("instance"))
                {
                    List<String> wordsAfter = Arrays.asList(getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern()));
                    wordsAfter = Utils.subListEndingOnFirstEndOfSentenceMarker(wordsAfter);
                    for (String wordAfter : wordsAfter)
                    {
                        Word w = new Word(currentSentence);
                        w.setValue(wordAfter);
                    }
                    out.writeSentence(currentSentence);
                }
                else if (localName.equals("head"))
                {
                    Word w = new Word(currentSentence);
                    w.setValue(getAndStopSaveCharacters().trim());
                    w.setAnnotation("lemma", SenseKeyUtils.extractLemmaFromSenseKey(currentSenseKey));
                    w.setAnnotation("pos", SenseKeyUtils.extractPOSFromSenseKey(currentSenseKey));
                    w.setAnnotation("wn" + wnVersion + "_key", currentSenseKey);
                    resetAndStartSaveCharacters();
                }
            }
        });
        saxReader.setEntityResolver(new SAXEntityResolverIgnoringDTD());
        saxReader.parse(path);
    }
}
