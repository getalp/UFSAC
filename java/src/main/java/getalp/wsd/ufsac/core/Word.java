package getalp.wsd.ufsac.core;

public class Word extends LexicalEntity
{    
    public Word()
    {

    }

	public Word(String value)
	{
		setAnnotation("surface_form", value);
	}
	
	public Word(Sentence parentSentence)
	{
		setParentSentence(parentSentence);
	}
	
	public Word(String value, Sentence parentSentence)
	{
        setAnnotation("surface_form", value);
		setParentSentence(parentSentence);
	}
	
	public void setValue(String value)
	{
	    setAnnotation("surface_form", value);
	}
	
	public String getValue()
	{
		return getAnnotationValue("surface_form");
	}
	
	public void setParentSentence(Sentence parentSentence)
	{
		setParent(parentSentence);
	}
	
	public Sentence getParentSentence()
	{
	    return getParent();
	}
	
	public Document getParentDocument()
	{
	    return getParentSentence().getParentDocument();
	}
	
	public Word clone()
	{
		Word copy = new Word();
		transfertAnnotationsToCopy(copy);
		return copy;
	}
	
	public String toString()
	{
		return getAnnotationValue("surface_form");
	}
}
