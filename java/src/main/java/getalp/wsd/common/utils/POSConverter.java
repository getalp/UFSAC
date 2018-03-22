package getalp.wsd.common.utils;

public class POSConverter
{
    public static String toWNPOS(String anyPOS)
    {
        anyPOS = anyPOS.toLowerCase();
        if (anyPOS.startsWith("n")) return "n";
        if (anyPOS.startsWith("v")) return "v";
        if (anyPOS.startsWith("r") || anyPOS.startsWith("adv")) return "r";
        if (anyPOS.startsWith("j") || anyPOS.startsWith("a")) return "a";
        return "x";
    }
    
    public static String toWNPOS(int wordnetPOS)
    {
        if (wordnetPOS == 1) return "n";
        if (wordnetPOS == 2) return "v";
        if (wordnetPOS == 3) return "a";
        if (wordnetPOS == 4) return "r";
        if (wordnetPOS == 5) return "a";
        return "x";
    }
    
    public static String toPTBPOS(String wordnetPOS)
    {
        wordnetPOS = toWNPOS(wordnetPOS);
        if (wordnetPOS.equals("n")) return "NN";
        if (wordnetPOS.equals("v")) return "VB";
        if (wordnetPOS.equals("a")) return "JJ";
        if (wordnetPOS.equals("r")) return "RB";
        return "";
    }
}
