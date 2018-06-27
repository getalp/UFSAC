package getalp.wsd.ufsac.utils;

import java.util.List;
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Word;

/**
 * This class allows to annotate a list of words with lemma tags, using the WordNet's Morphy tool.
 * Every word must be POS tagged first in order to be lemmatized.
 */
public class CorpusLemmatizer
{
    private String lemmaAnnotationName;
    
    private WordnetHelper wn;

    public CorpusLemmatizer()
    {
        this("lemma", 30);
    }

    public CorpusLemmatizer(String lemmaAnnotationName)
    {
        this(lemmaAnnotationName, 30);
    }

    public CorpusLemmatizer(int wnVersion)
    {
        this("lemma", wnVersion);
    }

    public CorpusLemmatizer(String lemmaAnnotationName, int wnVersion)
    {
        this.lemmaAnnotationName = lemmaAnnotationName;
        this.wn = WordnetHelper.wn(wnVersion);
    }

    public void tag(List<Word> words)
    {
        addWNMorphyLemmaAnnotations(words);
    }

    private void addWNMorphyLemmaAnnotations(List<Word> words)
    {
        for (Word word : words)
        {
            if (word.hasAnnotation(lemmaAnnotationName)) continue;
            String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
            if (pos.equals("x")) continue;
            word.setAnnotation(lemmaAnnotationName, wn.morphy(word.getValue(), pos));
        }
    }
}
