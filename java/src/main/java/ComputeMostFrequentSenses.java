import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderWord;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class ComputeMostFrequentSenses
{
    public static void main(String[] args) throws Exception
    {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        
        WordnetHelper wn21 = WordnetHelper.wn21();
        
        for (String wordKey : wn21.getVocabulary())
        {
            Map<String, Integer> mapmap = new HashMap<>();
            for (String senseKey : wn21.getSenseKeyListFromWordKey(wordKey))
            {
                mapmap.put(senseKey, 0);
            }
            map.put(wordKey, mapmap);
        }
        
        StreamingCorpusReaderWord corpus = new StreamingCorpusReaderWord()
        {
            @Override
            public void readWord(Word word)
            {
                String lemma = word.getAnnotationValue("lemma");
                String pos = word.getAnnotationValue("pos");
                String wn21Key = word.getAnnotationValue("wn21_key");
                if (!lemma.isEmpty() && !pos.isEmpty() && !wn21Key.isEmpty())
                {
                    pos = POSConverter.toWNPOS(pos);
                    String wordKey = lemma + "%" + pos;
                    String senseKey = wn21Key;
                    Map<String, Integer> mapmap = map.get(wordKey);
                    Integer exValue = mapmap.get(senseKey);
                    Integer newValue = exValue + 1;
                    mapmap.put(senseKey, newValue);
                }
            }
        };

        corpus.load("data/corpus/semcor.xml");
        
        BufferedWriter out = new BufferedWriter(new FileWriter("data/mfs"));
        
        for (String wordKey : map.keySet())
        {
            String bestSenseKey = "";
            int bestSenseKeyScore = -1;
            for (String senseKey : map.get(wordKey).keySet())
            {
                if (map.get(wordKey).get(senseKey) > bestSenseKeyScore)
                {
                    bestSenseKey = senseKey;
                    bestSenseKeyScore = map.get(wordKey).get(senseKey);
                }
            }
            out.write(wordKey + " " + bestSenseKey + "\n");
        }
        
        out.close();
        
    }
}
