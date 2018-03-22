package getalp.wsd.ufsac.streaming.writer;

import getalp.wsd.ufsac.core.Corpus;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;

public class StreamingCorpusFullWriter
{
    public void open(String path)
    {
        out.open(path);
    }
    
    public void writeCorpus(Corpus corpus)
    {
        out.writeBeginCorpus(corpus);
        for (Document document : corpus.getDocuments())
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
        out.writeEndCorpus();
    }
    
    public void close()
    {
        out.close();
    }
    
    private StreamingCorpusWriter out = new StreamingCorpusWriter();
}
