package getalp.wsd.ufsac.scripts;

import getalp.wsd.common.utils.ArgumentParser;
import getalp.wsd.common.utils.Wrapper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.modifier.StreamingCorpusModifierSentence;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusReaderWord;
import getalp.wsd.ufsac.utils.CorpusPOSTagger;

import java.util.ArrayList;
import java.util.List;

public class EvaluateWSD
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("corpus");
        parser.addArgument("reference_tag", "wn30_key");
        parser.addArgument("hypothesis_tag", "wsd");
        if (!parser.parse(args, true)) return;

        String corpusPath = parser.getArgValue("corpus");
        String referenceSenseTag = parser.getArgValue("reference_tag");
        String candidateSenseTag = parser.getArgValue("hypothesis_tag");

        Wrapper<Integer> total = new Wrapper<>(0);
        Wrapper<Integer> good = new Wrapper<>(0);
        Wrapper<Integer> bad = new Wrapper<>(0);

        StreamingCorpusReaderWord reader = new StreamingCorpusReaderWord()
        {
            @Override
            public void readWord(Word word)
            {
                List<String> referenceSenseKeys = word.getAnnotationValues(referenceSenseTag, ";");
                if (referenceSenseKeys.isEmpty()) return;

                total.obj += 1;

                String candidateSenseKey = word.getAnnotationValue(candidateSenseTag);
                if (candidateSenseKey.isEmpty()) return;

                bad.obj += 1;
                for (String refSenseKey : referenceSenseKeys)
                {
                    if (refSenseKey.equals(candidateSenseKey))
                    {
                        good.obj += 1;
                        bad.obj -= 1;
                        break;
                    }
                }
            }
        };

        reader.load(corpusPath);

        int attempted = good.obj + bad.obj;
        int missed = total.obj - attempted;

        double coverage = ratioPercent(total.obj - missed, total.obj);
        double recall = ratioPercent(good.obj, total.obj);
        double precision = ratioPercent(good.obj, total.obj - missed);
        double f1 = 2.0 * ((precision * recall) / (precision + recall));

        System.out.println("Total: " + total.obj);
        System.out.println("Good: " + good.obj);
        System.out.println("Bad: " + bad.obj);
        System.out.println("Attempted: " + attempted);
        System.out.println("Missed: " + missed);
        System.out.println("Coverage: " + coverage);
        System.out.println("Recall: " + recall);
        System.out.println("Precision: " + precision);
        System.out.println("F1: " + f1);
    }

    private static double ratioPercent(double num, double den)
    {
        return (num / den) * 100;
    }
}
