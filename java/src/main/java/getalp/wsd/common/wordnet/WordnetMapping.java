package getalp.wsd.common.wordnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordnetMapping
{    
    public static final String wordnetMappingDirectoryPath = "data/wordnet/mapping";
    
    private Map<String, List<String>> map;
    
    private WordnetHelper wnFrom;
    
    private WordnetHelper wnTo;
        
    private static final Map<String, WordnetMapping> loadedMappings = new HashMap<>();

    private WordnetMapping(WordnetHelper wnFrom, WordnetHelper wnTo)
    {
        this.wnFrom = wnFrom;
        this.wnTo = wnTo;
        load();
    }

    private void load()
    {
        map = new HashMap<>();
        String wnFromTo = wnFrom.getVersion() + "-" + wnTo.getVersion();
        load(wordnetMappingDirectoryPath + "/mapping-" + wnFromTo + "/wn" + wnFromTo + ".noun", "n");
        load(wordnetMappingDirectoryPath + "/mapping-" + wnFromTo + "/wn" + wnFromTo + ".verb", "v");
        load(wordnetMappingDirectoryPath + "/mapping-" + wnFromTo + "/wn" + wnFromTo + ".adj", "a");
        load(wordnetMappingDirectoryPath + "/mapping-" + wnFromTo + "/wn" + wnFromTo + ".adv", "r");
    }
    
    private void load(String mappingFile, String postag)
    {
        try
        {
            loadWithException(mappingFile, postag);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void loadWithException(String mappingFile, String postag) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(mappingFile));
        String line;
        while ((line = br.readLine()) != null) 
        {
           String[] tokens = line.split("\\s+");
           int from = Integer.valueOf(tokens[0]);
           List<Integer> bestTo = new ArrayList<>(Arrays.asList(Integer.valueOf(tokens[1])));
           double bestProb = Double.valueOf(tokens[2]);
           for (int i = 3 ; i + 1 < tokens.length ; i += 2)
           {
               int candidateTo = Integer.valueOf(tokens[i]);
               double candidateProb = Double.valueOf(tokens[i+1]);
               if (candidateProb > bestProb)
               {
                   bestTo = new ArrayList<>(Arrays.asList(candidateTo));
                   bestProb = candidateProb;
               }
               else if (candidateProb == bestProb)
               {
                   bestTo.add(candidateTo);
               }
           }
           addToMap(postag + from, bestTo.stream().map(to -> postag + to).collect(Collectors.toList()));
        }
        br.close();
    }
    
    private void addToMap(String fromSynsetKey, List<String> toSynsetKeys)
    {
        List<String> fromSenseKeys = wnFrom.getSenseKeyListFromSynsetKey(fromSynsetKey);
        List<String> toSenseKeys = toSynsetKeys.stream().flatMap(synsetKey -> wnTo.getSenseKeyListFromSynsetKey(synsetKey).stream()).distinct().collect(Collectors.toList());
        for (String fromSenseKey : fromSenseKeys)
        {
            if (wnTo.isSenseKeyExists(fromSenseKey))
            {
                map.put(fromSenseKey, Arrays.asList(fromSenseKey));
            }
            else if (wnTo.isSenseKeyExists(fromSenseKey.replaceAll("%5", "%3")))
            {
                map.put(fromSenseKey, Arrays.asList(fromSenseKey.replaceAll("%5", "%3")));
            }
            else if (wnTo.isSenseKeyExists(fromSenseKey.replaceAll("%3", "%5")))
            {
                map.put(fromSenseKey, Arrays.asList(fromSenseKey.replaceAll("%3", "%5")));
            }
            else
            {
                List<String> specificToSenseKeys = new ArrayList<>();
                String fromLemma = fromSenseKey.substring(0, fromSenseKey.indexOf("%"));
                for (String toSenseKey : toSenseKeys)
                {
                    String toLemma = toSenseKey.substring(0, toSenseKey.indexOf("%"));
                    if (fromLemma.equals(toLemma))
                    {
                        specificToSenseKeys.add(toSenseKey);
                    }
                }
                if (!specificToSenseKeys.isEmpty())
                {
                    map.put(fromSenseKey, specificToSenseKeys);
                }
                else
                {
                    map.put(fromSenseKey, Collections.emptyList());
                }
            }
        }
    }

    public static WordnetMapping wnXtoY(int versionX, int versionY)
    {
        if (!loadedMappings.containsKey(versionX + "to" + versionY))
        {
            loadedMappings.put(versionX + "to" + versionY, new WordnetMapping(WordnetHelper.wn(versionX), WordnetHelper.wn(versionY)));
        }
        return loadedMappings.get(versionX + "to" + versionY);
    }
    
    public static WordnetMapping wn16to21()
    {
        return wnXtoY(16, 21);
    }

    public static WordnetMapping wn21to30()
    {
        return wnXtoY(21, 30);
    }
    
    public static WordnetMapping wn16to30()
    {
        return wnXtoY(16, 30);
    }

    public static WordnetMapping wn30to21()
    {
        return wnXtoY(30, 21);
    }

    public static WordnetMapping wn21to16()
    {
        return wnXtoY(21, 16);
    }

    public static WordnetMapping wn30to16()
    {
        return wnXtoY(30, 16);
    }
    
    public List<String> fromXtoY(String senseKeyX)
    {
        return map.get(senseKeyX);
    }

    public static List<String> from16to21(String senseKeyX)
    {
        return wn16to21().fromXtoY(senseKeyX);
    }

    public static List<String> from21to16(String senseKeyX)
    {
        return wn21to16().fromXtoY(senseKeyX);
    }

    public static List<String> from21to30(String senseKeyX)
    {
        return wn21to30().fromXtoY(senseKeyX);
    }

    public static List<String> from30to21(String senseKeyX)
    {
        return wn30to21().fromXtoY(senseKeyX);
    }

    public static List<String> from16to30(String senseKeyX)
    {
        return wn16to30().fromXtoY(senseKeyX);
    }

    public static List<String> from30to16(String senseKeyX)
    {
        return wn30to16().fromXtoY(senseKeyX);
    }
}
