package getalp.wsd.common.utils;

import java.util.List;

public class Utils
{
    public static boolean isEndOfSentenceMarker(String word)
    {
        if (word.equals(".") || word.equals("?") || word.equals("!"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static int lastIndexOfEndOfSentenceMarker(List<String> words)
    {
        int ret = -1;
        for (int i = words.size() - 1 ; i >= 0 ; i--)
        {
            if (isEndOfSentenceMarker(words.get(i)))
            {
                ret = i;
                break;
            }
        }
        return ret;
    }
    
    public static int firstIndexOfEndOfSentenceMarker(List<String> words)
    {
        int ret = words.size() - 1;
        for (int i = 0 ; i < words.size() ; i++)
        {
            if (isEndOfSentenceMarker(words.get(i)))
            {
                ret = i;
                break;
            }
        }
        return ret;
    }
    
    public static List<String> subListStartingAfterLastEndOfSentenceMarker(List<String> words)
    {
        return words.subList(lastIndexOfEndOfSentenceMarker(words) + 1, words.size());
    }

    
    public static List<String> subListEndingOnFirstEndOfSentenceMarker(List<String> words)
    {
        return words.subList(0, firstIndexOfEndOfSentenceMarker(words) + 1);
    }
}
