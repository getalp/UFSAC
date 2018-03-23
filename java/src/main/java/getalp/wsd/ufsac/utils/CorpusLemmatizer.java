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
    public CorpusLemmatizer()
    {

    }

    public void tag(List<Word> words)
    {
        addWNMorphyLemmaAnnotations(words);
    }

    private static void addWNMorphyLemmaAnnotations(List<Word> words)
    {        
        WordnetHelper wn = WordnetHelper.wn();
        for (Word word : words)
        {
            if (word.hasAnnotation("lemma")) continue;
            String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
            if (pos.equals("x")) continue;
            word.setAnnotation("lemma", wn.morphy(word.getValue(), pos));
        }
    }
}
