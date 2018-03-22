package getalp.wsd.ufsac.streaming.writer;

import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;

public class StreamingCorpusWriterSentence
{
    public StreamingCorpusWriterSentence()
    {

    }
    
    public void open(String path)
    {
        out.open(path);
        out.writeBeginCorpus();
        out.writeBeginDocument();
        out.writeBeginParagraph();
    }
    
    public void writeSentence(Sentence sentence)
    {
        out.writeBeginSentence(sentence);
        for (Word word : sentence.getWords())
        {
            out.writeWord(word);
        }
        out.writeEndSentence();
    }
    
    public void close()
    {
        out.writeEndParagraph();
        out.writeEndDocument();
        out.writeEndCorpus();
        out.close();
    }
    
    private StreamingCorpusWriter out = new StreamingCorpusWriter();
}
