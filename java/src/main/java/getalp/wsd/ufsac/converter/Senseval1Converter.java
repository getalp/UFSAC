package getalp.wsd.ufsac.converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.streaming.writer.StreamingCorpusWriterSentence;

public class Senseval1Converter  implements UFSACConverter
{
	private WordnetHelper wn;
	
	private HashMap<String,String> WordSenses;
	
	private HashMap<String ,String> uidWordSenses;
	
    private StreamingCorpusWriterSentence out;

    @Override
    public void convert(String inputPath, String outputPath, int wnVersion)
    {
    	
    	WordnetHelper.useWNGT = false;
        wn = WordnetHelper.wn(wnVersion);
        out = new StreamingCorpusWriterSentence();
        out.open(outputPath);       
    	try {			
    		processTrainList(inputPath , inputPath + "/train_list.txt");
    		processTestList(inputPath, inputPath + "/test_list.txt");
		} catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    	out.close();
    }
    
    private void loadWordSenseFromDic(String lemma, String dicPath)throws IOException{
    	BufferedReader br = new BufferedReader(new FileReader(dicPath));
    	uidWordSenses = new HashMap<>();
    	String ord = null;
		String uid = null;
		String pos = null;
		boolean uidTag = false, posTag = false;
    	for (String line = br.readLine() ; line != null ; line = br.readLine())
        {
    		
    		if(line.startsWith("<sen")){
    			uidTag = true;
    			String[] splitedSenseTag = line.split("\\s+");
    			
    			
    			for(int i = 1; i < splitedSenseTag.length ; i++){
    				if(splitedSenseTag[i].startsWith("uid="))
    				{
    					uid = splitedSenseTag[i].substring(4, splitedSenseTag[i].length());	
    				}
    				else if(splitedSenseTag[i].startsWith("ord="))
    				{
    					if(!splitedSenseTag[i].contains(">")){
    						ord = splitedSenseTag[i].substring(4, splitedSenseTag[i].length() );
    					}else{
    						ord = splitedSenseTag[i].substring(4, splitedSenseTag[i].indexOf(">") );
    					}
    					//enleve tous les caractere non decimal (sauf le ".")
    					ord = ord.replaceAll("[^\\d.]", "");
    				}
    			}    			
    			
    		}else if(line.startsWith("<gr>")){
    			posTag = true;
    			pos = line.substring(line.indexOf("<gr>") + 4, line.indexOf("</gr>"));
    			if(pos.contains("/")){
    				pos = pos.substring(pos.indexOf("/") + 1, pos.length());    			
    			}
    			pos = convertPos(pos);    			
    			
    		}
    		if(uidTag && posTag){
    			uidTag = false; posTag = false;
    			if(ord != null){
    				if(ord.contains(".")){
    					String[] splitedOrd = ord.split("[.]");    					
    					String value = null; 
    					for(String tmpOrd : splitedOrd){    						
    						if(value == null){
    							value = lemma + "%" + pos + "#" + tmpOrd;
    						}else{
    							value += ";" + lemma + "%" + pos + "#" + tmpOrd;
    						}
    					}    				
    					uidWordSenses.put(uid, value);
    				}else{
    					uidWordSenses.put(uid, lemma + "%" + pos + "#" + ord);
    				}
    			}
    		}
        }
        br.close();
    }
    
    private void processTrainList(String rootPath, String listPath) throws IOException{
    	     	
    	BufferedReader br = new BufferedReader(new FileReader(listPath));
        for (String line = br.readLine() ; line != null ; line = br.readLine())
        {
        	loadWordSenseFromDic(line, rootPath + "/DICT/" + line + ".dic");
        	processTrainWordInList(rootPath, line);
        }
        br.close();
    }
    
    
    private void processTrainWordInList(String wordPath, String listPath) throws IOException{
    	
    
    	BufferedReader brTrain = new BufferedReader(new FileReader(wordPath + "/TRAIN/" + listPath + ".cor"));
        for (String line = brTrain.readLine() ; line != null ; line = brTrain.readLine())
        {
            if (line.isEmpty() || line.trim().length() == 6) {continue; }
            else{             	
            	processTrainSentenceInWord(line);
            }            
        }
        brTrain.close();
        
    }
    
