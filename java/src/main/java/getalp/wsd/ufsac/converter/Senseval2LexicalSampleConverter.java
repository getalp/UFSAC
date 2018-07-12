package getalp.wsd.ufsac.converter;

import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.SenseKeyUtils;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Senseval2LexicalSampleConverter implements UFSACConverter
{
    private Map<String, List<String>> senseKeysById;
    
    private String trainOrTest = "";
    
    private TokenizerFactory<edu.stanford.nlp.ling.Word> tokenizerFactory;
    
    public Senseval2LexicalSampleConverter(String trainOrTest)
    {
        this.trainOrTest = trainOrTest;
        this.tokenizerFactory = PTBTokenizerFactory.newTokenizerFactory();
    }
    
    @Override
    public void convert(String inpath, String outpath, int wnVersion)
    {
        try
        {
            senseKeysById = new HashMap<>();
            if (trainOrTest.equals("train"))
            {
                loadKeys(inpath + "/train/eng-lex-sample.training.key");
                loadCorpus(inpath + "/train/eng-lex-sample.training.xml", outpath, wnVersion);
            }
            else
            {
                loadKeys(inpath + "/test/eng-lex-sample.evaluation.key");
                loadCorpus(inpath + "/test/eng-lex-sample.evaluation.xml", outpath, wnVersion);
            }
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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
        		if (!tokens[i].equals("U") && !tokens[i].equals("P"))
        		{
                    senses.add(tokens[i]);
        		}
        	}
        	senseKeysById.put(tokenId, senses);
        }
        br.close();
    }

    private void loadCorpus(String inpath, String outpath, int wnVersion) throws Exception
    {
    	StreamingCorpusWriterSentence out = new StreamingCorpusWriterSentence();
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(new SAXBasicHandler()
		{
        	private String currentTokenId;
        	
        	private Sentence currentSentence;
        	
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
                if (localName.equals("instance"))
                {
                	currentTokenId = atts.getValue("id");
                	currentSentence = new Sentence();
                	resetAndStartSaveCharacters();
                }
                else if (localName.equals("head"))
                {
                    List<String> wordsBefore = tokenize(clean(getAndStopSaveCharacters()));
                    wordsBefore = wordsBefore.subList(wordsBefore.lastIndexOf(".") + 1, wordsBefore.size());
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
                    List<String> wordsAfter = tokenize(clean(getAndStopSaveCharacters()));
                    int indexOfFirstDot = wordsAfter.indexOf(".");
                    if (indexOfFirstDot == -1) indexOfFirstDot = wordsAfter.size();
                    wordsAfter = wordsAfter.subList(0, indexOfFirstDot);
                    for (String wordAfter : wordsAfter)
                    {
                        Word w = new Word(currentSentence);
                        w.setValue(wordAfter);
                    }
                    Word w = new Word(currentSentence);
                    w.setValue(".");
                    out.writeSentence(currentSentence);
                } 
                else if (localName.equals("head"))
                {
                    Word w = new Word(currentSentence);
                    w.setValue(getAndStopSaveCharacters());
                    List<String> senseKeys = senseKeysById.get(currentTokenId);
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
        out.open(outpath);
        saxReader.parse(inpath);
        out.close();
    }
    
    private List<String> tokenize(String fullString)
    {
        List<String> tokenized = new ArrayList<>();
        
        Tokenizer<edu.stanford.nlp.ling.Word> tokenizer = tokenizerFactory.getTokenizer(new StringReader(fullString));
        while (tokenizer.hasNext())
        {
            tokenized.add(tokenizer.next().word());
        }
        
        return tokenized;
    }
    
    private String clean(String string)
    {
        String cleaned = string;
        cleaned = cleaned.replaceAll("\\[.*?\\]", "");
        cleaned = StringEscapeUtils.unescapeXml(cleaned);
        return cleaned;
    }
}