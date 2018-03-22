package getalp.wsd.common.utils;

import java.util.regex.Pattern;

public class RegExp
{
    public static final Pattern nonLetterPattern = Pattern.compile("[^\\p{IsAlphabetic}]");

    public static final Pattern anyWhiteSpaceGrouped = Pattern.compile("\\s+");

    public static final Pattern nonLetterWhiteSpaceOrDigit = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsWhite_Space}\\p{IsDigit}]");
    
    public static final Pattern digitGrouped = Pattern.compile("[\\p{IsDigit}]+");

    public static final Pattern invisiblePattern = Pattern.compile("[^\\p{Graph}]");
}
