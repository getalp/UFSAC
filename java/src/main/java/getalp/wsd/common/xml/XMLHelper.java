package getalp.wsd.common.xml;

public class XMLHelper
{
    private static final int indentSize = 4;
    
    public static String getIndent(int indentLevel)
    {
        String indent = "";
        for (int i = 0 ; i < indentSize * indentLevel ; i++)
        {
            indent += ' ';
        }
        return indent;
    }

    public static String toValidXMLEntity(String value)
    {
        String valueCleaned = value;
        valueCleaned = valueCleaned.replace("&", "&amp;");
        valueCleaned = valueCleaned.replace("<", "&lt;");
        valueCleaned = valueCleaned.replace(">", "&gt;");
        valueCleaned = valueCleaned.replace("'", "&apos;");
        valueCleaned = valueCleaned.replace("\"", "&quot;");
        return valueCleaned;
    }
    
    public static String fromValidXMLEntity(String value)
    {
        String valueCleaned = value;
        valueCleaned = valueCleaned.replace("&amp;", "&");
        valueCleaned = valueCleaned.replace("&lt;", "<");
        valueCleaned = valueCleaned.replace("&gt;", ">");
        valueCleaned = valueCleaned.replace("&apos;", "'");
        valueCleaned = valueCleaned.replace("&quot;", "\"");
        return valueCleaned;
    }
}
