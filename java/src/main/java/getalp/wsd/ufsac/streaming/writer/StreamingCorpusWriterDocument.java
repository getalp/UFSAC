package getalp.wsd.ufsac.streaming.writer;

import getalp.wsd.ufsac.core.*;

public class StreamingCorpusWriterDocument
{
    public StreamingCorpusWriterDocument()
    {

    }
    
    public void open(String path)
    {
        out.open(path);
        out.writeBeginCorpus();
    }
    
    public void writeDocument(Document document)
    {
        out.writeBeginDocument(document);
        for (Paragraph paragraph : document.getParagraphs())
        {
            out.writeBeginParagraph(paragraph);
            for (Sentence sentence : paragraph.getSentences())
            {
                out.writeBeginSentence(sentence);
                for (Word word : sentence.getWords())
                {
                    out.writeWord(word);
                }
                out.writeEndSentence();
            }
            out.writeEndParagraph();
        }
        out.writeEndDocument();
    }
    
    public void close()
    {
        out.writeEndCorpus();
        out.close();
    }
    
    private StreamingCorpusWriter out = new StreamingCorpusWriter();
}
