package getalp.wsd.ufsac.streaming.reader;

import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;

public class StreamingCorpusReaderSentence
{
    public void readSentence(Sentence sentence)
    {
        
    }

    public void load(String path)
    {
        reader.load(path);
    }
    
    private StreamingCorpusReader reader = new StreamingCorpusReader()
    {
        private Sentence currentSentence;
      
        @Override
        public void readBeginSentence(Sentence sentence)
        {
            currentSentence = sentence;
        }

        @Override
        public void readWord(Word word)
        {
            Word currentWord = word;
            currentWord.setParentSentence(currentSentence);
        }

        @Override
        public void readEndSentence()
        {
            StreamingCorpusReaderSentence.this.readSentence(currentSentence);
        }
    };
}
