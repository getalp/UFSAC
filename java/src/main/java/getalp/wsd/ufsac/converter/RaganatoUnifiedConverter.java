package getalp.wsd.ufsac.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.xml.SAXBasicHandler;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterDocument;

public class RaganatoUnifiedConverter implements UFSACConverter
{
    public void convert(String inpath, String outpath, int wnVersion)
    {
        try
        {
            Map<String, String> sensesById = loadSenses(inpath + ".gold.key.txt");
            loadCorpus(inpath + ".data.xml", outpath, wnVersion, sensesById);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> loadSenses(String path) throws Exception
    {
        Map<String, String> sensesById = new HashMap<>();
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine())
        {
            String line = sc.nextLine();
            String[] tokens = line.split(RegExp.anyWhiteSpaceGrouped.pattern());
            String id = tokens[0];
            List<String> senses = new ArrayList<>();
            for (int i = 1 ; i < tokens.length ; i++)
            {
                senses.add(tokens[i].replaceAll("%5", "%3"));
            }
            String sense = StringUtils.join(senses, ";");
            sensesById.put(id, sense);
        }
        sc.close();
        return sensesById;
    }

    public void loadCorpus(String inpath, String outpath, int wnVersion, Map<String, String> sensesById) throws Exception
    {
        StreamingCorpusWriterDocument out = new StreamingCorpusWriterDocument();
        XMLReader saxReader = XMLReaderFactory.createXMLReader();
        saxReader.setContentHandler(new SAXBasicHandler()
        {
            private Document currentDocument;

            private Paragraph currentParagraph;

            private Sentence currentSentence;

            private Word currentWord;

            private String currentPos;

            private String currentLemma;

            private String currentWordId;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
            {
                if (localName.equals("text"))
                {
                    currentDocument = new Document();
                    currentDocument.setAnnotation("id", atts.getValue("id"));
                    currentParagraph = new Paragraph(currentDocument);
                }
                else if (localName.equals("sentence"))
                {
                    currentSentence = new Sentence(currentParagraph);
                    currentSentence.setAnnotation("id", atts.getValue("id"));
                }
                else if (localName.equals("instance"))
                {
                    currentPos = atts.getValue("pos");
                    currentLemma = atts.getValue("lemma").toLowerCase();
                    currentWordId = atts.getValue("id");
                    resetAndStartSaveCharacters();
                }
                else if (localName.equals("wf"))
                {
                    currentPos = atts.getValue("pos");
                    currentLemma = atts.getValue("lemma").toLowerCase();
                    resetAndStartSaveCharacters();
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException
            {
                if (localName.equals("text"))
                {
                    out.writeDocument(currentDocument);
                }
                else if (localName.equals("instance"))
                {
                    currentWord = new Word(currentSentence);
                    currentWord.setValue(getAndStopSaveCharacters());
                    currentWord.setAnnotation("lemma", currentLemma);
                    currentWord.setAnnotation("pos", currentPos);
                    currentWord.setAnnotation("id", currentWordId);
                    currentWord.setAnnotation("wn" + wnVersion + "_key", sensesById.get(currentWordId));
                }
                else if (localName.equals("wf"))
                {
                    currentWord = new Word(currentSentence);
                    currentWord.setValue(getAndStopSaveCharacters());
                    currentWord.setAnnotation("lemma", currentLemma);
                    currentWord.setAnnotation("pos", currentPos);
                }
            }
        });
        out.open(outpath);
        saxReader.parse(inpath);
        out.close();
    }
}
