package getalp.wsd.ufsac.utils;

import java.util.List;
import getalp.wsd.ufsac.core.Word;

/**
 * This class combines a CorpusPOSTagger and a CorpusLemmatizer.
 */
public class CorpusPOSTaggerAndLemmatizer
{
    private CorpusPOSTagger posTagger;
    
    private CorpusLemmatizer lemmatizer;
    
    public CorpusPOSTaggerAndLemmatizer()
    {
        this(false);
    }

    public CorpusPOSTaggerAndLemmatizer(boolean privilegiateSpeedOverQuality)
    {
        posTagger = new CorpusPOSTagger(privilegiateSpeedOverQuality);
        lemmatizer = new CorpusLemmatizer();
    }
    
    public void tag(List<Word> words)
    {
        posTagger.tag(words);
        lemmatizer.tag(words);
    }
}
