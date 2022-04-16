package getalp.wsd.ufsac.scripts;
import getalp.wsd.ufsac.converter.*;
import getalp.wsd.ufsac.utils.OriginalCorpusConverter;

public class ConvertOriginalCorpora
{
    /**
     * Usage: java getalp.wsd.ufsac.scripts.ConvertOriginalCorpora
     * 
     * Note that you must manually remove the lines of code that you do not need if you want to convert specific corpora.
     * This is just an example of script that was used to produce all the corpora described in the article.
     */
    public static void main(String[] args) throws Exception
    {
        OriginalCorpusConverter converter = new OriginalCorpusConverter(30);
        
        converter.convert(new Senseval2LexicalSampleConverter("train"), "data/original_corpus/senseval2/english-lex-sample", "data/corpus/senseval2_lexical_sample_train.xml", 17, false);

        converter.convert(new Senseval2LexicalSampleConverter("test"), "data/original_corpus/senseval2/english-lex-sample", "data/corpus/senseval2_lexical_sample_test.xml", 17, false);

        converter.convert(new MihalceaSensevalConverter("d00", "d01", "d02"), "data/original_corpus/mihalcea/senseval2", "data/corpus/senseval2.xml", 171, false);

        converter.convert(new MihalceaSensevalConverter("d000", "d001", "d002"), "data/original_corpus/mihalcea/senseval3", "data/corpus/senseval3task1.xml", 171, false);
        
        converter.convert(new Semeval2007Task7Converter(), "data/original_corpus/semeval/2007/task7", "data/corpus/semeval2007task7.xml", 21, false);
        
        converter.convert(new Semeval2007Task17Converter(), "data/original_corpus/semeval/2007/task17", "data/corpus/semeval2007task17.xml", 21, false);
        
        converter.convert(new Semeval2013Task12Converter(), "data/original_corpus/semeval/2013/task12", "data/corpus/semeval2013task12.xml", 30, false);
        
        converter.convert(new Semeval2015Task13Converter(), "data/original_corpus/semeval/2015/task13", "data/corpus/semeval2015task13.xml", 30, false);

        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/senseval2/senseval2", "data/corpus/raganato_senseval2.xml", 30, false);
        
        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/senseval3/senseval3", "data/corpus/raganato_senseval3.xml", 30, false);
        
        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2007/semeval2007", "data/corpus/raganato_semeval2007.xml", 30, false);
        
        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2013/semeval2013", "data/corpus/raganato_semeval2013.xml", 30, false);
        
        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/semeval2015/semeval2015", "data/corpus/raganato_semeval2015.xml", 30, false);
        
        converter.convert(new RaganatoUnifiedConverter(), "data/original_corpus/raganato/ALL/ALL", "data/corpus/raganato_ALL.xml", 30, false);

        converter.convert(new SemcorConverter(), "data/original_corpus/semcor", "data/corpus/semcor.xml", 16, false);
        
        converter.convert(new DSOConverter(), "data/original_corpus/dso", "data/corpus/dso.xml", 16, true);
        
        converter.convert(new WNGTConverter(), "data/wordnet/30/glosstag", "data/corpus/wngt.xml", 30, false);
        
        converter.convert(new MASCConverter(), "data/original_corpus/google/masc", "data/corpus/masc.xml", 30, true);

        converter.convert(new OMSTIConverter(), "data/original_corpus/omsti/30", "data/corpus/omsti.xml", 30, true);

        converter.convert(new OntonotesConverter(), "data/original_corpus/ontonotes/5.0/data/files/data/english", "data/corpus/ontonotes.xml", 30, true);

    }
}
