package getalp.wsd.common.utils;

public class SenseKeyUtils 
{
	public static String extractLemmaFromSenseKey(String senseKey)
	{
		return senseKey.substring(0, senseKey.indexOf("%"));
	}
	
	public static String extractPOSFromSenseKey(String senseKey)
	{
		int index = senseKey.indexOf("%");
		String posAsInt = senseKey.substring(index + 1, index + 2);
		return POSConverter.toWNPOS(Integer.valueOf(posAsInt));
	}

	public static String extractWordKeyFromSenseKey(String senseKey)
	{
		return extractLemmaFromSenseKey(senseKey) + "%" + extractPOSFromSenseKey(senseKey);
	}
}
