package getalp.wsd.ufsac.converter;

import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RaganatoUnifiedConverterInverse implements UFSACConverter
{
    public void convert(String inpath, String outpath, int wnVersion)
    {
        try
        {
            Map<String, String> sensesById = new LinkedHashMap<>();
            loadCorpus(inpath, outpath + ".data.xml", wnVersion, sensesById);
            saveSenses(outpath + ".gold.key.txt", sensesById);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void loadCorpus(String inputPpath, String outputPath, int wnVersion, Map<String, String> sensesById) throws Exception
    {
        String wnTag = "wn" + wnVersion + "_key";
        BufferedWriter out = Files.newBufferedWriter(Paths.get(outputPath));
        StreamingCorpusReader reader = new StreamingCorpusReader()
        {
            public void readBeginCorpus(Corpus corpus)
            {
                try
                {
                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                    out.write("<corpus>\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readBeginDocument(Document document)
            {
                try
                {
                    String idString = "";
                    if (document.hasAnnotation("id"))
                    {
                        idString = " id=\"" + document.getAnnotationValue("id") + "\"";
                    }
                    out.write("<text" + idString + ">\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readBeginParagraph(Paragraph paragraph)
            {

            }

            public void readBeginSentence(Sentence sentence)
            {
                try
                {
                    String idString = "";
                    if (sentence.hasAnnotation("id"))
                    {
                        idString = " id=\"" + sentence.getAnnotationValue("id") + "\"";
                    }
                    out.write("<sentence" + idString + ">\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readWord(Word word)
            {
                try
                {
                    String wordTag = "wf";
                    if (word.hasAnnotation(wnTag))
                    {
                        wordTag = "instance";
                    }
                    String idTag = "";
                    if (word.hasAnnotation("id"))
                    {
                        idTag = " id=\"" + word.getAnnotationValue("id") + "\"";
                    }
                    String lemmaTag = "";
                    if (word.hasAnnotation("lemma"))
                    {
                        lemmaTag = "lemma=\"" + word.getAnnotationValue("lemma") + "\"";
                    }
                    String posTag = "";
                    if (word.hasAnnotation("pos"))
                    {
                        posTag = "pos=\"" + word.getAnnotationValue("pos") + "\"";
                    }
                    out.write("<" + wordTag + "" + idTag + " " + lemmaTag + " " + posTag + ">");
                    out.write(word.getValue());
                    out.write("</" + wordTag + ">\n");
                    if (word.hasAnnotation(wnTag) && word.hasAnnotation("id"))
                    {
                        sensesById.put(word.getAnnotationValue("id"), word.getAnnotationValue(wnTag));
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readEndSentence()
            {
                try
                {
                    out.write("</sentence>\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readEndParagraph()
            {

            }

            public void readEndDocument()
            {
                try
                {
                    out.write("</text>\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void readEndCorpus()
            {
                try
                {
                    out.write("</corpus>\n");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        reader.load(inputPpath);
        out.close();
    }

    private void saveSenses(String path, Map<String, String> sensesById) throws Exception
    {
        BufferedWriter out = Files.newBufferedWriter(Paths.get(path));
        for (String id : sensesById.keySet())
        {
            String[] senseKeys = sensesById.get(id).split(";");
            out.write(id);
            for (String senseKey : senseKeys)
            {
                out.write(" " + senseKey);
            }
            out.newLine();
        }
        out.close();
    }
}
