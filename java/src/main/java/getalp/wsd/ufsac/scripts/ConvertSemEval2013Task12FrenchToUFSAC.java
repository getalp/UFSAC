package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.ufsac.converter.*;
import getalp.wsd.ufsac.utils.OriginalCorpusConverter;

public class ConvertSemEval2013Task12FrenchToUFSAC
{
    public static void main(String[] args)
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input_corpus");
        parser.addArgument("input_keys");
        parser.addArgument("output_corpus");
        if (!parser.parse(args, true)) return;

        String inputCorpusPath = parser.getArgValue("input_corpus");
        String inputKeysPath = parser.getArgValue("input_keys");
        String outputCorpusPath = parser.getArgValue("output_corpus");
        int oldWordNetVersion = 30;
        int newWordNetVersion = 30;

        new Semeval2013Task12Converter("fr").convert(inputCorpusPath, inputKeysPath, outputCorpusPath, oldWordNetVersion);

        OriginalCorpusConverter.cleanWords(outputCorpusPath);
        OriginalCorpusConverter.removeEmptyWords(outputCorpusPath);
        OriginalCorpusConverter.removeEmptySentences(outputCorpusPath);
        OriginalCorpusConverter.normalizePunctuation(outputCorpusPath);
        OriginalCorpusConverter.removeInvalidWordnetAnnotations(outputCorpusPath, oldWordNetVersion);
        OriginalCorpusConverter.convertWordnetAnnotations(outputCorpusPath, oldWordNetVersion, newWordNetVersion);
        OriginalCorpusConverter.reorganizeWordAnnotations(outputCorpusPath);

    }
}
