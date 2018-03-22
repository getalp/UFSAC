package getalp.wsd.ufsac.streaming.writer;

import getalp.wsd.ufsac.core.*;

public class StreamingCorpusWriterParagraph
{
    public StreamingCorpusWriterParagraph()
    {

    }
    
    public void open(String path)
    {
        out.open(path);
        out.writeBeginCorpus();
        out.writeBeginDocument();
    }
    
    public void writeParagraph(Paragraph paragraph)
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
    
    public void close()
    {
        out.writeEndDocument();
        out.writeEndCorpus();
        out.close();
    }
    
    private StreamingCorpusWriter out = new StreamingCorpusWriter();
}
