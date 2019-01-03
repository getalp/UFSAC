package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.ufsac.converter.*;
import getalp.wsd.ufsac.utils.OriginalCorpusConverter;

public class ConvertToUFSAC
{
    public static void main(String[] args)
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("input_format");
        parser.addArgument("input_path");
        parser.addArgument("output_path");
        parser.addArgument("input_wn_version");
        parser.addArgument("output_wn_version", "30");
        if (!parser.parse(args, true)) return;

        String inputFormat = parser.getArgValue("input_format");
        String inputPath = parser.getArgValue("input_path");
        String outputPath = parser.getArgValue("output_path");
        int inputWNVersion = parser.getArgValueInteger("input_wn_version");
        int outputWNVersion = parser.getArgValueInteger("output_wn_version");

        OriginalCorpusConverter converter = new OriginalCorpusConverter(outputWNVersion);

        if (inputFormat.equals("semcor"))
        {
            converter.convert(new SemcorConverter(), inputPath, outputPath, inputWNVersion, false);
        }
        else if (inputFormat.equals("dso"))
        {
            converter.convert(new DSOConverter(), inputPath, outputPath, inputWNVersion, true);
        }
        else if (inputFormat.equals("wngt"))
        {
            converter.convert(new WNGTConverter(), inputPath, outputPath, inputWNVersion, false);
        }
        else if (inputFormat.equals("omsti"))
        {
            converter.convert(new OMSTIConverter(), inputPath, outputPath, inputWNVersion, true);
        }
        else if (inputFormat.equals("masc"))
        {
            converter.convert(new MASCConverter(), inputPath, outputPath, inputWNVersion, true);
        }
        else if (inputFormat.equals("ontonotes"))
        {
            converter.convert(new OntonotesConverter(), inputPath, outputPath, inputWNVersion, true);
        }
        else if (inputFormat.equals("raganato"))
        {
            converter.convert(new RaganatoUnifiedConverter(), inputPath, outputPath, inputWNVersion, false);
        }
        else
        {
            System.out.println("Error: input_format must be one of {semcor,dso,wngt,omsti,masc,ontonotes,raganato}.");
        }

    }
}
