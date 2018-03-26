package getalp.wsd.ufsac.utils;

import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import getalp.wsd.common.utils.StdOutStdErr;
import getalp.wsd.ufsac.core.Word;

/**
 * This class allows to annotate a list of words with parts-of-speech, using the Stanford POS Tagger.
 */
public class CorpusPOSTagger
{
    private static final String qualityModelPath = "data/stanford/models/english-bidirectional-distsim.tagger";
    
    private static final String speedModelPath = "data/stanford/models/english-left3words-distsim.tagger";
    
    private MaxentTagger tagger = null;
    
    private boolean privilegiateSpeedOverQuality;
    
    private String posAnnotationName;

    public CorpusPOSTagger()
    {
        this(false, "pos");
    }

    public CorpusPOSTagger(String posAnnotationName)
    {
        this(false, posAnnotationName);
    }

    public CorpusPOSTagger(boolean privilegiateSpeedOverQuality)
    {
        this(privilegiateSpeedOverQuality, "pos");
    }

    public CorpusPOSTagger(boolean privilegiateSpeedOverQuality, String posAnnotationName)
    {
        this.privilegiateSpeedOverQuality = privilegiateSpeedOverQuality;
        this.posAnnotationName = posAnnotationName;
    }

    public void tag(List<Word> words)
    {
        addStanfordPOSAnnotations(words);
    }

    private void addStanfordPOSAnnotations(List<Word> words)
    {
        if (tagger == null) initStanfordPOSTagger();
        
        List<TaggedWord> stanfordWords = tagger.tagSentence(toStanfordWordList(words));
        assert (stanfordWords.size() != words.size());
        for (int i = 0; i < stanfordWords.size(); i++)
        {
            Word word = words.get(i);
            String pos = word.getAnnotationValue(posAnnotationName);
            if (!pos.isEmpty()) continue;
            pos = stanfordWords.get(i).tag();
            word.setAnnotation(posAnnotationName, pos);
        }
    }
    
    private void initStanfordPOSTagger()
    {
        StdOutStdErr.stfu();
        if (privilegiateSpeedOverQuality)
        {
            tagger = new MaxentTagger(speedModelPath);
        }
        else
        {
            tagger = new MaxentTagger(qualityModelPath);
        }
        StdOutStdErr.speak();
    }

    private List<HasWord> toStanfordWordList(List<Word> words)
    {
        List<HasWord> stanfordSentence = new ArrayList<>();
        for (Word word : words)
        {
            stanfordSentence.add(new edu.stanford.nlp.ling.Word(word.getValue()));
        }
        return stanfordSentence;
    }
}
