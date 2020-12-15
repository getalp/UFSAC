package getalp.wsd.ufsac.converter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.common.xml.SAXEntityResolverIgnoringDTD;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MASCConverter extends DefaultHandler implements UFSACConverter
{
	private StreamingCorpusWriterSentence out;

    private Map<String, String> NOADSenseKeyToWNSenseKey;
    
    private Sentence currentSentence;

    private WordnetHelper wn;
    
    @Override
    public void convert(String inputPath, String outputPath, int wnVersion)
    {
    	wn = WordnetHelper.wn(wnVersion);
        out = new StreamingCorpusWriterSentence();
        out.open(outputPath);
        try
        {
            NOADSenseKeyToWNSenseKey = new HashMap<>();
            loadMappingFile(inputPath + "/algorithmic_map.txt");
            loadMappingFile(inputPath + "/manual_map.txt");
            purgeMapping();
            loadCorpusDirectory(inputPath);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        out.close();
    }

    private void loadMappingFile(String path) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] linesplit = line.trim().split("\\s+");
            String noadSense = linesplit[0];
            String wnSense = linesplit[1].replaceAll(",", ";").toLowerCase();
            NOADSenseKeyToWNSenseKey.put(noadSense, wnSense);
        }
        br.close();
    }
    
    private void purgeMapping()
    {
        Set<String> keysToDelete = new HashSet<>();
        for (Map.Entry<String, String> entry : NOADSenseKeyToWNSenseKey.entrySet())
        {
            entry.setValue(entry.getValue().replaceAll("%5", "%3"));
            String[] valuez = entry.getValue().split(";");
            for (String v : valuez)
            {
                if (!wn.isSenseKeyExists(v))
                {
                    keysToDelete.add(entry.getKey());
                }
            }
        }
        for (String key : keysToDelete)
        {
            NOADSenseKeyToWNSenseKey.remove(key);
        }
    }

    private void loadCorpusDirectory(String inputPath) throws Exception
    {
        List<String> filePathList = getFilePathList(inputPath);
        for (String filePath : filePathList)
        {
            loadCorpusFile(filePath);
        }
    }
    
    private List<String> getFilePathList(String inputPath) throws Exception
    {
        List<String> filePathList = new ArrayList<>();
        Stream<Path> paths = Files.find(Paths.get(inputPath), Integer.MAX_VALUE, new BiPredicate<Path,BasicFileAttributes>()
        {
            @Override
            public boolean test(Path arg0, BasicFileAttributes arg1)
            {
                if (arg0.toString().endsWith(".xml")) return true;
                return false;
            }
        });
        paths.forEach(filePath ->
        {
            filePathList.add(filePath.toString());
        });
        paths.close();
        Collections.sort(filePathList);
        return filePathList;
    }
    
    private void loadCorpusFile(String inputPath) throws Exception
    {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader saxReader = parser.getXMLReader();
        saxReader.setContentHandler(this);
        saxReader.setEntityResolver(new SAXEntityResolverIgnoringDTD());
        currentSentence = new Sentence();
        saxReader.parse(inputPath);
        out.writeSentence(currentSentence);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (localName.equals("word"))
        {
            String text = atts.getValue("text");
            String lemma = atts.getValue("lemma");
            String pos = atts.getValue("pos");
            String sense = atts.getValue("sense");
            String breakLevel = atts.getValue("break_level");
            if (breakLevel.equals("PARAGRAPH_BREAK") || breakLevel.equals("SENTENCE_BREAK"))
            {
                out.writeSentence(currentSentence);
                currentSentence = new Sentence();
            }
            Word word = new Word(currentSentence);
            word.setValue(text);
            if (lemma != null)
            {
                word.setAnnotation("lemma", lemma);
            }
            if (pos != null)
            {
                word.setAnnotation("pos", pos);
            }
            if (sense != null)
            {
                word.setAnnotation("wn" + wn.getVersion() + "_key", NOADSenseKeyToWNSenseKey.get(sense));
            }
        }
    }
    
}

