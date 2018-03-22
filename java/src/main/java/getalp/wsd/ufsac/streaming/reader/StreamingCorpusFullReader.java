package getalp.wsd.ufsac.streaming.reader;

import getalp.wsd.ufsac.core.Corpus;
import getalp.wsd.ufsac.core.Document;
import getalp.wsd.ufsac.core.Paragraph;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;

public class StreamingCorpusFullReader
{
	public Corpus load(String path) 
    {
	    SequentialReader reader = new SequentialReader();
	    reader.load(path);
	    return reader.currentCorpus;
    }

	private class SequentialReader extends StreamingCorpusReader 
	{
	    private Corpus currentCorpus;
	    
	    private Document currentDocument;
	    
	    private Paragraph currentParagraph;
	    
	    private Sentence currentSentence;
	    
	    private Word currentWord;

	    @Override
	    public void readBeginCorpus(Corpus corpus)
	    {
	        currentCorpus = corpus;
	    }

	    @Override
	    public void readBeginDocument(Document document)
	    {
	        currentDocument = document;
	        currentDocument.setParentCorpus(currentCorpus);
	    }

	    @Override
	    public void readBeginParagraph(Paragraph paragraph)
	    {
	        currentParagraph = paragraph;
	        currentParagraph.setParentDocument(currentDocument);
	    }

	    @Override
	    public void readBeginSentence(Sentence sentence)
	    {
	        currentSentence = sentence;
	        currentSentence.setParentParagraph(currentParagraph);
	    }

	    @Override
	    public void readWord(Word word)
	    {
	        currentWord = word;
	        currentWord.setParentSentence(currentSentence);
	    }
	}
}
