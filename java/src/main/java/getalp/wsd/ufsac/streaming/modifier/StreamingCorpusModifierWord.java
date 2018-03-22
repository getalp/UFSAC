package getalp.wsd.ufsac.streaming.modifier;

import getalp.wsd.common.utils.File;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriter;

public class StreamingCorpusModifierWord 
{
    public void modifyWord(Word word)
    {
        
    }
    
    public void load(String inputPath)
    {
        String outputPath = File.createTemporaryFileName();
        out.open(outputPath);
        in.load(inputPath);
        out.close();
        File.moveFile(outputPath, inputPath);
    }

    public void load(String inputPath, String outputPath)
    {
        out.open(outputPath);
        in.load(inputPath);
        out.close();
    }

    private StreamingCorpusWriter out = new StreamingCorpusWriter();

    private StreamingCorpusReader in = new StreamingCorpusReader()
    {                        
        @Override
        public void readBeginCorpus(Corpus corpus)
        {
            out.writeBeginCorpus(corpus);
        }

        @Override
        public void readBeginDocument(Document document)
        {
            out.writeBeginDocument(document);
        }

        @Override
        public void readBeginParagraph(Paragraph paragraph)
        {
            out.writeBeginParagraph(paragraph);
        }

        @Override
        public void readBeginSentence(Sentence sentence)
        {
            out.writeBeginSentence(sentence);
        }

        @Override
        public void readWord(Word word)
        {
            modifyWord(word);
            out.writeWord(word);
        }
        
        @Override
        public void readEndSentence()
        {
            out.writeEndSentence();
        }
        
        @Override
        public void readEndParagraph()
        {
            out.writeEndParagraph();
        }

        @Override
        public void readEndDocument()
        {
            out.writeEndDocument();
        }

        @Override
        public void readEndCorpus()
        {
            out.writeEndCorpus();
        }
    };
}
