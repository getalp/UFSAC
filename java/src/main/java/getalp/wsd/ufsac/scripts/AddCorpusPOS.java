package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import getalp.wsd.ufsac.utils.CorpusLemmatizer;
import getalp.wsd.ufsac.utils.CorpusPOSTagger;

public class AddCorpusPOS
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input");
        parser.addArgument("output");
        if (!parser.parse(args, true)) return;

        String inputPath = parser.getArgValue("input");
        String outputPath = parser.getArgValue("output");

        CorpusPOSTagger posTagger = new CorpusPOSTagger(false, "pos");

        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                posTagger.tag(sentence.getWords());
            }
        };

        inout.load(inputPath, outputPath);
    }
}
