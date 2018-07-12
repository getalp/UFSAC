package getalp.wsd.ufsac.converter;

import org.xml.sax.*;

import getalp.wsd.common.utils.File;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.SenseKeyUtils;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Senseval3Task6Converter implements UFSACConverter
{
    private Map<String, List<String>> verbKeyToWordnetKey;

    private Map<String, List<String>> sensesById;
    
    private String trainOrTest = "";
    
    public Senseval3Task6Converter(String trainOrTest)
    {
        this.trainOrTest = trainOrTest;
    }
    
    @Override
    public void convert(String inpath, String outpath, int wnVersion)
    {
        try
        {
        	verbKeyToWordnetKey = new HashMap<>();
            sensesById = new HashMap<>();
            loadMappingForVerbs(inpath + "/" + trainOrTest + "/EnglishLS.dictionary.mapping.xml");
            loadKeys(inpath + "/" + trainOrTest + "/EnglishLS." + trainOrTest + ".key");
            loadCorpus(inpath + "/" + trainOrTest + "/EnglishLS." + trainOrTest, outpath, wnVersion);
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void loadMappingForVerbs(String path) throws Exception
    {       
    	BufferedReader reader = Files.newBufferedReader(Paths.get(path));
    	String tmpfilepath = File.createTemporaryFileName();
    	BufferedWriter writer = Files.newBufferedWriter(Paths.get(tmpfilepath));
    	String line;
    	while ((line = reader.readLine()) != null)
    	{
    		if (line.contains("<sense"))
    		{
    			line = line.substring(0, line.indexOf("synset=\""));
    			line += "/>";
    		}
    		writer.write(line + "\n");
    	}
    	writer.close();
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();
        parser.parse(
                new SequenceInputStream(
                    Collections.enumeration(Arrays.asList(
                    new InputStream[] {
                        new ByteArrayInputStream("<dummy>".getBytes()),
                        new FileInputStream(tmpfilepath),
                        new ByteArrayInputStream("</dummy>".getBytes()),
                    }))
                ), 
    	new SAXBasicHandler()
		{
        	private boolean inVerb = false;
        	
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
            	if (qName.equals("lexelt"))
            	{
            		if (atts.getValue("item").endsWith(".v"))
            		{
            			inVerb = true;
            		}
            		else
            		{
            			inVerb = false;
            		}
            	}
            	else if (qName.equals("sense"))
            	{
            		if (inVerb && atts.getValue("wn") != null)
            		{
            			verbKeyToWordnetKey.put(atts.getValue("id"), Arrays.asList(atts.getValue("wn").split(";")));
            		}
            	}
            }
		});
        File.removeFile(tmpfilepath);
    }

    public void loadKeys(String path) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] tokens = line.split(RegExp.anyWhiteSpaceGrouped.pattern());
            String tokenId = tokens[1];
            List<String> senses = new ArrayList<>();
        	for (int i = 2 ; i < tokens.length ; i++)
        	{
        		if (!tokens[i].equals("U"))
        		{
                    if (tokens[0].endsWith(".v"))
                    {
                    	if (verbKeyToWordnetKey.containsKey(tokens[i]))
                    	{
                    		senses.addAll(verbKeyToWordnetKey.get(tokens[i]));
                    	}
                    }
                    else
                    {
                    	senses.add(tokens[i]);
                    }
        		}
        	}
        	sensesById.put(tokenId, senses);
        }
        br.close();
    }

    private void loadCorpus(String inpath, String outpath, int wnVersion) throws Exception
    {
    	BufferedReader reader = Files.newBufferedReader(Paths.get(inpath));
    	String tmpfilepath = File.createTemporaryFileName();
    	BufferedWriter writer = Files.newBufferedWriter(Paths.get(tmpfilepath));
    	String line;
    	while ((line = reader.readLine()) != null)
    	{
    		if (line.contains("&frac12 ;"))
    		{
    			line = line.replaceAll("&frac12 ;", "1/2");
    		}
    		if (line.contains("&frac14 ;"))
    		{
    			line = line.replaceAll("&frac14 ;", "1/4");
    		}
    		if (line.contains("&frac34 ;"))
    		{
    			line = line.replaceAll("&frac34 ;", "3/4");
    		}
            if (line.contains("&deg/60 ;"))
            {
                line = line.replaceAll("&deg/60 ;", "deg");
            }
            if (line.contains("&Eacute ;"))
            {
                line = line.replaceAll("&Eacute ;", "E");
            }
            if (line.contains("&sup2 ;"))
            {
                line = line.replaceAll("&sup2 ;", "2");
            }
            if (line.contains("&THORN ;"))
            {
                line = line.replaceAll("&THORN ;", "p");
            }
    		writer.write(line + "\n");
    	}
    	writer.close();
    	
    	StreamingCorpusWriterSentence out = new StreamingCorpusWriterSentence();
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();

        out.open(outpath);
        
        parser.parse(
            new SequenceInputStream(
                Collections.enumeration(Arrays.asList(
                new InputStream[] {
                    new ByteArrayInputStream("<dummy>".getBytes()),
                    new FileInputStream(tmpfilepath),
                    new ByteArrayInputStream("</dummy>".getBytes()),
                }))
            ), 
        new SAXBasicHandler()
		{
        	private String currentTokenId = "";
        	
        	private Sentence currentSentence = new Sentence();
        	
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
                if (qName.equals("instance"))
                {
                	currentTokenId = atts.getValue("id");
                	currentSentence = new Sentence();
                	resetAndStartSaveCharacters();
                }
                else if (qName.equals("head"))
                {
                    String[] wordsBefore = getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern());
                    int indexOfLastDot = -1;
                    for (int i = wordsBefore.length - 1; i >= 0; i--)
                    {
                        if (wordsBefore[i].equals("."))
                        {
                            indexOfLastDot = i;
                            break;
                        }
                    }
                    for (int i = indexOfLastDot + 1; i < wordsBefore.length; i++)
                    {
                        Word w = new Word(currentSentence);
                        w.setValue(wordsBefore[i]);
                    }
                    resetAndStartSaveCharacters();
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException
            {
                if (qName.equals("instance"))
                {
                    String[] wordsAfter = getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern());
                    int indexOfFirstDot = wordsAfter.length;
                    for (int i = 0; i < wordsAfter.length; i++)
                    {
                        if (wordsAfter[i].equals("."))
                        {
                            indexOfFirstDot = i;
                            break;
                        }
                    }
                    for (int i = 0; i < indexOfFirstDot; i++)
                    {
                        Word w = new Word(currentSentence);
                        w.setValue(wordsAfter[i]);
                    }
                    Word w = new Word(currentSentence);
                    w.setValue(".");
                    out.writeSentence(currentSentence);
                } 
                else if (qName.equals("head"))
                {
                    Word w = new Word(currentSentence);
                    w.setValue(getAndStopSaveCharacters());
                    List<String> senseKeys = sensesById.get(currentTokenId);
                    if (senseKeys != null && !senseKeys.isEmpty())
                    {
	                    w.setAnnotation("lemma", SenseKeyUtils.extractLemmaFromSenseKey(senseKeys.get(0)));
	                    w.setAnnotation("pos", SenseKeyUtils.extractPOSFromSenseKey(senseKeys.get(0)));
	                    w.setAnnotation("wn" + wnVersion + "_key", StringUtils.join(senseKeys, ";"));
                    }
                    resetAndStartSaveCharacters();
                }
            }
		});
        out.close();
        File.removeFile(tmpfilepath);
    }
}