    private void processTrainSentenceInWord(String sentence){
    	String[] tokens = sentence.split("\\s+");
        Sentence resultingSentence = new Sentence();
        for(int i = 0 ; i < tokens.length; i++){
        	if(tokens[i].startsWith("<tag") && i+1 < tokens.length && tokens[i+1].contains("</>")){
        		i = i + 1;
        		String uid = tokens[i].substring(1, 7);
        		if(!uidWordSenses.containsKey(uid)){
        			Word currentWord = new Word(resultingSentence);
        			String surfaceform = tokens[i].substring(tokens[i].indexOf(">") + 1, tokens[i].indexOf("</>"));        			
        			currentWord.setValue(surfaceform);                    
        		}else{
        			Word currentWord = new Word(resultingSentence);
        			String surfaceform = tokens[i].substring(tokens[i].indexOf(">") + 1, tokens[i].indexOf("</>"));        			
        			currentWord.setValue(surfaceform);
        			String senseTag = uidWordSenses.get(uid);
        			String lemma = senseTag.split("%")[0];
        			String pos = senseTag.split("%")[1].substring(0, 1);
        			currentWord.setAnnotation("lemma", lemma);        			
        			currentWord.setAnnotation("pos", pos);
        			String senseKey = convertSenseKeyFromSenseNumber(senseTag);
        			if(senseKey != null){
        				currentWord.setAnnotation("wn" + wn.getVersion() + "_key", senseKey);
        			}
        		}
        		
        	}else if(tokens[i].startsWith("<tag") && i + 3 < tokens.length && tokens[i + 2].equals("or")){
        		ArrayList<String> uids = new ArrayList<>();
        		uids.add(tokens[i + 1].substring(1, 7));
        		uids.add(tokens[i + 3].substring(0, 6));
        		i = i + 3;
        		Word currentWord = new Word(resultingSentence);
    			String surfaceform = tokens[i].substring(tokens[i].indexOf(">") + 1, tokens[i].indexOf("</>"));        			
    			currentWord.setValue(surfaceform);
        		
    			String senseTag = null;
    			for(String sense :  uidWordSenses.get(uids.get(0)).split(";")){
    				if(senseTag == null){
    					senseTag = sense;
    				}else if(!senseTag.contains(sense)){
    					senseTag += ";" + sense;
    				}
    			}
        		
    			String lemma = senseTag.split("%")[0];
    			String pos = senseTag.split("%")[1].substring(0, 1);
    			currentWord.setAnnotation("lemma", lemma);        			
    			currentWord.setAnnotation("pos", pos);
    			String senseTag2 = uidWordSenses.get(uids.get(1));
    			for(String sense : senseTag2.split(";")){
    				if(!senseTag.contains(sense)){    					
    					senseTag += ";" + sense;
    				}
    			}
    			String senseKey = convertSenseKeyFromSenseNumber(senseTag);
    			if(senseKey != null){
    				currentWord.setAnnotation("wn" + wn.getVersion() + "_key", senseKey);
    			}    			
        	}else{
        		Word currentWord = new Word(resultingSentence);
                currentWord.setValue(tokens[i]);
        	}
        }
        out.writeSentence(resultingSentence);
    }
    
