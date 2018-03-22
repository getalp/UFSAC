package getalp.wsd.ufsac.converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.utils.PercentProgressDisplayer;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class OntonotesConverter implements CorpusConverter
{    
    private WordnetHelper wn;
    
    private String wnSenseTag;
    
    private Map<String, List<String>> senseMapWN;
    
    @Override
    public void convert(String inputPath, String outputPath, int wnVersion)
    {
        try
        {
            convertWithExceptions(inputPath, outputPath, wnVersion);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void convertWithExceptions(String inputPath, String ouputPath, int wnVersion) throws Exception
    {
        this.wn = WordnetHelper.wn(wnVersion);
        this.wnSenseTag = "wn" + wnVersion + "_key";
        this.senseMapWN = new HashMap<>();
        
        String pathSenseMapping = inputPath + "/metadata/sense-inventories/";
        loadSenseMappings(pathSenseMapping);
        
        String pathAnnotations = inputPath + "/annotations";
        loadFiles(ouputPath, pathAnnotations);
    }

    private void loadFiles(String output, String pathAnnotations) throws Exception
    {
        List<String> wordList = Files.walk(Paths.get(pathAnnotations)).filter(p -> p.toString().endsWith(".onf")).map(p -> p.toString()).collect(Collectors.toList());
        Collections.sort(wordList);
        StreamingCorpusWriterSentence out = new StreamingCorpusWriterSentence();
        out.open(output);
        PercentProgressDisplayer progress = new PercentProgressDisplayer(wordList.size());
        for (int i = 0; i < wordList.size(); i++)
        {
            progress.refresh("Info : Ontonotes loading... ", i);
            loadFile(wordList.get(i), out);
        }
        System.out.println();
        out.close();
    }
    
    private void loadFile(String path, StreamingCorpusWriterSentence out) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        boolean inSentence = false;
        int currentSentenceIndex = 0;
        Sentence currentSentence = null;
        Word currentWord = null;
        while ((line = br.readLine()) != null)
        {
            String trimmedLine = line.trim();
            if (!inSentence && trimmedLine.startsWith("Leaves:"))
            {
                line = br.readLine();
                inSentence = true;
                currentSentenceIndex = 0;
                currentSentence = new Sentence();
            }
            else if (inSentence && (trimmedLine.startsWith("Plain sentence:") || trimmedLine.startsWith("Coreference chains")))
            {
                out.writeSentence(currentSentence);
                inSentence = false;
            }
            else if (inSentence && trimmedLine.startsWith("" + currentSentenceIndex))
            {
                String[] tokens = trimmedLine.split(RegExp.anyWhiteSpaceGrouped.pattern());
                if (tokens.length >= 2 && tokens[0].equals("" + currentSentenceIndex))
                {
                    String wordValue = cleanWord(tokens[1]);
                    currentWord = new Word(wordValue, currentSentence);
                    currentSentenceIndex++;
                }
            }
            else if (inSentence && trimmedLine.startsWith("sense:"))
            {
                String[] tokens = trimmedLine.split(RegExp.anyWhiteSpaceGrouped.pattern());
                List<String> senseKeys = senseMapWN.get(tokens[1]);
                currentWord.setAnnotation(wnSenseTag, StringUtils.join(senseKeys, ";"));
            }
        }        
        br.close();
    }
    
    private static String cleanWord(String wordSurfaceForm)
    {
        if (wordSurfaceForm.contains("*"))
        {
            return "";
        }
        if (wordSurfaceForm.equals("/."))
        {
            return ".";
        }
        if (wordSurfaceForm.equals("-LCB-"))
        {
            return "{";
        } 
        if (wordSurfaceForm.equals("-LRB-"))
        {
            return "(";
        } 
        if (wordSurfaceForm.equals("-LSB-"))
        {
            return "[";
        } 
        if (wordSurfaceForm.equals("-RCB-"))
        {
            return "}";
        } 
        if (wordSurfaceForm.equals("-RRB-"))
        {
            return ")";
        } 
        if (wordSurfaceForm.equals("-RSB-"))
        {
            return "]";
        }
        return wordSurfaceForm;
    }
    
    private void loadSenseMappings(String senseMappingFolderPath) throws Exception
    {
        List<String> fileList = new ArrayList<>();
        Files.list(Paths.get(senseMappingFolderPath)).forEach(filePath ->
        {
            if (filePath.toString().endsWith(".xml") && 
                !filePath.toString().endsWith("noun_grouping_template.xml") && 
                !filePath.toString().endsWith("fracture-v.xml"))
            {
                fileList.add(filePath.toString());
            }
        });
        Collections.sort(fileList);
        for (String file : fileList)
        {
            loadSenseMapping(file);
        }
    }

    private class SenseMappingXMLHandler extends DefaultHandler
    {        
        public String onWordKey;
        
        public String wnWordKey;
        
        public boolean saveCharacters;
        
        public String currentCharacters;
        
        public String currentONSenseKey;
                
        public boolean insideWNSense;
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
            if (localName.equals("sense"))
            {
                currentONSenseKey = onWordKey + "." + atts.getValue("n");
                senseMapWN.put(currentONSenseKey, new ArrayList<>());
            }
            else if (localName.equals("wn"))
            {
                if (atts.getValue("lemma") == null && atts.getValue("version").equals("3.0"))
                {
                    insideWNSense = true;
                    saveCharacters = true;
                    currentCharacters = "";
                }
                else
                {
                    insideWNSense = false;
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (localName.equals("wn") && insideWNSense)
            {
                String[] WNSenses = currentCharacters.trim().split(",");
                for (String wnsense : WNSenses)
                {
                    if (wnsense.isEmpty()) continue;
                    String wnSenseNumber = wnWordKey + "#" + wnsense;
                    String wnSenseKey = wn.getSenseKeyFromSenseNumber(wnSenseNumber);
                    senseMapWN.get(currentONSenseKey).add(wnSenseKey);
                }
                saveCharacters = false;
                insideWNSense = false;
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
    }
        
    private void loadSenseMapping(String filePath) throws Exception
    {
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        SenseMappingXMLHandler handler = new SenseMappingXMLHandler();
        handler.onWordKey = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".xml"));
        handler.wnWordKey = handler.onWordKey.replace("-", "%");
        saxReader.setContentHandler(handler);
        saxReader.parse(filePath);
    }

}
