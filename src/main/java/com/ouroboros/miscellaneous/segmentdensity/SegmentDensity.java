package com.ouroboros.miscellaneous.segmentdensity;


import java.util.ArrayList;

public class SegmentDensity {

	public double[] inputs = null;
	
	public int lower = 0;
	
	public int upper = 0;
	
	public int count = 0;
	
	public double[] points = null;
	
	public double[][][] slopes = null;
	
	public int[][][] candidate = null;
	
	public ArrayList<Integer> hull = null;
	
	public int alpha = 0;
	
	public SegmentDensity(double[] inputs, int lower, int upper)
	{
		this.inputs = inputs;
		this.lower = lower;
		this.upper = upper;
		
		this.prefixSum();
		
		count = (inputs.length-lower)/(upper-lower+1) + ((inputs.length-lower)%(upper-lower+1)==0?0:1);
		
		this.slopes = new double[count][2][2];
		this.candidate = new int[count][2][2];
		
		this.hull = new ArrayList<Integer>();
	}
	
	private void prefixSum()
	{
		int size = inputs.length;
		points = new double[size];
		
		double sum = 0;
		for(int i = 0; i < size; i++)
		{
			sum += inputs[i];
			points[i] = sum;
		}
	}
	
	public void stepPass(int direction, int batch, int ith)
	{
		int index0 = lower+(upper-lower+1)*batch+ith;
				
		if(index0 == points.length -1 && direction == 1)
		{
			finalPass();
			return;
		}
		
		if(direction == 0)
		{
			int index1 = index0-lower;
			
			if(ith == 0)
			{
				slopes[batch][0][0] = index0-index1;
				slopes[batch][0][1] = points[index0]-points[index1];
				
				candidate[batch][0][0] = index0;
				candidate[batch][0][1] = index1;
				
				hull = new ArrayList<Integer>();
				hull.add(index1);
				
				alpha = 0;
				
				return;
			}
			else
			{		
				double pos1 = plPos(slopes[batch][0][0], slopes[batch][0][1], hull.get(alpha), points[hull.get(alpha)], index1);
				
				if(doubleCompare(points[index1], pos1) > 0)
				{
					if(alpha != hull.size()-1)
					{
						for(int i = alpha; i < hull.size()-1; i++)
						{	
							int c = hull.get(i);;
							int r = hull.get(i+1);
							
							if(doubleCompare(points[r], plPos(index1-c, points[index1]-points[c], c, points[c], r)) >= 0)
							{
								int s = hull.size();
								for(int j = i+1; j < s; j++)
									hull.remove(hull.size()-1);
								break;
							}
						}
					}
					
					hull.add(index1);
					
					double pos0 = plPos(slopes[batch][0][0], slopes[batch][0][1], hull.get(alpha), points[hull.get(alpha)], index0);
					
					if(doubleCompare(points[index0], pos0) > 0)
					{
						int i = alpha;
						if(alpha != hull.size()-1)
						{
							for(; i < hull.size()-1; i++)
							{
								int c = hull.get(i);
								int r = hull.get(i+1);
								
								if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
								{								
									slopes[batch][0][0] = index0-c;
									slopes[batch][0][1] = points[index0]-points[c];
									
									candidate[batch][0][0] = index0;
									candidate[batch][0][1] = c;
									
									alpha = i;
									
									break;
								}
							}
						}

						if(i == hull.size()-1)
						{
							int c = hull.get(i);
							
							slopes[batch][0][0] = index0-c;
							slopes[batch][0][1] = points[index0]-points[c];
							
							candidate[batch][0][0] = index0;
							candidate[batch][0][1] = c;
							
							alpha = i;
						}
					}
					
					return;
				}
				
				if(doubleCompare(points[index1], pos1) < 0)
				{
					int i = alpha;
					if(alpha != 0)
					{
						for(; i > 0; i--)
						{	
							int c = hull.get(i);
							int l = hull.get(i-1);
							
							if(doubleCompare(points[l], plPos(index1-c, points[index1]-points[c], c, points[c], l)) > 0)
							{
								int s = hull.size();
								for(int j = i+1; j < s; j++)
								{
									hull.remove(hull.size()-1);
								}
								
								break;
							}
						}
					}
					
					if(i == 0)
					{
						int s = hull.size();
						for(int j = 1; j < s; j++)
							hull.remove(hull.size()-1);
					}
					
					hull.add(index1);
					alpha = hull.size()-1;
					
					double pos0 = plPos(slopes[batch][0][0], slopes[batch][0][1], index1, points[index1], index0);
					
					if(doubleCompare(points[index0], pos0) > 0)
					{
						slopes[batch][0][0] = index0-index1;
						slopes[batch][0][1] = points[index0]-points[index1];
						
						candidate[batch][0][0] = index0;
						candidate[batch][0][1] = index1;
					}
					
					return;
				}
				
				if(doubleCompare(points[index1], pos1) == 0)
				{
					int i = alpha+1;
					
					if(alpha != 0)
					{
						int c = hull.get(alpha);
						int l = hull.get(alpha-1);
						
						if(doubleCompare(points[l], plPos(index1-c, points[index1]-points[c], c, points[c], l)) == 0)
						{
							i = alpha;
							alpha--;
						}
					}
					
					int s = hull.size();
					for(; i < s; i++)
						hull.remove(hull.size()-1);
					
					hull.add(index1);
					alpha = hull.size()-1;
					
					double pos0 = plPos(slopes[batch][0][0], slopes[batch][0][1], index1, points[index1], index0);
					if(doubleCompare(points[index0], pos0) > 0)
					{
						slopes[batch][0][0] = index0-index1;
						slopes[batch][0][1] = points[index0]-points[index1];
						
						candidate[batch][0][0] = index0;
						candidate[batch][0][1] = index1;
					}
					
					return;
				}
			}
		}
		else
		{
			int index1 = index0-upper;
			
			if(ith == upper-lower)
			{
				slopes[batch][1][0] = index0-index1;
				slopes[batch][1][1] = points[index0]-points[index1];
				
				candidate[batch][1][0] = index0;
				candidate[batch][1][1] = index1;
				
				hull = new ArrayList<Integer>();
				hull.add(index1);
				
				alpha = 0;
				
				return;
			}
			else
			{				
				double pos1 = plPos(slopes[batch][1][0], slopes[batch][1][1], hull.get(alpha), points[hull.get(alpha)], index1);
				double pos0 = plPos(slopes[batch][1][0], slopes[batch][1][1], hull.get(alpha), points[hull.get(alpha)], index0);
				
				if(doubleCompare(points[index1], pos1) > 0)
				{
					int i = alpha;
					if(alpha != 0)
					{
						for(; i > 0; i--)
						{	
							int c = hull.get(i);
							int l = hull.get(i-1);
							
							if(doubleCompare(points[l], plPos(index1-c, points[index1]-points[c], c, points[c], l)) >= 0)
							{
								for(int j = i-1; j >= 0; j--)
									hull.remove(0);
								
								break;
							}
						}
					}
					
					hull.add(0, index1);
					
					alpha = alpha-i+1;
					
					if(doubleCompare(points[index0], pos0) > 0)
					{
						i = alpha;
						if(alpha != hull.size()-1)
						{
							for(; i < hull.size()-1; i++)
							{
								int c = hull.get(i);
								int r = hull.get(i+1);
								
								if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
								{								
									slopes[batch][1][0] = index0-c;
									slopes[batch][1][1] = points[index0]-points[c];
									
									candidate[batch][1][0] = index0;
									candidate[batch][1][1] = c;
									
									alpha = i;
									
									break;
								}
							}
						}
						
						if(i == hull.size()-1)
						{
							int c = hull.get(i);
							
							slopes[batch][1][0] = index0-c;
							slopes[batch][1][1] = points[index0]-points[c];
							
							candidate[batch][1][0] = index0;
							candidate[batch][1][1] = c;
							
							alpha = i;
						}
					}
					
					return;
				}
				
				if(doubleCompare(points[index1], pos1) < 0)
				{				
					int i = alpha;
					int s = hull.size();
					if(alpha != hull.size()-1)
					{
						for(; i < s-1; i++)
						{	
							int c = hull.get(i);
							int r = hull.get(i+1);
							
							if(doubleCompare(points[r], plPos(index1-c, points[index1]-points[c], c, points[c], r)) > 0)
							{
								for(int j = i-1; j >= 0; j--)
								{
									hull.remove(0);
								}
								break;
							}
						}
					}
					
					if(i == s-1)
					{
						for(int j = i-1; j >= 0; j--)
						{
							hull.remove(0);
						}
					}
					
					hull.add(0, index1);
					
					if(doubleCompare(points[index0], pos0) > 0)
					{
						int j = 0;

						for(; j < hull.size()-1; j++)
						{
							int c = hull.get(j);
							int r = hull.get(j+1);
							
							if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
							{								
								slopes[batch][1][0] = index0-c;
								slopes[batch][1][1] = points[index0]-points[c];
								
								candidate[batch][1][0] = index0;
								candidate[batch][1][1] = c;
								
								alpha = j;
								
								break;
							}
						}
						
						if(j == hull.size()-1)
						{
							int c = hull.get(j);
							
							slopes[batch][1][0] = index0-c;
							slopes[batch][1][1] = points[index0]-points[c];
							
							candidate[batch][1][0] = index0;
							candidate[batch][1][1] = c;
							
							alpha = j;
						}
					}
					
					if(doubleCompare(points[index0], pos0) <= 0)
					{
						if(doubleCompare(points[index0], plPos(slopes[batch][1][0], slopes[batch][1][1], index1, points[index1], index0)) > 0)
						{
							slopes[batch][1][0] = index0-index1;
							slopes[batch][1][1] = points[index0]-points[index1];
							
							candidate[batch][1][0] = index0;
							candidate[batch][1][1] = index1;
						}
						
						alpha = 0;
					}
					
					return;
				}
				
				if(doubleCompare(points[index1], pos1) == 0)
				{
					int i = alpha-1;
					
					if(alpha != hull.size()-1)
					{
						int c = hull.get(alpha);
						int r = hull.get(alpha+1);
						
						if(doubleCompare(points[r], plPos(index1-c, points[index1]-points[c], c, points[c], r)) == 0)
							i = alpha;
					}
					
					for(; i >= 0; i--)
						hull.remove(0);

					hull.add(0, index1);
					
					alpha = 1;
					
					if(doubleCompare(points[index0], pos0) > 0)
					{
						i = alpha;
						if(alpha != hull.size()-1)
						{
							for(; i < hull.size()-1; i++)
							{
								int c = hull.get(i);
								int r = hull.get(i+1);
								
								if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
								{								
									slopes[batch][1][0] = index0-c;
									slopes[batch][1][1] = points[index0]-points[c];
									
									candidate[batch][1][0] = index0;
									candidate[batch][1][1] = c;
									
									alpha = i;
									
									break;
								}
							}
						}
						
						if(i == hull.size()-1)
						{
							int c = hull.get(i);
							
							slopes[batch][1][0] = index0-c;
							slopes[batch][1][1] = points[index0]-points[c];
							
							candidate[batch][1][0] = index0;
							candidate[batch][1][1] = c;
							
							alpha = i;
						}
					}
					
					return;
				}
			}
		}
	}
	
