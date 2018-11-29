package getalp.wsd.ufsac.examples;

import getalp.wsd.ufsac.converter.RaganatoUnifiedConverter;
import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.ufsac.utils.OriginalCorpusConverter;

public class ConvertFromRaganato
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input");
        parser.addArgument("output");
        if (!parser.parse(args, true)) return;

        String inputPath = parser.getArgValue("input");
        String outputPath = parser.getArgValue("output");

        OriginalCorpusConverter converter = new OriginalCorpusConverter(30);

        converter.convert(new RaganatoUnifiedConverter(), inputPath, outputPath, 30, false);
    }
}
