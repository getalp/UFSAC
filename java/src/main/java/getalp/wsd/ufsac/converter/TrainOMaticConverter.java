package getalp.wsd.ufsac.converter;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.SenseKeyUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class TrainOMaticConverter implements UFSACConverter
{        
    @Override
    public void convert(String inPath, String outPath, int wnVersion)
    {
        try
        {
            loadFile(inPath + "/evaluation-framework-ims-training.xml", outPath, wnVersion);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void loadFile(String inPath, String outPath, int wnVersion) throws Exception
    {
    	StreamingCorpusWriterSentence out = new StreamingCorpusWriterSentence();
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(new SAXBasicHandler()
        {
            private Sentence currentSentence;

            private String currentSenseKey;
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
                if (localName.equals("answer"))
                {
                    currentSenseKey = atts.getValue("sensekey");
                }
                else if (localName.equals("context"))
                {
                    currentSentence = new Sentence();
                    resetAndStartSaveCharacters();
                }
                else if (localName.equals("head"))
                {
                    List<String> wordsBefore = Arrays.asList(getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern()));
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
                if (localName.equals("context"))
                {
                    List<String> wordsAfter = Arrays.asList(getAndStopSaveCharacters().split(RegExp.anyWhiteSpaceGrouped.pattern()));
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
                    w.setValue(getAndStopSaveCharacters());
                    w.setAnnotation("lemma", SenseKeyUtils.extractLemmaFromSenseKey(currentSenseKey));
                    w.setAnnotation("pos", SenseKeyUtils.extractPOSFromSenseKey(currentSenseKey));
                    w.setAnnotation("wn" + wnVersion + "_key", currentSenseKey);
                    resetAndStartSaveCharacters();
                }
            }
        });

        out.open(outPath);
        saxReader.parse(inPath);
        out.close();
    }
}
