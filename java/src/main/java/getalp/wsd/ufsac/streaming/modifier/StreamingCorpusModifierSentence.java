package getalp.wsd.ufsac.streaming.modifier;

import getalp.wsd.common.utils.File;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriter;

public class StreamingCorpusModifierSentence
{
    public void modifySentence(Sentence sentence)
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
        private Sentence currentSentence;
        
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
            currentSentence = sentence;
        }

        @Override
        public void readWord(Word word)
        {
            word.setParentSentence(currentSentence);
        }
        
        @Override
        public void readEndSentence()
        {
            modifySentence(currentSentence);
            out.writeBeginSentence(currentSentence);
            for (Word word : currentSentence.getWords())
            {
                out.writeWord(word);
            }
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
