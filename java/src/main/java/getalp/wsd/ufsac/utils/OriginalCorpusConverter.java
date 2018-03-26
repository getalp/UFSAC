package getalp.wsd.ufsac.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.StdOutStdErr;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.common.wordnet.WordnetMapping;
import getalp.wsd.ufsac.converter.CorpusConverter;
import getalp.wsd.ufsac.core.Annotation;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifier;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierWord;

public class OriginalCorpusConverter
{
    private CorpusPOSTagger posTagger = new CorpusPOSTagger(false, "stanford_pos");
    
    private CorpusLemmatizer lemmatizer = new CorpusLemmatizer();
    
    
    
    private Map<String, Integer> wordAnnotationOrder = null;
    
    public void convert(CorpusConverter formatConverter, String originalCorpusPath, String ufsacCorpusPath, int originalWordnetVersion, int newWordnetVersion, boolean mergeDuplicateSentences)
    {
        System.out.println("[" + ufsacCorpusPath + "] Converting...");
        formatConverter.convert(originalCorpusPath, ufsacCorpusPath, originalWordnetVersion);
        postProcess(ufsacCorpusPath, originalWordnetVersion, newWordnetVersion, mergeDuplicateSentences);
    }
        
    private void postProcess(String corpusPath, int originalWordnetVersion, int newWordnetVersion, boolean mergeDuplicateSentences)
    {
        cleanWords(corpusPath);
        removeEmptyWords(corpusPath);
        removeEmptySentences(corpusPath);
        if (mergeDuplicateSentences)
        {
            mergeDuplicatedSentences(corpusPath, originalWordnetVersion, newWordnetVersion);
        }
        removeInvalidWordnetAnnotations(corpusPath, originalWordnetVersion);
        convertWordnetAnnotations(corpusPath, originalWordnetVersion, newWordnetVersion);
        addStanfordPOSAnnotations(corpusPath);
        setLemmaAndPOSAnnotationsFromFirstSenseAnnotations(corpusPath, newWordnetVersion);
        addWNMorphyLemmaAnnotations(corpusPath, newWordnetVersion);
        removeSenseTagsWhereLemmaOrPOSDiffers(corpusPath, newWordnetVersion);
        reorganizeWordAnnotations(corpusPath);
    }

