import getalp.wsd.common.utils.File;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.ufsac.converter.*;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderSentence;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;
import getalp.wsd.ufsac.utils.OriginalCorpusConverter;

public class ConvertOriginalCorpora
{
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

    private static void convertOriginalCorpus(CorpusConverter formatConverter, String originalCorpusPath, String newCorpusPath, int originalWordnetVersion, int newWordnetVersion, boolean mergeDuplicateSentences)
    {
        OriginalCorpusConverter converter = new OriginalCorpusConverter();
        converter.convert(formatConverter, originalCorpusPath, newCorpusPath, originalWordnetVersion, newWordnetVersion, mergeDuplicateSentences);

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
