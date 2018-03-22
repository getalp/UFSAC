import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class AddCorpusLemmasAndPOS
{
    public static void main(String[] args)
    {
        doIt("data/corpus/dso.xml", "data/corpus/dso2.xml", 16);
    }
    
    public static void doIt(String inPath, String outPath, int wordnetVersion)
    {
        Wrapper<Integer> countTotal = new Wrapper<Integer>(0);
        Wrapper<Integer> countFailed = new Wrapper<Integer>(0);
        
        MaxentTagger tagger = new MaxentTagger("data/stanford/model/english.tagger");        
        
        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                List<TaggedWord> stanfordSentence = tagger.tagSentence(toStanfordSentence(sentence));
                if (stanfordSentence.size() != sentence.getWords().size()) throw new RuntimeException();
                for (int i = 0 ; i < stanfordSentence.size() ; i++)
                {
                    Word word = sentence.getWords().get(i);
                    TaggedWord stanfordWord = stanfordSentence.get(i);
                    String stanfordPostag = stanfordWord.tag();
                    if (word.hasAnnotation("pos"))
                    {
                        String stanfordPostagProcessed = POSConverter.toWNPOS(stanfordPostag);
                        String inplacePostagProcessed = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
                        if (stanfordPostagProcessed.equals(inplacePostagProcessed))
                        {
                            word.setAnnotation("pos", stanfordPostag);
                        }
                    }
                    else
                    {
                        word.setAnnotation("pos", stanfordPostag);
                    }
                    if (!word.hasAnnotation("lemma"))
                    {
                        String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
                        if (!pos.equals("x"))
                        {
                            countTotal.obj++;
                            String surfaceForm = word.getValue();
                            String lemma = WordnetHelper.wn(wordnetVersion).morphy(surfaceForm, pos);
                            if (WordnetHelper.wn(wordnetVersion).isWordKeyExists(lemma + "%" + pos))
                            {
                                word.setAnnotation("lemma", lemma);
                            }
                            else
                            {
                                //System.out.println("Failed : " + surfaceForm);
                                countFailed.obj++;
                            }
                        }
                    }
                }
            }
        };
        
        inout.load(inPath, outPath);
        
        System.out.println("Info : " + countTotal.obj + " total missing tags");
        System.out.println("Info : " + (countTotal.obj - countFailed.obj) + " suceed to tag");
        System.out.println("Info : " + countFailed.obj + " failed to tag");
    }
    
    private static List<HasWord> toStanfordSentence(Sentence sentence)
    {
        List<HasWord> stanfordSentence = new ArrayList<>();
        for (Word word : sentence.getWords())
        {
            stanfordSentence.add(new edu.stanford.nlp.ling.Word(word.getValue()));
        }
        return stanfordSentence;
    }
}
