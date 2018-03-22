package getalp.wsd.ufsac.core;

import java.util.ArrayList;
import java.util.List;

public class Document extends ParentLexicalEntity
{		
	public Document()
	{

	}
	
	public Document(Corpus parentCorpus)
	{
		setParentCorpus(parentCorpus);
	}
	
	public void addParagraph(Paragraph paragraph)
	{
		addChild(paragraph);
	}
	
	public List<Paragraph> getParagraphs()
	{
		return getChildren();
	}
	
	public void setParentCorpus(Corpus parentCorpus)
	{
		setParent(parentCorpus);
	}
	
	public List<Sentence> getSentences()
	{
	    List<Sentence> sentences = new ArrayList<>();
	    for (Paragraph p : getParagraphs())
	    {
	        sentences.addAll(p.getSentences());
	    }
	    return sentences;
	}
	
	public List<Word> getWords()
	{
	    List<Word> words = new ArrayList<>();
	    for (Paragraph p : getParagraphs())
	    {
	        words.addAll(p.getWords());
	    }
	    return words;
	}
}