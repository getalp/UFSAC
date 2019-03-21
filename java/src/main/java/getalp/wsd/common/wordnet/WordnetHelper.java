package getalp.wsd.common.wordnet;

import java.io.File;
import java.util.*;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import getalp.wsd.common.utils.Resources;
import getalp.wsd.ufsac.core.Sentence;

public class WordnetHelper
{
    public static final String wordnetDirectoryPath = "/data/wordnet";

    private Map<String, String> senseToSynset;

    private Map<String, List<String>> synsetToSenseList;

    private Map<String, Sentence> synsetToGloss;

    private Map<String, List<String>> senseToRelatedSynsets;

    private Map<String, List<String>> synsetToHypernymsSynsets;

    private Map<String, List<String>> synsetToInstanceHypernymsSynsets;

    private Map<String, List<String>> synsetToAntonymsSynsets;

    private Map<String, List<String>> wordKeyToSenseList;

    private Map<String, String> senseKeyToSenseNumber;

    private Map<String, String> senseNumberToSenseKey;
    
    private Map<String, String> wordKeyToFirstSenseKey;
    
    private Dictionary wordnet;
    
    private int version;
    
    private WordnetStemmer morphy;


    private static final Map<Integer, WordnetHelper> loadedHelpers = new HashMap<>();

    
    public static WordnetHelper wn16()
    {
    	return wn(16);
    }

    public static WordnetHelper wn21()
    {
    	return wn(21);
    }

    public static WordnetHelper wn30()
    {
    	return wn(30);
    }

    public static WordnetHelper wn()
    {
        return wn(30);
    }
    
    public static WordnetHelper wn(int version)
    {
        if (!loadedHelpers.containsKey(version))
        {
            loadedHelpers.put(version, new WordnetHelper(wordnetDirectoryPath + "/" + version + "/dict", version));
        }
        return loadedHelpers.get(version);
    }

    public String getSynsetKeyFromSenseKey(String senseKey)
    {
        return senseToSynset.get(senseKey);
    }
    
    public List<String> getSenseKeyListFromSynsetKey(String synsetKey)
    {
        return synsetToSenseList.get(synsetKey);
    }
    
    public List<String> getSenseKeyListFromSenseKey(String senseKey)
    {
        return synsetToSenseList.get(senseToSynset.get(senseKey));
    }
    
    public String getSenseKeyFromSenseNumber(String senseNumber)
    {
        return senseNumberToSenseKey.get(senseNumber);
    }
    
    public String getSenseNumberFromSenseKey(String sensekey)
    {
        return senseKeyToSenseNumber.get(sensekey);
    }
    
    public Sentence getGlossFromSynsetKey(String synsetKey)
    {
        return synsetToGloss.get(synsetKey);
    }

    public Sentence getGlossFromSenseKey(String senseKey)
    {
        return synsetToGloss.get(senseToSynset.get(senseKey));
    }

    public List<String> getRelatedSynsetsKeyFromSenseKey(String senseKey)
    {
        return senseToRelatedSynsets.get(senseKey);
    }

    public List<String> getHypernymSynsetKeysFromSynsetKey(String synsetKey)
    {
        return synsetToHypernymsSynsets.get(synsetKey);
    }

    public List<String> getInstanceHypernymSynsetKeysFromSynsetKey(String synsetKey)
    {
        return synsetToInstanceHypernymsSynsets.get(synsetKey);
    }

    public List<String> getAntonymSynsetKeysFromSynsetKey(String synsetKey)
    {
        return synsetToAntonymsSynsets.get(synsetKey);
    }

    public Collection<String> getVocabulary()
    {
    	return wordKeyToSenseList.keySet();
    }
    
    public Collection<String> getAllSenseKeys()
    {
        return senseToSynset.keySet();
    }
    
    public List<String> getSenseKeyListFromWordKey(String wordKey)
    {
    	return wordKeyToSenseList.get(wordKey);
    }

    public String getFirstSenseKeyFromWordKey(String wordKey)
    {
        return wordKeyToFirstSenseKey.get(wordKey);
    }

    public int getFirstSenseKeyIndexFromWordKey(String wordKey)
    {
        String firstSenseKey = getFirstSenseKeyFromWordKey(wordKey);
        List<String> senseKeys = getSenseKeyListFromWordKey(wordKey);
        for (int i = 0 ; i < senseKeys.size() ; i++)
        {
            if (senseKeys.get(i).equals(firstSenseKey))
            {
                return i;
            }
        }
        return -1;
    }

