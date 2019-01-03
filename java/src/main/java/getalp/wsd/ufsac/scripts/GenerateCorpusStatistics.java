package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;

public class GenerateCorpusStatistics
{
    /**
     * Usage: java getalp.wsd.ufsac.scripts.GenerateCorpusStatistics [corpus]...
     */
    public static void main(String[] corpusPaths) throws Exception
    {
        System.out.println("\\begin{tabular}{l|c|cc|cccc}");
        System.out.println("\\toprule");
        System.out.println("\\multirow{2}{*}{Corpus} & \\multirow{2}{*}{Sentences} & \\multicolumn{2}{c|}{Words} & \\multicolumn{4}{c}{Annotated parts of speech} \\\\");
        System.out.println(" & & Total & Annotated & Nouns & Verbs & Adj. & Adv. \\\\");
        System.out.println("\\midrule");           
  
    	int i = 0;
	    for (String corpusPath : corpusPaths)
	    {
	        Wrapper<Integer> sentenceCount = new Wrapper<>(0);
            Wrapper<Integer> wordCount = new Wrapper<>(0);
            Wrapper<Integer> annotatedWordCount = new Wrapper<>(0);
            Wrapper<Integer> nounWordCount = new Wrapper<>(0);
            Wrapper<Integer> verbWordCount = new Wrapper<>(0);
            Wrapper<Integer> adjectiveWordCount = new Wrapper<>(0);
            Wrapper<Integer> adverbWordCount = new Wrapper<>(0);
	        StreamingCorpusReader reader = new StreamingCorpusReader()
            {
                public void readBeginSentence(Sentence sentence)
                {
                    sentenceCount.obj++;
                }
                
                public void readWord(Word word)
                {
                    wordCount.obj++;
                    if (word.hasAnnotation("wn30_key"))
                    {
                        annotatedWordCount.obj++;
                        if (word.hasAnnotation("pos"))
                        {
                            if (POSConverter.toWNPOS(word.getAnnotationValue("pos")).equals("n"))
                            {
                                nounWordCount.obj++;
                            }
                            else if (POSConverter.toWNPOS(word.getAnnotationValue("pos")).equals("v"))
                            {
                                verbWordCount.obj++;
                            }
                            else if (POSConverter.toWNPOS(word.getAnnotationValue("pos")).equals("a"))
                            {
                                adjectiveWordCount.obj++;
                            }
                            else if (POSConverter.toWNPOS(word.getAnnotationValue("pos")).equals("r"))
                            {
                                adverbWordCount.obj++;
                            }
                        }
                    }
                }
            };

            reader.load(corpusPath);
            
            if ((i % 2) == 1)
            {
                System.out.print("\\rowcolor{gray!10} ");
            }
            i++;
	        
	        System.out.println(corpusPath + " & " + sentenceCount.obj + " & " + wordCount.obj + " & " + annotatedWordCount.obj + " & " + 
	        nounWordCount.obj + " & " + verbWordCount.obj + " & " + adjectiveWordCount.obj + " & " + adverbWordCount.obj + " \\\\");
	    }
        
	    System.out.println("\\bottomrule");
        System.out.println("\\end{tabular}");
    }
}
