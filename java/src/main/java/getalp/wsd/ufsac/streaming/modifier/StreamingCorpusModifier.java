package getalp.wsd.ufsac.streaming.modifier;

import getalp.wsd.common.utils.File;
import getalp.wsd.ufsac.core.*;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriter;

public class StreamingCorpusModifier
{
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
    
    public void readBeginCorpus(Corpus corpus)
    {
        out.writeBeginCorpus(corpus);
    }

    public void readBeginDocument(Document document)
    {
        out.writeBeginDocument(document);
    }

    public void readBeginParagraph(Paragraph paragraph)
    {
        out.writeBeginParagraph(paragraph);
    }

    public void readBeginSentence(Sentence sentence)
    {
        out.writeBeginSentence(sentence);
    }

    public void readWord(Word word)
    {
        out.writeWord(word);
    }

    public void readEndSentence()
    {
        out.writeEndSentence();
    }

    public void readEndParagraph()
    {
        out.writeEndParagraph();
    }

    public void readEndDocument()
    {
        out.writeEndDocument();
    }

    public void readEndCorpus()
    {
        out.writeEndCorpus();
    }

    private StreamingCorpusWriter out = new StreamingCorpusWriter();

    private StreamingCorpusReader in = new StreamingCorpusReader()
    {
        @Override
        public void readBeginCorpus(Corpus corpus)
        {
            StreamingCorpusModifier.this.readBeginCorpus(corpus);
        }

        @Override
        public void readBeginDocument(Document document)
        {
            StreamingCorpusModifier.this.readBeginDocument(document);
        }

        @Override
        public void readBeginParagraph(Paragraph paragraph)
        {
            StreamingCorpusModifier.this.readBeginParagraph(paragraph);
        }

        @Override
        public void readBeginSentence(Sentence sentence)
        {
            StreamingCorpusModifier.this.readBeginSentence(sentence);
        }

        @Override
        public void readWord(Word word)
        {
            StreamingCorpusModifier.this.readWord(word);
        }

        @Override
        public void readEndSentence()
        {
            StreamingCorpusModifier.this.readEndSentence();
        }

        @Override
        public void readEndParagraph()
        {
            StreamingCorpusModifier.this.readEndParagraph();
        }

        @Override
        public void readEndDocument()
        {
            StreamingCorpusModifier.this.readEndDocument();
        }

        @Override
        public void readEndCorpus()
        {
            StreamingCorpusModifier.this.readEndCorpus();
        }
    };
}