    private void loadHectorMappingWn16(String path) throws IOException
    {
    	WordSenses = new HashMap<>();
    	BufferedReader br = new BufferedReader(new FileReader(path));
    	for (String line = br.readLine() ; line != null ; line = br.readLine())
        {
    		String lineTrimed = line.trim();    		
    		if(lineTrimed.isEmpty()) continue;
    		if(lineTrimed.startsWith("***")) continue;
    		String[] tokens = lineTrimed.split(":");
    		if(tokens.length == 1) continue;
    		if(tokens.length >=3)
    		{
    			String[] synonymes = tokens[2].split("\\s+");
    			for(int i = 0 ; i < synonymes.length; i++){
    				if(synonymes[i].equals("NOT") && i + 1 < synonymes.length && synonymes[i+1].equals("SURE")) break;    				
    				if(synonymes[i].equals("or") || synonymes[i].isEmpty()) continue;
    				if(!WordSenses.containsKey(synonymes[i])){
    					String[] splitedFirstToken = tokens[0].split("_"); 
    					String senseNumber = splitedFirstToken[0];
    					String lemma = splitedFirstToken[1];
    					WordSenses.put(synonymes[i], lemma + "%" + tokens[1].substring(1, 2) + "#" + senseNumber);
    			
    				}else{
    					String[] splitedFirstToken = tokens[0].split("_"); 
    					String senseNumber = splitedFirstToken[0];
    					String lemma = splitedFirstToken[1];
    					String multiSensesKey = WordSenses.get(synonymes[i]) + ";" + lemma + "%" + tokens[1].substring(1, 2) + "#" + senseNumber;
    					WordSenses.put(synonymes[i], multiSensesKey);
    				}
    			}
    		}
    	}  
    	br.close();
    }

    private void processTestList(String rootPath, String listPath) throws IOException
    {
    	loadHectorMappingWn16(rootPath + "/hector.txt");
        BufferedReader br = new BufferedReader(new FileReader(listPath));
        for (String line = br.readLine() ; line != null ; line = br.readLine())
        {
        	processTestWordInList(rootPath, line);
        }
        br.close();
    }
    
    private void processTestWordInList(String wordPath, String lemmaPos) throws IOException
    {        
    	String wordSenseKey = null;
    	BufferedReader brGold = new BufferedReader(new FileReader(wordPath + "/GOLD/" + lemmaPos));
        BufferedReader brEval = new BufferedReader(new FileReader(wordPath + "/TEST/" + lemmaPos + ".eval"));
        for (String line = brEval.readLine() ; line != null ; line = brEval.readLine())
        {
            if (line.isEmpty()) continue;
            if(line.trim().length() == 6){ 
            	String goldLine = brGold.readLine();
            	if(goldLine == null) break;            	            
            	wordSenseKey = getWordSenseKeyFromMap(goldLine);
            	continue; 
            }
            processTestSentenceInWord(line, wordSenseKey);
        }
        brGold.close();
        brEval.close();
    }
    
    private String getWordSenseKeyFromMap(String line){
    	String wordSenseKey = null;    	         
    	String[] splitedGoldLine = line.split(":");
    	if(splitedGoldLine[1].split("\\s+").length == 1){
    		wordSenseKey = WordSenses.get(splitedGoldLine[1]);    		
    	}else{
    		for(String word : splitedGoldLine[1].split("\\s+") ){
    			if(word.equals("or")) continue;
    			else{    				
    				if(WordSenses.containsKey(word)){
    					String[] splitedWordSenseKey = WordSenses.get(word).split(";");
    					for(String s : splitedWordSenseKey){
    						if(wordSenseKey == null){
    							wordSenseKey = s;
    						}else if(!wordSenseKey.contains(s)){
    							wordSenseKey += ";" + s ;
    						}
    					}
    				}
    			}
    		}
    	}
    	return wordSenseKey;
    }
    