	private void finalPass()
	{
		int batch = count-1;
		
		int index0 = points.length-1;
		int index1 = lower+(upper-lower+1)*count-1-upper;
		
		
		slopes[batch][1][0] = index0-index1;
		slopes[batch][1][1] = points[index0]-points[index1];
			
		candidate[batch][1][0] = index0;
		candidate[batch][1][1] = index1;
			
		hull = new ArrayList<Integer>();
		hull.add(index1);
			
		alpha = 0;
		
		index1--;
			
		while(index1 >= index0-upper)
		{				
			double pos1 = plPos(slopes[batch][1][0], slopes[batch][1][1], hull.get(alpha), points[hull.get(alpha)], index1);
			double pos0 = plPos(slopes[batch][1][0], slopes[batch][1][1], hull.get(alpha), points[hull.get(alpha)], index0);
			
			if(doubleCompare(points[index1], pos1) > 0)
			{
				int i = alpha;
				if(alpha != 0)
				{
					for(; i > 0; i--)
					{	
						int c = hull.get(i);
						int l = hull.get(i-1);
						
						if(doubleCompare(points[l], plPos(index1-c, points[index1]-points[c], c, points[c], l)) >= 0)
						{
							for(int j = i-1; j >= 0; j--)
								hull.remove(0);
							
							break;
						}
					}
				}
				
				hull.add(0, index1);
				
				alpha = alpha-i+1;
				
				if(doubleCompare(points[index0], pos0) > 0)
				{
					i = alpha;
					if(alpha != hull.size()-1)
					{
						for(; i < hull.size()-1; i++)
						{
							int c = hull.get(i);
							int r = hull.get(i+1);
							
							if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
							{								
								slopes[batch][1][0] = index0-c;
								slopes[batch][1][1] = points[index0]-points[c];
								
								candidate[batch][1][0] = index0;
								candidate[batch][1][1] = c;
								
								alpha = i;
								
								break;
							}
						}
					}
					
					if(i == hull.size()-1)
					{
						int c = hull.get(i);
						
						slopes[batch][1][0] = index0-c;
						slopes[batch][1][1] = points[index0]-points[c];
						
						candidate[batch][1][0] = index0;
						candidate[batch][1][1] = c;
						
						alpha = i;
					}
				}
				
			}
			
			if(doubleCompare(points[index1], pos1) < 0)
			{					
				int i = alpha;
				int s = hull.size();
				if(alpha != hull.size()-1)
				{
					for(; i < s-1; i++)
					{	
						int c = hull.get(i);
						int r = hull.get(i+1);
						
						if(doubleCompare(points[r], plPos(index1-c, points[index1]-points[c], c, points[c], r)) > 0)
						{
							for(int j = i-1; j >= 0; j--)
								hull.remove(0);
							break;
						}
					}
				}
				
				if(i == s-1)
				{
					for(int j = i-1; j >= 0; j--)
						hull.remove(0);
				}
				
				hull.add(0, index1);
				
				if(doubleCompare(points[index0], pos0) > 0)
				{
					int j = 0;

					for(; j < hull.size()-1; j++)
					{
						int c = hull.get(j);
						int r = hull.get(j+1);
						
						if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
						{								
							slopes[batch][1][0] = index0-c;
							slopes[batch][1][1] = points[index0]-points[c];
							
							candidate[batch][1][0] = index0;
							candidate[batch][1][1] = c;
							
							alpha = j;
							
							break;
						}
					}
					
					if(j == hull.size()-1)
					{
						int c = hull.get(j);
						
						slopes[batch][1][0] = index0-c;
						slopes[batch][1][1] = points[index0]-points[c];
						
						candidate[batch][1][0] = index0;
						candidate[batch][1][1] = c;
						
						alpha = j;
					}
				}
				
				if(doubleCompare(points[index0], pos0) <= 0)
				{
					if(doubleCompare(points[index0], plPos(slopes[batch][1][0], slopes[batch][1][1], index1, points[index1], index0)) > 0)
					{
						slopes[batch][1][0] = index0-index1;
						slopes[batch][1][1] = points[index0]-points[index1];
						
						candidate[batch][1][0] = index0;
						candidate[batch][1][1] = index1;
					}
					
					alpha = 0;
				}
				
			}
			
			if(doubleCompare(points[index1], pos1) == 0)
			{
				int i = alpha-1;
				
				if(alpha != hull.size()-1)
				{
					int c = hull.get(alpha);
					int r = hull.get(alpha+1);
					
					if(doubleCompare(points[r], plPos(index1-c, points[index1]-points[c], c, points[c], r)) == 0)
						i = alpha;
				}
				
				for(; i >= 0; i--)
					hull.remove(0);

				hull.add(0, index1);
				
				alpha = 1;
				
				if(doubleCompare(points[index0], pos0) > 0)
				{
					i = alpha;
					if(alpha != hull.size()-1)
					{
						for(; i < hull.size()-1; i++)
						{
							int c = hull.get(i);
							int r = hull.get(i+1);
							
							if(doubleCompare(points[r], plPos(index0-c, points[index0]-points[c], c, points[c], r)) > 0)
							{								
								slopes[batch][1][0] = index0-c;
								slopes[batch][1][1] = points[index0]-points[c];
								
								candidate[batch][1][0] = index0;
								candidate[batch][1][1] = c;
								
								alpha = i;
								
								break;
							}
						}
					}
					
					if(i == hull.size()-1)
					{
						int c = hull.get(i);
						
						slopes[batch][1][0] = index0-c;
						slopes[batch][1][1] = points[index0]-points[c];
						
						candidate[batch][1][0] = index0;
						candidate[batch][1][1] = c;
						
						alpha = i;
					}
				}

			}
			
			index1--;
		}
	}
	
	public int[] result()
	{
		int[] result = new int[2];
		double slopeM = -Double.MAX_VALUE;
		double slopeT = 0;
		
		for(int i = 0; i < count; i++)
		{
			
			slopeT = slopes[i][0][1]/slopes[i][0][0];
			if(doubleCompare(slopeT,slopeM) > 0)
			{
				slopeM = slopeT;
				result[0] = candidate[i][0][0];
				result[1] = candidate[i][0][1];
			}
			
			if(i == 0)
				continue;
			
			slopeT = slopes[i][1][1]/slopes[i][1][0];
			if(doubleCompare(slopeT,slopeM) > 0)
			{
				slopeM = slopeT;
				result[0] = candidate[i][1][0];
				result[1] = candidate[i][1][1];
			}
		}
		
		return result;
	}
	
	private int doubleCompare(double d1, double d2)
	{
		if(Math.abs(d1-d2) < 0.00001)
			return 0;
		
		if(d1 > d2)
			return 1;
		else
			return -1;
	}
	
	private double plPos(double slopeX, double slopeY, double pointX, double pointY, double candX)
	{
		return (slopeY/slopeX*(candX-pointX)+pointY);
	}
}

