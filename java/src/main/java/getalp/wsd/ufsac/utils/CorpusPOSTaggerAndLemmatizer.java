package getalp.wsd.ufsac.utils;

import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.StdOutStdErr;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Word;

public class CorpusPOSTaggerAndLemmatizer
{
    private static final String qualityModelPath = "data/stanford/models/english-bidirectional-distsim.tagger";
    
    private static final String speedModelPath = "data/stanford/models/english-left3words-distsim.tagger";
    
    private MaxentTagger tagger = null;
    
    private boolean privilegiateSpeedOverQuality;

    public CorpusPOSTaggerAndLemmatizer()
    {
        this(false);
    }

    public CorpusPOSTaggerAndLemmatizer(boolean privilegiateSpeedOverQuality)
    {
        this.privilegiateSpeedOverQuality = privilegiateSpeedOverQuality;
    }
    
    public void tag(List<Word> words)
    {
        addStanfordPOSAnnotations(words);
        addWNMorphyLemmaAnnotations(words);
    }

    private void addStanfordPOSAnnotations(List<Word> words)
    {
        if (tagger == null) initStanfordPOSTagger();
        
        List<TaggedWord> stanfordWords = tagger.tagSentence(toStanfordWordList(words));
        assert (stanfordWords.size() != words.size());
        for (int i = 0; i < stanfordWords.size(); i++)
        {
            Word word = words.get(i);
            String pos = word.getAnnotationValue("pos");
            if (!pos.isEmpty()) continue;
            pos = stanfordWords.get(i).tag();
            word.setAnnotation("pos", pos);
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

    private static List<HasWord> toStanfordWordList(List<Word> words)
    {
        List<HasWord> stanfordSentence = new ArrayList<>();
        for (Word word : words)
        {
            stanfordSentence.add(new edu.stanford.nlp.ling.Word(word.getValue()));
        }
        return stanfordSentence;
    }

    private static void addWNMorphyLemmaAnnotations(List<Word> words)
    {        
        WordnetHelper wn = WordnetHelper.wn();
        for (Word word : words)
        {
            String lemma = word.getAnnotationValue("lemma");
            if (!lemma.isEmpty()) continue;
            String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
            if (pos.equals("x")) continue;
            word.setAnnotation("lemma", wn.morphy(word.getValue(), pos));
        }
    }
}
