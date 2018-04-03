package getalp.wsd.ufsac.utils;

import getalp.wsd.common.utils.File;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderSentence;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

/**
 * This class allows to split a sentence-based corpus into multiple pieces.
 * It used to be applied on huge corpora like the OMSTI.
 */
public class CorpusSplitter
{
    public CorpusSplitter()
    {
        
    }

    public void split(String inputPath, String outputPath, int piecesCount)
    {        
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
