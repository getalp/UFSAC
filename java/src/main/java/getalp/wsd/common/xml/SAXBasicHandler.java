package getalp.wsd.common.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXBasicHandler extends DefaultHandler
{
    private boolean saveCharacters = false;
    
    private String currentCharacters = "";
    
    public void resetAndStartSaveCharacters()
    {
        resetCurrentCharacters();
        startSaveCharacters();
    }

    public String getAndStopSaveCharacters()
    {
        stopSaveCharacters();
        return getCurrentCharacters();
    }
    
    public void startSaveCharacters()
    {
        saveCharacters = true;
    }
    
    public void stopSaveCharacters()
    {
        saveCharacters = false;
    }
    
    public String getCurrentCharacters()
    {
        return currentCharacters;
    }
    
    public void resetCurrentCharacters()
    {
        currentCharacters = "";
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (saveCharacters)
        {
            currentCharacters += new String(ch, start, length);
        }
    }
}
