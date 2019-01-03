package getalp.wsd.ufsac.scripts;

import getalp.wsd.ufsac.converter.RaganatoUnifiedConverterInverse;
import getalp.wsd.common.utils.ArgumentParser;

public class ConvertToRaganato
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input");
        parser.addArgument("output");
        if (!parser.parse(args, true)) return;

        String inputPath = parser.getArgValue("input");
        String outputPath = parser.getArgValue("output");

        new RaganatoUnifiedConverterInverse().convert(inputPath, outputPath, 30);
    }
}
