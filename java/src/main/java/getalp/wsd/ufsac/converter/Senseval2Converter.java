package getalp.wsd.ufsac.converter;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

public class Senseval2Converter extends SAXBasicHandler implements CorpusConverter
{
    private StreamingCorpusWriterDocument out;
    
    private Document currentDocument;
    
    private Paragraph currentParagraph;
    
    private Sentence currentSentence;
    
    private Word currentWord;

    private int wnVersion;

    @Override
    public void convert(String inputPath, String outputPath, int wnVersion)
    {
        out = new StreamingCorpusWriterDocument();
        this.wnVersion = wnVersion;
        try
        {
            XMLReader saxReader = XMLReaderFactory.createXMLReader();
            saxReader.setContentHandler(this);
            out.open(outputPath);
            saxReader.parse(inputPath + "/d00.xml");
            saxReader.parse(inputPath + "/d01.xml");
            saxReader.parse(inputPath + "/d02.xml");
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (localName.equals("context"))
        {
            currentDocument = new Document();
            currentDocument.setAnnotation("id", atts.getValue("filename"));
            currentParagraph = new Paragraph(currentDocument);
        }
        else if (localName.equals("s"))
        {
            currentSentence = new Sentence(currentParagraph);
        }
        else if (localName.equals("wf"))
        {
            currentWord = new Word(currentSentence);
            String lemma = atts.getValue("lemma");
            String lexsn = atts.getValue("lexsn");
            String wnsn = atts.getValue("wnsn");
            if (lemma != null && lemma.equals("UNKNOWN")) lemma = null;
            if (wnsn != null && wnsn.equals("-1")) lexsn = null;
            if (lexsn != null && lexsn.equals("U")) lexsn = null;
            currentWord.setAnnotation("lemma", lemma);
            currentWord.setAnnotation("pos", atts.getValue("pos"));
            currentWord.setAnnotation("id", atts.getValue("id"));
            if (lemma != null && lexsn != null)
            {
                String[] senseKeys = lexsn.split(";");
                List<String> newSenseKey = new ArrayList<>();
                for (String senseKey : senseKeys)
                {
                    newSenseKey.add(lemma + "%" + senseKey);
                }
                currentWord.setAnnotation("wn" + wnVersion + "_key", StringUtils.join(newSenseKey, ";"));
            }
            resetAndStartSaveCharacters();
        }
        else if (localName.equals("punc"))
        {
            currentWord = new Word(currentSentence);
            resetAndStartSaveCharacters();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (localName.equals("context"))
        {
            out.writeDocument(currentDocument);
        }
        else if (localName.equals("wf"))
        {
            currentWord.setValue(getAndStopSaveCharacters());
        }
        else if (localName.equals("punc"))
        {
            currentWord.setValue(getAndStopSaveCharacters());
        }
    }
}