    public boolean isSenseKeyExists(String senseKey)
    {
        return senseToSynset.containsKey(senseKey);
    }

    public boolean isSynsetKeyExists(String synsetKey)
    {
        return synsetToSenseList.containsKey(synsetKey);
    }
    
    public boolean isLemmaExists(String lemma)
    {
        for (String wordKey : wordKeyToSenseList.keySet())
        {
            if (lemma.equals(wordKey.substring(0, wordKey.indexOf("%"))))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isWordKeyExists(String wordKey)
    {
        return wordKeyToSenseList.containsKey(wordKey);
    }
    
    public int getVersion()
    {
        return version;
    }
    
    /// posTag = "n", "v", "a", "r" 
    public String morphy(String surfaceForm, String posTag)
    {
        if (posTag == null || posTag.isEmpty()) return morphy(surfaceForm);
        return morphy(surfaceForm, POS.getPartOfSpeech(posTag.charAt(0)));
    }

    public String morphy(String surfaceForm)
    {
        return morphy(surfaceForm, (POS) null);
    }
    
    private String morphy(String surfaceForm, POS pos)
    {
        try
        {
            return morphy.findStems(surfaceForm, pos).get(0);
        }
        catch (Exception e)
        {
            return "";
        }
    }
    
    private WordnetHelper(String wordnetDictPath, int version)
    {
        this.version = version;
    	load(wordnetDictPath);
    }
    
    private void load(String wordnetDictPath)
    {
        senseToSynset = new HashMap<>();
        synsetToSenseList = new HashMap<>();
        synsetToGloss = new HashMap<>();
        senseToRelatedSynsets = new HashMap<>();
        synsetToHypernymsSynsets = new HashMap<>();
        synsetToInstanceHypernymsSynsets = new HashMap<>();
        synsetToAntonymsSynsets = new HashMap<>();
        wordKeyToSenseList = new HashMap<>();
        senseKeyToSenseNumber = new HashMap<>();
        senseNumberToSenseKey = new HashMap<>();
        wordKeyToFirstSenseKey = new HashMap<>();
        try
        {
            String tmpdir = System.getProperty("java.io.tmpdir") + "/ufsac";
            Resources.extractResources(tmpdir);
            wordnet = new Dictionary(new File(tmpdir + wordnetDictPath));
            wordnet.open();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        morphy = new WordnetStemmer(wordnet);
        
        Iterator<ISenseEntry> iise = wordnet.getSenseEntryIterator();
        while (iise.hasNext())
        {
            ISenseEntry ise = iise.next();
            String senseKey = ise.getSenseKey().toString().toLowerCase();
            if (version == 30)
            {
                senseKey = senseKey.replaceAll("%5", "%3");
            }
            String senseLemma = ise.getSenseKey().getLemma().toLowerCase();
            char sensePos = ise.getSenseKey().getPOS().getTag();
            String senseNumber = senseLemma + "%" + sensePos + "#" + ise.getSenseNumber();
            senseKeyToSenseNumber.put(senseKey, senseNumber);
            senseNumberToSenseKey.put(senseNumber, senseKey);
            if (ise.getSenseNumber() == 1)
            {
                wordKeyToFirstSenseKey.put(senseLemma + "%" + sensePos, senseKey);
            }
        }

        for (POS pos : POS.values())
        {
            Iterator<ISynset> iis = wordnet.getSynsetIterator(pos);
            while (iis.hasNext())
            {
                ISynset is = iis.next();
                addSynset(is, pos);
            }
        }
    }
    
    private void addSynset(ISynset is, POS pos)
    {
        List<String> senseKeyList = new ArrayList<>();
        String synsetKey = "" + pos.getTag() + is.getOffset();
        for (IWord iw : is.getWords())
        {
            String lemma = iw.getLemma().toLowerCase();
            String wordKey = lemma + "%" + iw.getPOS().getTag();
            String senseKey = iw.getSenseKey().toString().toLowerCase();
            if (version == 30)
            {
                senseKey = senseKey.replaceAll("%5", "%3");
            }
            if (!senseKeyToSenseNumber.containsKey(senseKey))
            {
                continue;
            }
            if (senseKeyList.contains(senseKey)) // remove duplicates (e.g. a%1:10:00::)
            {
                continue;
            }
            senseKeyList.add(senseKey);
            senseToSynset.put(senseKey, synsetKey);
            senseToRelatedSynsets.put(senseKey, loadRelations(is, iw));
            synsetToHypernymsSynsets.put(synsetKey, loadHypernyms(is));
            synsetToInstanceHypernymsSynsets.put(synsetKey, loadInstanceHypernyms(is));
            synsetToAntonymsSynsets.put(synsetKey, loadAntonyms(iw));
            if (wordKeyToSenseList.containsKey(wordKey))
            {
                wordKeyToSenseList.get(wordKey).add(senseKey);
            }
            else
            {
                wordKeyToSenseList.put(wordKey, new ArrayList<>(Collections.singletonList(senseKey)));
            }
        }
        synsetToSenseList.put(synsetKey, senseKeyList);
        synsetToGloss.put(synsetKey, new Sentence(is.getGloss()));
    }
    
    private List<String> loadRelations(ISynset synset, IWord word) 
    {
        List<String> relatedSynsets = new ArrayList<>();

        // semantic relations
        for (Map.Entry<IPointer, List<ISynsetID>> iPointerListEntry : synset.getRelatedMap().entrySet()) 
        {
            for (ISynsetID iwd : iPointerListEntry.getValue()) 
            {
                ISynset relatedSynset = wordnet.getSynset(iwd);
                String relatedSynsetKey = "" + relatedSynset.getPOS().getTag() + relatedSynset.getOffset();
                relatedSynsets.add(relatedSynsetKey);
            }
        }
        
        // lexical relations
        for (Map.Entry<IPointer, List<IWordID>> iPointerListEntry : word.getRelatedMap().entrySet()) 
        {
            for (IWordID iwd : iPointerListEntry.getValue()) 
            {
                IWord relatedWord = wordnet.getWord(iwd);
                ISynset relatedSynset = relatedWord.getSynset();
                String relatedSynsetKey = "" + relatedSynset.getPOS().getTag() + relatedSynset.getOffset();
                relatedSynsets.add(relatedSynsetKey);
            }
        }

        return relatedSynsets;
    }

    private List<String> loadHypernyms(ISynset synset)
    {
        return loadSemanticRelationsBySymbol(synset, "@");
    }

    private List<String> loadInstanceHypernyms(ISynset synset)
    {
        return loadSemanticRelationsBySymbol(synset, "@i");
    }

    private List<String> loadAntonyms(IWord word)
    {
        return loadLexicalRelationsBySymbol(word, "!");
    }

    private List<String> loadSemanticRelationsBySymbol(ISynset synset, String relationSymbol)
    {
        List<String> relatedSynsetKeys = new ArrayList<>();
        // semantic relations
        for (Map.Entry<IPointer, List<ISynsetID>> iPointerListEntry : synset.getRelatedMap().entrySet())
        {
            if (iPointerListEntry.getKey().getSymbol().equals(relationSymbol))
            {
                for (ISynsetID iwd : iPointerListEntry.getValue())
                {
                    ISynset relatedSynset = wordnet.getSynset(iwd);
                    String relatedSynsetKey = "" + relatedSynset.getPOS().getTag() + relatedSynset.getOffset();
                    relatedSynsetKeys.add(relatedSynsetKey);
                }
            }
        }
        return relatedSynsetKeys;
    }

    private List<String> loadLexicalRelationsBySymbol(IWord word, String relationSymbol)
    {
        List<String> relatedSynsetKeys = new ArrayList<>();
        // lexical relations
        for (Map.Entry<IPointer, List<IWordID>> iPointerListEntry : word.getRelatedMap().entrySet())
        {
            if (iPointerListEntry.getKey().getSymbol().equals(relationSymbol))
            {
                for (IWordID iwd : iPointerListEntry.getValue())
                {
                    IWord relatedWord = wordnet.getWord(iwd);
                    ISynset relatedSynset = relatedWord.getSynset();
                    String relatedSynsetKey = "" + relatedSynset.getPOS().getTag() + relatedSynset.getOffset();
                    relatedSynsetKeys.add(relatedSynsetKey);
                }
            }
        }
        return relatedSynsetKeys;
    }

}