    private void cleanWords(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Cleaning words...");
        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            @Override
            public void modifyWord(Word word)
            {
                String wordValue = word.getValue();
                wordValue = RegExp.invisiblePattern.matcher(wordValue).replaceAll("");
                word.setValue(wordValue);
            }
        };
        inout.load(corpusPath);
    }
    
    private void removeEmptyWords(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Removing empty words...");
        StreamingCorpusModifier inout = new StreamingCorpusModifier()
        {
            @Override
            public void readWord(Word word)
            {
                String wordValue = word.getValue();
                if (!wordValue.isEmpty())
                {
                    super.readWord(word);
                }
            }
        };
        inout.load(corpusPath);
    }

    private void removeEmptySentences(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Removing empty sentences...");
        StreamingCorpusModifier inout = new StreamingCorpusModifier()
        {
            private Sentence currentSentence;
            
            private boolean currentSentenceIsEmpty;
            
            @Override
            public void readBeginSentence(Sentence sentence)
            {
                currentSentence = sentence;
                currentSentenceIsEmpty = true;
            }
            
            @Override
            public void readWord(Word word)
            {
                if (currentSentenceIsEmpty)
                {
                    super.readBeginSentence(currentSentence);
                    currentSentenceIsEmpty = false;
                }
                super.readWord(word);
            }
            
            @Override
            public void readEndSentence()
            {
                if (!currentSentenceIsEmpty)
                {
                    super.readEndSentence();
                }
            }
        };
        inout.load(corpusPath);
    }

    private void removeInvalidWordnetAnnotations(String corpusPath, int wnVersion)
    {
        System.out.println("[" + corpusPath + "] Removing invalid WN " + wnVersion + " annotations...");

        Wrapper<Integer> total = new Wrapper<>(0);
        Wrapper<Integer> incorrect = new Wrapper<>(0);

        WordnetHelper wn = WordnetHelper.wn(wnVersion);
        String senseTag = "wn" + wnVersion + "_key";

        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            @Override
            public void modifyWord(Word word)
            {
                if (word.hasAnnotation(senseTag))
                {
                    List<String> oldSenseKeys = word.getAnnotationValues(senseTag, ";");
                    List<String> newSenseKeys = new ArrayList<>();
                    for (String senseKey : oldSenseKeys)
                    {
                        total.obj++;
                        if (wn.isSenseKeyExists(senseKey) && !newSenseKeys.contains(senseKey))
                        {
                            newSenseKeys.add(senseKey);
                        }
                        else
                        {
                            incorrect.obj++;
                        }
                    }
                    word.setAnnotation(senseTag, StringUtils.join(newSenseKeys, ";"));
                }
            }
        };
        inout.load(corpusPath);

        System.out.println("\tOn a total of " + total.obj + " WN " + wnVersion + " annotations, " + incorrect.obj + " were incorrect and deleted");
    }

    private void convertWordnetAnnotations(String inputPath, int wnVersionIn, int wnVersionOut)
    {
        if (wnVersionIn == wnVersionOut) return;
        System.out.println("[" + inputPath + "] Converting WN annotations from WN " + wnVersionIn + " to WN " + wnVersionOut + "...");

        Wrapper<Integer> total = new Wrapper<>(0);
        Wrapper<Integer> failed = new Wrapper<>(0);

        WordnetMapping wnMapping = WordnetMapping.wnXtoY(wnVersionIn, wnVersionOut);
        String senseTagIn = "wn" + wnVersionIn + "_key";
        String senseTagOut = "wn" + wnVersionOut + "_key";

        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            public void modifyWord(Word word)
            {
                if (word.hasAnnotation(senseTagIn))
                {
                    List<String> oldSenseKeys = word.getAnnotationValues(senseTagIn, ";");
                    List<String> newSenseKeys = new ArrayList<>();
                    for (String oldSenseKey : oldSenseKeys)
                    {
                        total.obj++;
                        List<String> newSenseKey = wnMapping.fromXtoY(oldSenseKey);
                        if (newSenseKey == null || newSenseKey.isEmpty())
                        {
                            failed.obj++;
                        }
                        else
                        {
                            newSenseKeys.addAll(newSenseKey);
                        }
                    }
                    newSenseKeys = newSenseKeys.stream().distinct().collect(Collectors.toList());
                    word.setAnnotation(senseTagOut, newSenseKeys, ";");
                }
            }
        };
        inout.load(inputPath);

        System.out.println("\tOn a total of " + total.obj + " WN " + wnVersionIn + " annotations, failed to convert " + failed.obj + " to WN " + wnVersionOut);
    }

    private void setLemmaAndPOSAnnotationsFromFirstSenseAnnotations(String inputPath, int wordnetVersion)
    {
        System.out.println("[" + inputPath + "] Setting Lemma and POS annotations from first WN " + wordnetVersion + " sense annotation...");
        String senseTag = "wn" + wordnetVersion + "_key";
        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            public void modifyWord(Word word)
            {
                if (word.hasAnnotation(senseTag))
                {
                    String senseKey = word.getAnnotationValue(senseTag);
                    String senseKeyPOS = POSConverter.toWNPOS(Integer.valueOf(senseKey.substring(senseKey.indexOf("%") + 1, senseKey.indexOf("%") + 2)));
                    String senseKeyLemma = senseKey.substring(0, senseKey.indexOf("%"));
                    String currentPOS = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
                    if (!currentPOS.equals(senseKeyPOS))
                    {
                        word.setAnnotation("pos", senseKeyPOS);
                    }
                    String currentLemma = word.getAnnotationValue("lemma");
                    if (!currentLemma.equals(senseKeyLemma))
                    {
                        word.setAnnotation("lemma", senseKeyLemma);
                    }
                }
            }
        };
        inout.load(inputPath);
    }

    private void addStanfordPOSAnnotations(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Adding POS annotations with Stanford POS Tagger...");
        
        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                posTagger.tag(sentence.getWords());
            }
        };
        inout.load(corpusPath);
    }
    
    private void addWNMorphyLemmaAnnotations(String corpusPath, int wnVersion)
    {
        System.out.println("[" + corpusPath + "] Adding lemma annotations with WordNet Morphy...");
        
        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                lemmatizer.tag(sentence.getWords());
            }
        };
        inout.load(corpusPath);
    }

    private void removeSenseTagsWhereLemmaOrPOSDiffers(String corpusPath, int wordnetVersion)
    {
        System.out.println("[" + corpusPath + "] Removing WN " + wordnetVersion + " sense tags where lemma or POS differs...");

        String senseTag = "wn" + wordnetVersion + "_key";
        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            public void modifyWord(Word word)
            {
                if (word.hasAnnotation(senseTag))
                {
                    String lemma = word.getAnnotationValue("lemma");
                    String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
                    List<String> oldSenseKeys = word.getAnnotationValues(senseTag, ";");
                    List<String> newSenseKeys = new ArrayList<>();
                    for (String sense : oldSenseKeys)
                    {
                        String senseLemma = sense.substring(0, sense.indexOf("%"));
                        String sensePos = POSConverter.toWNPOS(Integer.valueOf(sense.substring(sense.indexOf("%") + 1, sense.indexOf("%") + 2)));
                        if (senseLemma.equals(lemma) && sensePos.equals(pos) && !newSenseKeys.contains(sense))
                        {
                            newSenseKeys.add(sense);
                        }
                    }
                    word.setAnnotation(senseTag, StringUtils.join(newSenseKeys, ";"));
                }
            }
        };
        inout.load(corpusPath);
    }
    
    private void reorganizeWordAnnotations(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Reorganizing word annotations...");

        if (wordAnnotationOrder == null) initMapAnnotationsOrder();
        
        StreamingCorpusModifierWord inout = new StreamingCorpusModifierWord()
        {
            public void modifyWord(Word word)
            {
                List<Annotation> annotations = word.getAnnotations();
                List<Annotation> sortedAnnotations = new ArrayList<>(annotations);
                sortedAnnotations.sort(new Comparator<Annotation>() 
                {
                    @Override
                    public int compare(Annotation a1, Annotation a2)
                    {
                        String a1Name = a1.getAnnotationName();
                        String a2Name = a2.getAnnotationName();
                        int a1Order = wordAnnotationOrder.getOrDefault(a1Name, Integer.MAX_VALUE);
                        int a2Order = wordAnnotationOrder.getOrDefault(a2Name, Integer.MAX_VALUE);
                        return Integer.compare(a1Order, a2Order);
                    }
                });
                word.removeAllAnnotations();
                sortedAnnotations.forEach(a -> word.setAnnotation(a.getAnnotationName(), a.getAnnotationValue()));
            }
        };
        inout.load(corpusPath);
    }

    private void initMapAnnotationsOrder()
    {
        wordAnnotationOrder.put("surface_form", 0);
        wordAnnotationOrder.put("lemma", 1);
        wordAnnotationOrder.put("pos", 2);
        wordAnnotationOrder.put("wn16_key", 3);
        wordAnnotationOrder.put("wn17_key", 4);
        wordAnnotationOrder.put("wn171_key", 5);
        wordAnnotationOrder.put("wn20_key", 6);
        wordAnnotationOrder.put("wn21_key", 7);
        wordAnnotationOrder.put("wn30_key", 8);
        wordAnnotationOrder.put("wn31_key", 9);
    }

    private void mergeDuplicatedSentences(String inputPath, int... wnVersionToKeep)
    {
        System.out.println("[" + inputPath + "] Merging duplicated sentences...");

        Wrapper<Integer> total = new Wrapper<>(0);
        
        List<String> senseTagToKeep = new ArrayList<>();
        for (int wnVersion : wnVersionToKeep)
        {
            senseTagToKeep.add("wn" + wnVersion + "_key");
        }
        
        StreamingCorpusModifier inout = new StreamingCorpusModifier()
        {
            private Paragraph currentParagraph;
            
            private Sentence currentSentence;

            private Map<String, Sentence> realSentences = new LinkedHashMap<>();
            
            @Override
            public void readBeginParagraph(Paragraph paragraph)
            {
                currentParagraph = paragraph;
            }
            
            @Override
            public void readBeginSentence(Sentence sentence)
            {
                currentSentence = sentence;
            }
            
            @Override
            public void readWord(Word word)
            {
                currentSentence.addWord(word);
            }
            
            @Override
            public void readEndSentence()
            {
                String sentenceAsString = currentSentence.toString();
                if (!realSentences.containsKey(sentenceAsString))
                {
                    realSentences.put(sentenceAsString, currentSentence);
                    currentSentence.setParentParagraph(currentParagraph);
                }
                else
                {
                    total.obj++;
                    Sentence realSentence = realSentences.get(sentenceAsString);
                    assert(realSentence.getWords().size() == currentSentence.getWords().size());
                    for (int i = 0; i < currentSentence.getWords().size(); i++)
                    {
                        Word currentSentenceWord = currentSentence.getWords().get(i);
                        Word realWord = realSentence.getWords().get(i);
                        for (String senseTag : senseTagToKeep)
                        {
                            if (currentSentenceWord.hasAnnotation(senseTag))
                            {
                                List<String> oldSenseKeys = currentSentenceWord.getAnnotationValues(senseTag, ";");
                                List<String> newSenseKeys = new ArrayList<>(realWord.getAnnotationValues(senseTag, ";"));
                                for (String sense : oldSenseKeys)
                                {
                                    if (!newSenseKeys.contains(sense))
                                    {
                                        newSenseKeys.add(sense);
                                    }
                                }
                                realWord.setAnnotation(senseTag, StringUtils.join(newSenseKeys, ";"));
                            }
                        }
                    }
                }
            }

            @Override
            public void readEndParagraph()
            {
                super.readBeginParagraph(currentParagraph);
                for (Sentence sentence : currentParagraph.getSentences())
                {
                    super.readBeginSentence(sentence);
                    sentence.getWords().forEach(super::readWord);
                    super.readEndSentence();
                }
                super.readEndParagraph();
            }
        };
            
        inout.load(inputPath);

        System.out.println("\tFound " + total.obj + " duplicated sentences in " + inputPath);
    }
}
