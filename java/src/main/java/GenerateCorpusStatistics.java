
import getalp.wsd.common.utils.POSConverter;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReader;

public class GenerateCorpusStatistics
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("\\begin{tabular}{l|c|cc|cccc}");
        System.out.println("\\toprule");
        System.out.println("\\multirow{2}{*}{Corpus} & \\multirow{2}{*}{Sentences} & \\multicolumn{2}{c|}{Words} & \\multicolumn{4}{c}{Annotated parts of speech} \\\\");
        System.out.println(" & & Total & Annotated & Nouns & Verbs & Adj. & Adv. \\\\");
        System.out.println("\\midrule");           

        //String dirPath = "/home/getalp/hadjsala/arabic-smt-wsd/run_translation/TALN-2018/TA-output/ATB/4corpus/without_proc-ini-moses/";
        
        //String[][] corpuses = new String[][]{{"semcor"}, {"dso"}, {"wngt"}, {"masc"}, {"omsti_part0", "omsti_part1", "omsti_part2", "omsti_part3"}, 
        //    {"ontonotes"}, {"sem7"}, {"sem13"}, {"semeval15task13"}, {"senseval2"}, {"senseval3task1"}};
        /*
        String[][] corpuses = new String[][]{
            {"semcor-ATB.ar.without_proc-ini-moses.xml"}, 
            {"dso-ATB.ar.without_proc-ini-moses.xml"}, 
            {"wngt-ATB.ar.without_proc-ini-moses.xml"}, 
            {"masc-ATB.ar.without_proc-ini-moses.xml"}, 
            {"omsti_part1-ATB.ar.without_proc-ini-moses.xml", "omsti_part2-ATB.ar.without_proc-ini-moses.xml", "omsti_part3-ATB.ar.without_proc-ini-moses.xml"}, 
            {"ontonotes-ATB.ar.without_proc-ini-moses.xml"}, 
            {"semeval2007task7-ATB.ar.without_proc-ini-moses.xml"}, 
            {"semeval2007task17-ATB.ar.without_proc-ini-moses.xml"}, 
            {"sem13-ATB.ar.without_proc-ini-moses.xml"}, 
            {"semeval15task13-ATB.ar.without_proc-ini-moses.xml"}};
        */      
        String[][] corpuses = new String[][] {
                {"data/arabic/semcor-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/dso-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/wngt-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/masc-ATB.ar.alignment_info.ar.post-proc.xml"},
                {
                    "data/arabic/omsti_part0-ATB.ar.alignment_info.ar.post-proc.xml", 
                    "data/arabic/omsti_part1-ATB.ar.alignment_info.ar.post-proc.xml", 
                    "data/arabic/omsti_part2-ATB.ar.alignment_info.ar.post-proc.xml", 
                    "data/arabic/omsti_part3-ATB.ar.alignment_info.ar.post-proc.xml",
                    "data/arabic/omsti_part4-ATB.ar.alignment_info.ar.post-proc.xml"
                },
                {"data/arabic/ontonotes-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/senseval2-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/senseval3task1-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/semeval2007task7-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/semeval2007task17-ATB.ar.alignment_info.ar.post-proc.xml"},
                {"data/arabic/semeval2013task12-ATB.ar.alignment_info.ar.post-proc.xml"},     
                {"data/arabic/semeval2015task13-ATB.ar.alignment_info.ar.post-proc.xml"}
        };
        
    	int i = 0;
	    for (String[] corpusGroup : corpuses)
	    {
//            String resourceName = corpusGroup.split(";")[0];
            String resourceName = corpusGroup[0];
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
            for (String corpus : corpusGroup)
            {
                reader.load(corpus);
            }
            
            if ((i % 2) == 1)
            {
                System.out.print("\\rowcolor{gray!10} ");
            }
            i++;
	        
	        System.out.println(resourceName + " & " + sentenceCount.obj + " & " + wordCount.obj + " & " + annotatedWordCount.obj + " & " + 
	        nounWordCount.obj + " & " + verbWordCount.obj + " & " + adjectiveWordCount.obj + " & " + adverbWordCount.obj + " \\\\");
	    }
        
	    System.out.println("\\bottomrule");
        System.out.println("\\end{tabular}");
    }
}
