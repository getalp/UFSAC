package getalp.wsd.ufsac.core;

import java.util.ArrayList;
import java.util.List;
import getalp.wsd.ufsac.streaming.reader.StreamingCorpusFullReader;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusFullWriter;

public class Corpus extends ParentLexicalEntity
{
	public Corpus()
	{
	    
	}

    public void addDocument(Document document)
    {
        addChild(document);
    }

    public void addDocuments(List<Document> documents)
    {
        addChildren(documents);
    }
    
	public List<Document> getDocuments()
	{
		return getChildren();
	}

    public List<Sentence> getSentences()
    {
        List<Sentence> sentences = new ArrayList<>();
        for (Document d : getDocuments())
        {
            sentences.addAll(d.getSentences());
        }
        return sentences;
    }
    
    public List<Word> getWords()
    {
        List<Word> words = new ArrayList<>();
        for (Document d : getDocuments())
        {
            words.addAll(d.getWords());
        }
        return words;
    }
    
    public static Corpus loadFromXML(String path)
    {
        return new StreamingCorpusFullReader().load(path);
    }

    public static Corpus loadFromXML(String[] paths)
    {
        Corpus whole = new Corpus();
        for (String path : paths)
        {
            Corpus part = loadFromXML(path);
            whole.addDocuments(part.getDocuments());
        }
        return whole;
    }
    
	public void saveToXML(String path)
	{
	    StreamingCorpusFullWriter out = new StreamingCorpusFullWriter();
	    out.open(path);
	    out.writeCorpus(this);
	    out.close();
	}
}