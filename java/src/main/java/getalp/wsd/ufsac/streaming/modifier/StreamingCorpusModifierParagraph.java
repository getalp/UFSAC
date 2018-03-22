package getalp.wsd.ufsac.streaming.modifier;

import getalp.wsd.common.utils.File;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriter;

public class StreamingCorpusModifierParagraph
{
    public void modifyParagraph(Paragraph paragraph)
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
        private Paragraph currentParagraph;
        
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
            currentParagraph = paragraph;
        }

        @Override
        public void readBeginSentence(Sentence sentence)
        {
            currentSentence = sentence;
            currentSentence.setParentParagraph(currentParagraph);
        }

        @Override
        public void readWord(Word word)
        {
            word.setParentSentence(currentSentence);
        }
        
        @Override
        public void readEndParagraph()
        {
            modifyParagraph(currentParagraph);
            out.writeBeginParagraph(currentParagraph);
            for (Sentence sentence : currentParagraph.getSentences())
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
