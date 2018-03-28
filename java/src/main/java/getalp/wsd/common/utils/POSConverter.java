package getalp.wsd.common.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    
    public static String toPTBPOS(String anyPOS)
    {
        anyPOS = toWNPOS(anyPOS);
        if (anyPOS.equals("n")) return "NN";
        if (anyPOS.equals("v")) return "VB";
        if (anyPOS.equals("a")) return "JJ";
        if (anyPOS.equals("r")) return "RB";
        return "";
    }
    
    private static final Set<String> allPTBPOS = new HashSet<>(Arrays.asList("CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", 
                                                                             "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", 
                                                                             "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", 
                                                                             "WDT", "WP", "WP$", "WRB"));
    
    public static boolean isPTBPOS(String anyPOS)
    {
        return allPTBPOS.contains(anyPOS);
    }
}
