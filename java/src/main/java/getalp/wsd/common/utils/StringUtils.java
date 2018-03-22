package getalp.wsd.common.utils;

import java.util.Iterator;

public class StringUtils 
{
	public static String join(Iterable<String> iterable, String separator)
	{
		Iterator<String> iterator = iterable.iterator();
        if (!iterator.hasNext()) return "";
        StringBuilder buf = new StringBuilder();
        buf.append(iterator.next());
        while (iterator.hasNext()) 
        {
            buf.append(separator);
            buf.append(iterator.next());
        }
        return buf.toString();
	}
}
