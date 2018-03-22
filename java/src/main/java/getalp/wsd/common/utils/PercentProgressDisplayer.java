package getalp.wsd.common.utils;

public class PercentProgressDisplayer 
{
	private double max;
	
	private int percent;
	
	public PercentProgressDisplayer(double max)
	{
		this.max = max;
		this.percent = 0;
	}
	
	public void refresh(String message, double current)
	{
		int currentPercent = (int) (((double) current / (double) max) * 100.0);
		if (currentPercent != percent)
		{
			this.percent = currentPercent;
			System.out.print(message + percent + "%\r");
		}
	}
}
