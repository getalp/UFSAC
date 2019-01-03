package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.ufsac.converter.RaganatoUnifiedConverterInverse;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import getalp.wsd.ufsac.utils.CorpusLemmatizer;

public class AddCorpusLemma
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input");
        parser.addArgument("output");
        if (!parser.parse(args, true)) return;

        String inputPath = parser.getArgValue("input");
        String outputPath = parser.getArgValue("output");

        CorpusLemmatizer lemmatizer = new CorpusLemmatizer("lemma", 30);

        StreamingCorpusModifierSentence inout = new StreamingCorpusModifierSentence()
        {
            public void modifySentence(Sentence sentence)
            {
                lemmatizer.tag(sentence.getWords());
            }
        };

        inout.load(inputPath, outputPath);
    }
}
