package fsk.ouroboros725.miscellaneous.segmentdensity;

public class BruteForce {

	public static int[] segmentDensity(double[] inputs, int lower, int upper)
	{
		int[] result = new int[2];
		double sumT = 0;
		double denM = -Double.MAX_VALUE;
		double denT = 0;
		
		for(int i = lower-1; i < inputs.length; i++)
		{			
			sumT = 0;
			for(int j = i; j >= 0; j--)
			{
				sumT = sumT+inputs[j];
				if(i-j+1 > upper)
					break;
				else if(i-j+1 >= lower)
				{
					denT = sumT/(i-j+1);
					if(doubleCompare(denT, denM)>=0)
					{	
						denM = denT;
						result[0] = i;
						result[1] = j;
					}
				}
			}
		}
		
		return result;
	}
	
	private static int doubleCompare(double d1, double d2)
	{
			
		if(Math.abs(d1-d2) < 0.00001)
			return 0;
		
		if(d1 > d2)
			return 1;
		else
			return -1;
	}
}

