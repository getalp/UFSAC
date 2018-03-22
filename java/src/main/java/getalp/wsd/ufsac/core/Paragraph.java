package getalp.wsd.ufsac.core;

import java.util.ArrayList;
import java.util.List;

public class Paragraph extends ParentLexicalEntity
{
    public Paragraph()
    {
        
    }
	
	public Paragraph(Document parentDocument)
	{
		setParentDocument(parentDocument);
	}

    public void addSentence(Sentence sentence)
    {
        addChild(sentence);
    }

    public void addSentences(List<Sentence> sentences)
    {
        addChildren(sentences);
    }

    public void removeSentence(Sentence sentence)
    {
        removeChild(sentence);
    }

    public void removeAllSentences()
    {
        removeAllChildren();
    }
    
	public List<Sentence> getSentences()
	{
		return getChildren();
	}
	
	public void setParentDocument(Document parentDocument)
	{
		setParent(parentDocument);
	}
	
	public Document getParentDocument()
	{
	    return getParent();
	}
	   
    public List<Word> getWords()
    {
        List<Word> words = new ArrayList<>();
        for (Sentence s : getSentences())
        {
            words.addAll(s.getWords());
        }
        return words;
    }
}