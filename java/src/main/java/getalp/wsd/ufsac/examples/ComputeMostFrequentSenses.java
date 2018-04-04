package getalp.wsd.ufsac.examples;
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderWord;
import java.util.*;

public class ComputeMostFrequentSenses
{
    /**
     * Usage: java getalp.wsd.ufsac.examples.ComputeMostFrequentSenses [corpus]...
     */
    public static void main(String[] corpusPaths) throws Exception
    {
        Map<String, Map<String, Integer>> wordKeyToSenseKeyCount = new HashMap<>();
        
        WordnetHelper wn = WordnetHelper.wn();
        
        for (String wordKey : wn.getVocabulary())
        {
            Map<String, Integer> senseKeyCount = new HashMap<>();
            for (String senseKey : wn.getSenseKeyListFromWordKey(wordKey))
            {
                senseKeyCount.put(senseKey, 0);
            }
            wordKeyToSenseKeyCount.put(wordKey, senseKeyCount);
        }
        
        StreamingCorpusReaderWord corpus = new StreamingCorpusReaderWord()
        {
            @Override
            public void readWord(Word word)
            {
                String lemma = word.getAnnotationValue("lemma");
                String pos = word.getAnnotationValue("pos");
                String senseKey = word.getAnnotationValue("wn" + wn.getVersion() + "_key");
                if (!lemma.isEmpty() && !pos.isEmpty() && !senseKey.isEmpty())
                {
                    pos = POSConverter.toWNPOS(pos);
                    String wordKey = lemma + "%" + pos;
                    Map<String, Integer> senseKeyCount = wordKeyToSenseKeyCount.get(wordKey);
                    Integer exValue = senseKeyCount.get(senseKey);
                    Integer newValue = exValue + 1;
                    senseKeyCount.put(senseKey, newValue);
                }
            }
        };

        for (String corpusPath : corpusPaths)
        {
            corpus.load(corpusPath);
        }
                
        for (String wordKey : wordKeyToSenseKeyCount.keySet())
        {
            String mostFrequentSenseKey = "";
            int mostFrequentSenseKeyCount = -1;
            for (String senseKey : wordKeyToSenseKeyCount.get(wordKey).keySet())
            {
                if (wordKeyToSenseKeyCount.get(wordKey).get(senseKey) > mostFrequentSenseKeyCount)
                {
                    mostFrequentSenseKey = senseKey;
                    mostFrequentSenseKeyCount = wordKeyToSenseKeyCount.get(wordKey).get(senseKey);
                }
            }
            System.out.println(wordKey + " " + mostFrequentSenseKey);
        }        
    }
}
