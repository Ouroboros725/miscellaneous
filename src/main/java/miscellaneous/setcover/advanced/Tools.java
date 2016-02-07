package miscellaneous.setcover.advanced;

import java.util.Comparator;

public class Tools {
	
	//class associate the cost of a set with the number of rows it covers
	public static class Set
	{
		public double cost = 0;
		public int count = 0;
	}
	
	//comparator for objects of Set
	public static final Comparator<Set> setCmp = new Comparator<Set>()
	{
		public int compare(Set a, Set b)
		{
			if(a.cost - b.cost <= 0.000001)
			{
				if(a.count > b.count)
					return -1;
				else if(a.count < b.count)
					return 1;
				else
					return 0;
			}
			else if(a.cost > b.cost && a.cost - b.cost > 0.000001)
				return 1;
			else
				return -1;
		}
	};
	
	//comparator for objects of Integer
	public static final Comparator<Integer> intCmp = new Comparator<Integer>()
	{
		public int compare(Integer a, Integer b)
		{
			return a.compareTo(b);
		}
	};
	
	//calculate the average value
	public static double average(double[] results)
	{
		int k = results.length;
		double total = 0;
		for(int i = 0; i < results.length; i++)
		{
			total += results[i];
		}
		
		return total / k; 
	}
	
	//calculate the standard deviation
	public static double stdDev(double[] results)
	{
		double avg = average(results);
		double total = 0;
		int k = results.length;
		for(int i = 0; i < results.length; i++)
		{
			total += Math.pow(results[i]-avg, 2);
		}
		
		return Math.sqrt(total / k);
	}
	
	//get the max value
	public static double max(double[] results)
	{
		double max = Double.MIN_VALUE;
		
		for(int i = 0; i < results.length; i++)
		{
			if(results[i] > max)
				max = results[i];
		}
		
		return max;
	}
	
	//get the smallest value
	public static double min(double[] results)
	{
		double min = Double.MAX_VALUE;
		
		for(int i = 0; i < results.length; i++)
		{
			if(results[i] < min)
				min = results[i];
		}
		
		return min;
	}
}

