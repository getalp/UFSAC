package getalp.wsd.ufsac.streaming.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import getalp.wsd.common.xml.XMLHelper;
import getalp.wsd.ufsac.core.*;

public class StreamingCorpusWriter
{
    private BufferedWriter out;

    public StreamingCorpusWriter()
    {

    }

    public void writeBeginCorpus()
    {
        writeBeginCorpus(new Corpus());
    }

    public void writeBeginCorpus(Corpus corpus)
    {
        writeBeginEntity(0, "corpus", corpus.getAnnotations());
    }
    
    public void writeBeginDocument()
    {
        writeBeginDocument(new Document());
    }

    public void writeBeginDocument(Document document)
    {
        writeBeginEntity(1, "document", document.getAnnotations());
    }
    
    public void writeBeginParagraph()
    {
        writeBeginParagraph(new Paragraph());
    }

    public void writeBeginParagraph(Paragraph paragraph)
    {
        writeBeginEntity(2, "paragraph", paragraph.getAnnotations());
    }
    
    public void writeBeginSentence()
    {
        writeBeginSentence(new Sentence());
    }

    public void writeBeginSentence(Sentence sentence)
    {
        writeBeginEntity(3, "sentence", sentence.getAnnotations());
    }
    
    public void writeWord()
    {
        writeWord(new Word());
    }

    public void writeWord(Word word)
    {
        writeInlineEntity(4, "word", word.getAnnotations());
    }

    public void writeEndSentence()
    {
        writeEndEntity(3, "sentence");
    }

    public void writeEndParagraph()
    {
        writeEndEntity(2, "paragraph");
    }

    public void writeEndDocument()
    {
        writeEndEntity(1, "document");
    }

    public void writeEndCorpus()
    {
        writeEndEntity(0, "corpus");
    }

    private void write(String str)
    {
        try
        {
            out.write(str);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void open(String path)
    {
        try
        {
            out = new BufferedWriter(new FileWriter(path));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void flush()
    {
        try
        {
            out.flush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void close()
    {
        try
        {
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void writeInlineEntity(int indentLevel, String entityName, List<Annotation> annotations)
    {
        write(XMLHelper.getIndent(indentLevel));
        write("<" + entityName);
        writeAnnotations(annotations);
        write("/>\n");
        flush();
    }

    private void writeBeginEntity(int indentLevel, String entityName, List<Annotation> annotations)
    {
        write(XMLHelper.getIndent(indentLevel));
        write("<" + entityName);
        writeAnnotations(annotations);
        write(">\n");
        flush();
    }

    private void writeEndEntity(int indentLevel, String entityName)
    {
        write(XMLHelper.getIndent(indentLevel));
        write("</" + entityName + ">\n");
        flush();
    }
    
    private void writeAnnotations(List<Annotation> annotations)
    {
        if (annotations.isEmpty()) return;
        for (Annotation annotation : annotations)
        {
            if (annotation.getAnnotationValue().isEmpty()) continue;
            write(" " + XMLHelper.toValidXMLEntity(annotation.getAnnotationName()) + "=\"" + 
                        XMLHelper.toValidXMLEntity(annotation.getAnnotationValue()) + "\"");
        }
        write(" ");
    }
}
