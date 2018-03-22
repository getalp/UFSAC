import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import getalp.wsd.common.utils.File;
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.RegExp;
import getalp.wsd.common.utils.StdOutStdErr;
import getalp.wsd.common.utils.StringUtils;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.common.wordnet.WordnetMapping;
import getalp.wsd.ufsac.converter.*;
import getalp.wsd.ufsac.core.Annotation;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifier;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierWord;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderSentence;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class ConvertOriginalCorpora
{
    private static MaxentTagger tagger = null;

    private static Map<String, Integer> wordAnnotationOrder = null;

    public static void main(String[] args) throws Exception
    {

        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/senseval2/senseval2", "data/corpus/raganato_senseval2.xml", 30, 30, false);
        
        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/senseval3/senseval3", "data/corpus/raganato_senseval3.xml", 30, 30, false);
        
        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2007/semeval2007", "data/corpus/raganato_semeval2007.xml", 30, 30, false);
        
        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2013/semeval2013", "data/corpus/raganato_semeval2013.xml", 30, 30, false);
        
        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2015/semeval2015", "data/corpus/raganato_semeval2015.xml", 30, 30, false);
        
        convertOriginalCorpus(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/ALL/ALL", "data/corpus/raganato_ALL.xml", 30, 30, false);

        convertOriginalCorpus(new SemcorConverter(), "data/original_corpus/semcor", "data/corpus/semcor.xml", 16, 30, false);
        
        convertOriginalCorpus(new DSOConverter(), "data/original_corpus/dso", "data/corpus/dso.xml", 16, 30, true);
        
        convertOriginalCorpus(new WNGTConverter(), "data/wordnet/30/glosstag", "data/corpus/wngt.xml", 30, 30, false);
        
        convertOriginalCorpus(new MASCConverter(), "data/original_corpus/google/masc", "data/corpus/masc.xml", 30, 30, true);

        convertOriginalCorpus(new OMSTIConverter(), "data/original_corpus/omsti/30", "data/corpus/omsti.xml", 30, 30, true);
        cutInPieces("data/corpus/omsti.xml", "data/corpus/omsti_part", 5);

        convertOriginalCorpus(new OntonotesConverter(), "data/original_corpus/ontonotes/5.0/data/files/data/english", "data/corpus/ontonotes.xml", 30, 30, true);
        
        convertOriginalCorpus(new Senseval1Converter(), "data/original_corpus/senseval1", "data/corpus/senseval1.xml", 16, 30, false);
        
        convertOriginalCorpus(new Senseval2Converter(), "data/original_corpus/mihalcea/senseval2", "data/corpus/senseval2.xml", 171, 30, false);
        
        convertOriginalCorpus(new Senseval3Task1Converter(), "data/original_corpus/mihalcea/senseval3", "data/corpus/senseval3task1.xml", 171, 30, false);
        
        convertOriginalCorpus(new Semeval2007Task7Converter(), "data/original_corpus/semeval/2007/task7", "data/corpus/semeval2007task7.xml", 21, 30, false);
        
        convertOriginalCorpus(new Semeval2007Task17Converter(), "data/original_corpus/semeval/2007/task17", "data/corpus/semeval2007task17.xml", 21, 30, false);
        
        convertOriginalCorpus(new Semeval2013Task12Converter(), "data/original_corpus/semeval/2013/task12", "data/corpus/semeval2013task12.xml", 30, 30, false);
        
        convertOriginalCorpus(new Semeval2015Task13Converter(), "data/original_corpus/semeval/2015/task13", "data/corpus/semeval2015task13.xml", 30, 30, false);
        
    }

    private static void convertOriginalCorpus(CorpusConverter converter, String originalCorpusPath, String newCorpusPath, int originalWordnetVersion, int newWordnetVersion, boolean mergeDuplicateSentences)
    {
        convertCorpus(converter, originalCorpusPath, newCorpusPath, originalWordnetVersion);
        cleanWords(newCorpusPath);
        removeEmptyWords(newCorpusPath);
        removeEmptySentences(newCorpusPath);
        if (mergeDuplicateSentences)
        {
            mergeDuplicatedSentences(newCorpusPath, originalWordnetVersion, newWordnetVersion);
        }
        removeInvalidWordnetAnnotations(newCorpusPath, originalWordnetVersion);
        convertWordnetAnnotations(newCorpusPath, originalWordnetVersion, newWordnetVersion);
        addStanfordPOSAnnotations(newCorpusPath);
        setLemmaAndPOSAnnotationsFromFirstSenseAnnotations(newCorpusPath, newWordnetVersion);
        addWNMorphyLemmaAnnotations(newCorpusPath, newWordnetVersion);
        removeSenseTagsWhereLemmaOrPOSDiffers(newCorpusPath, newWordnetVersion);
        reorganizeWordAnnotations(newCorpusPath);
    }
    
    private static void convertCorpus(CorpusConverter converter, String originalCorpusPath, String newCorpusPath, int wordnetVersion)
    {
        System.out.println("[" + newCorpusPath + "] Converting...");
        converter.convert(originalCorpusPath, newCorpusPath, wordnetVersion);
    }

    private static void cleanWords(String corpusPath)
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
    
    private static void removeEmptyWords(String corpusPath)
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

    private static void removeEmptySentences(String corpusPath)
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

    private static void removeInvalidWordnetAnnotations(String corpusPath, int wnVersion)
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

    private static void convertWordnetAnnotations(String inputPath, int wnVersionIn, int wnVersionOut)
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

    private static void setLemmaAndPOSAnnotationsFromFirstSenseAnnotations(String inputPath, int wordnetVersion)
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

    private static void addStanfordPOSAnnotations(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Adding POS annotations with Stanford POS Tagger...");

        if (tagger == null) tagger = initStanfordPOSTagger();
        
        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                List<TaggedWord> stanfordSentence = tagger.tagSentence(toStanfordSentence(sentence));
                assert (stanfordSentence.size() != sentence.getWords().size());
                for (int i = 0; i < stanfordSentence.size(); i++)
                {
                    Word word = sentence.getWords().get(i);
                    String pos = word.getAnnotationValue("pos");
                    if (!pos.isEmpty()) continue;
                    pos = stanfordSentence.get(i).tag();
                    word.setAnnotation("pos", pos);
                }
            }
        };
        inout.load(corpusPath);
    }
    
    private static MaxentTagger initStanfordPOSTagger()
    {
        StdOutStdErr.stfu();
        MaxentTagger tagger = new MaxentTagger("data/stanford/model/english.tagger");
        StdOutStdErr.speak();
        return tagger;
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

    private static void addWNMorphyLemmaAnnotations(String corpusPath, int wnVersion)
    {
        System.out.println("[" + corpusPath + "] Adding lemma annotations with WN " + wnVersion + " morphy...");
        
        WordnetHelper wn = WordnetHelper.wn(wnVersion);
        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                for (Word word : sentence.getWords())
                {
                    String lemma = word.getAnnotationValue("lemma");
                    if (!lemma.isEmpty()) continue;
                    String pos = POSConverter.toWNPOS(word.getAnnotationValue("pos"));
                    if (pos.equals("x")) continue;
                    word.setAnnotation("lemma", wn.morphy(word.getValue(), pos));
                }
            }
        };
        inout.load(corpusPath);
    }

    private static void removeSenseTagsWhereLemmaOrPOSDiffers(String corpusPath, int wordnetVersion)
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
    
    private static void reorganizeWordAnnotations(String corpusPath)
    {
        System.out.println("[" + corpusPath + "] Reorganizing word annotations...");

        if (wordAnnotationOrder == null) wordAnnotationOrder = initMapAnnotationsOrder();
        
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

    private static Map<String, Integer> initMapAnnotationsOrder()
    {
        Map<String, Integer> annotationsOrder = new HashMap<>();
        annotationsOrder.put("surface_form", 0);
        annotationsOrder.put("lemma", 1);
        annotationsOrder.put("pos", 2);
        annotationsOrder.put("wn16_key", 3);
        annotationsOrder.put("wn17_key", 4);
        annotationsOrder.put("wn171_key", 5);
        annotationsOrder.put("wn20_key", 6);
        annotationsOrder.put("wn21_key", 7);
        annotationsOrder.put("wn30_key", 8);
        annotationsOrder.put("wn31_key", 9);
        return annotationsOrder;
    }

    private static void mergeDuplicatedSentences(String inputPath, int... wnVersionToKeep)
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

    private static void cutInPieces(String inputPath, String outputPath, int piecesCount)
    {
        System.out.println("[" + inputPath + "] Cutting into " + piecesCount + " pieces...");
        
        Wrapper<Integer> totalSentenceCount = new Wrapper<>(0);
        
        StreamingCorpusReaderSentence in = new StreamingCorpusReaderSentence()
        {
            @Override
            public void readSentence(Sentence s)
            {
                totalSentenceCount.obj += 1;
            }
        };

        in.load(inputPath);

        int sentencePerPieces = totalSentenceCount.obj / piecesCount;
        int remainingSentences = totalSentenceCount.obj % piecesCount;
        int finalSentencePerPieces = sentencePerPieces + ((remainingSentences == 0) ? 0 : 1);

        Wrapper<Integer> currentSentenceCount = new Wrapper<>(finalSentencePerPieces);
        Wrapper<Integer> currentPart = new Wrapper<>(0);
        Wrapper<StreamingCorpusWriterSentence> out = new Wrapper<>(null);
        
        in = new StreamingCorpusReaderSentence()
        {
            @Override
            public void readSentence(Sentence s)
            {
                if (currentSentenceCount.obj >= finalSentencePerPieces)
                {
                    if (out.obj != null) out.obj.close();
                    currentSentenceCount.obj = 0;
                    out.obj = new StreamingCorpusWriterSentence();
                    out.obj.open(outputPath + currentPart.obj + ".xml");
                    currentPart.obj += 1;
                }
                out.obj.writeSentence(s);
                currentSentenceCount.obj += 1;
            }
        };
        
        in.load(inputPath);
        out.obj.close();

        File.removeFile(inputPath);
    }
}