    private void processTestSentenceInWord(String sentence, String wordSenseKey)
    {
      
    	String[] tokens = sentence.split("\\s+");
        Sentence resultingSentence = new Sentence();
        for (int i = 0 ; i < tokens.length ; i++)
        {           
        	if(tokens[i].startsWith("<tag>") || tokens[i].endsWith("</>")){        		
        		String surfaceform = null;
    			if(tokens[i].endsWith("</>")){
    				int indexStart = tokens[i].indexOf("<tag>");
    				surfaceform = tokens[i].substring(indexStart + 5, tokens[i].length()-3);
    			}else{
    				int indexEnd = tokens[i].indexOf("</>");        			
    				surfaceform = tokens[i].substring(5, indexEnd);        			
    			}
    			String pos, lemma;
        		if(wordSenseKey != null){        			        		
        			Word currentWord = new Word(resultingSentence);
        			String[] splitedWordSenseKey = wordSenseKey.split(";");
        			lemma = splitedWordSenseKey[0].split("%")[0];
        			pos = splitedWordSenseKey[0].split("%")[1].substring(0, 1);
        			currentWord.setValue(surfaceform);
        			boolean isMultiplLemma = false;
        			String lemmaFirst = null;
        			Set<String> lemmas = new HashSet<String>();
        			for(String s : wordSenseKey.split(";")){
        				if(lemmaFirst == null){
        					lemmaFirst = s.split("%")[0];
        					lemmas.add(lemmaFirst);
        				}else{
        					if(!lemmaFirst.equals(s.split("%")[0])){
        						isMultiplLemma = true;
        						lemmas.add(s.split("%")[0]);
        					}
        				}
        			}
        			if(isMultiplLemma){
        				for(String lem : lemmas){
	        				if(surfaceform.toLowerCase().equals(lem)){
	        					lemma = lem;
	        					isMultiplLemma = false;
	        					break;
	        				}
	        			}
        			}
        			if(isMultiplLemma){
	        			for(String lem : lemmas){
	        				if(surfaceform.toLowerCase().contains(lem)){
	        					lemma = lem;
	        					isMultiplLemma = false;
	        					break;
	        				}
	        			}
        			}
        			if(isMultiplLemma){  
        				int maxScore = 0;        				
        				for(String lem : lemmas){
        					int currentScore = compareTo(surfaceform,lem);
        					if(maxScore < currentScore){
        						lemma = lem;    
        						maxScore = currentScore;
        						break;        						
        					}
	        			}
        				isMultiplLemma = false;        				
        			}
        			currentWord.setAnnotation("lemma", lemma);        			
        			currentWord.setAnnotation("pos", pos);    
        			String uniqueLemmaWordSenseKey = null;
        			for(String s : splitedWordSenseKey){
        				if(s.contains(lemma) && uniqueLemmaWordSenseKey == null){
        					uniqueLemmaWordSenseKey = s;
        				}else if(s.split("%")[0].equals(lemma)){
        					uniqueLemmaWordSenseKey += ";" + s;
        				}
        			}
        			String senseKey = convertSenseKeyFromSenseNumber(uniqueLemmaWordSenseKey);
        			if(senseKey != null){
        				currentWord.setAnnotation("wn" + wn.getVersion() + "_key", senseKey);
        			}
                    
        		}
        		else
        		{        			
        			Word currentWord = new Word(resultingSentence);
        			currentWord.setValue(surfaceform);                 
        		}
        	}
            else 
            {
                Word currentWord = new Word(resultingSentence);
                currentWord.setValue(tokens[i]);
            }
        } 
        out.writeSentence(resultingSentence);
        
    }
    private int compareTo(String s1, String s2){
    	int cmp = 0;
    	for(int i = 0 ; i < s1.length() ; i ++ ){
    		if(s2.contains(String.valueOf(s1.charAt(i)))){
    			cmp++;
    		}
    	}
    	return cmp;
    }
    
    private String convertSenseKeyFromSenseNumber(String senseNumbers){
    	String senseKey = null;
    	for(String senseNumber : senseNumbers.split(";")){
    		if(senseKey == null){
    			senseKey = wn.getSenseKeyFromSenseNumber(senseNumber);
    		}else{
    			String tmp = wn.getSenseKeyFromSenseNumber(senseNumber);
    			if(tmp != null && !senseKey.contains(tmp)){
    				senseKey += ";" + tmp;    				
    			}
    		}
    	}
    	return senseKey;
    }
    
    private String convertPos(String pos){
    	String convertedPos = null;
    	if(pos.startsWith("n")){
    		convertedPos = "n";
    	}else if(pos.startsWith("adj")){
    		convertedPos = "a";
    	}else if(pos.startsWith("adv")){
    		convertedPos = "r";
    	}else if(pos.startsWith("v")){
    		convertedPos = "v";
    	}else{
    		convertedPos = "x";
    	}
    	return convertedPos;
    }
    
}
