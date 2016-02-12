package com.ouroboros.miscellaneous.setcover.normal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

//greedy set covering
public class GreedySetCovering {
	public static final Random RND = new Random();
	
	public static final Quicksort<Integer, Integer> sortInt = new Quicksort<Integer, Integer>();  //sorter for array with weight of Integer value
	public static final Quicksort<Double, Integer> sortDou = new Quicksort<Double, Integer>();   //sorter for array with weight of Double value
	
	public static final Comparator<Integer> intInc = new Comparator<Integer>()  //compare two Integers
	{
		public int compare(Integer a, Integer b)
		{
			return (a.compareTo(b));
		}
	};
	
	public static final Comparator<Double> doubleInc = new Comparator<Double>()  //compare two Doubles
	{
		public int compare(Double a, Double b)
		{
			return (a.compareTo(b));
		}
	};
	
	public static final Comparator<Integer> intDec = new Comparator<Integer>()  //reverse compare two Integers
	{
		public int compare(Integer a, Integer b)
		{
			return (-a.compareTo(b));
		}
	};
	
	public static final Comparator<Double> doubleDec = new Comparator<Double>() //reverse compare two Doubles
	{
		public int compare(Double a, Double b)
		{
			return (-a.compareTo(b));
		}
	};
	
	//greedy first set covering
	public static int[] weightedFirstGreedy(int[][] state)
	{
		int numSet = state.length;     //number of sets to cover elements
		int numEle = state[0].length;  //number of elements being covered 
		
		int[] coverCount = new int[numEle];  //number of sets that already cover one element
		int[] result = new int[numSet];   //the result of set covering
		
		HashMap<Integer, Double> weight = new HashMap<Integer, Double>();  //the weight of each element calculate with weighted greedy method
		HashMap<Integer, Double> cost = new HashMap<Integer, Double>();   //the cost of each set calculated with weighted greedy method
		
		Integer[] sortedSet = new Integer[numSet]; for(int i = 0 ; i < numSet; i++) sortedSet[i] = i;   //sorted sets
		Integer[] sortedEle = new Integer[numEle]; for(int i = 0 ; i < numEle; i++) sortedEle[i] = i;   //sorted elements
		
		
		//initialize weight
		for(int i = 0; i < numEle; i++)
		{
			int count = 0;
			for(int j = 0; j < numSet; j++)
			{
				if(state[j][i] != 0)
				{
					count++;
				}
			}
			
			if(count > 0)  
				weight.put(i, 1.0 / count);
			else  //ignore the elements that can not be covered
				weight.put(i, 0.0);
		}
		
		//initialize cost
		for(int i = 0; i < numSet; i++)
		{
			int count = 0;
			double costCount = 0;
			for(int j = 0; j < numEle; j++)
			{
				if(state[i][j] != 0)
				{
					costCount += weight.get(j);
					count++;
				}
			}
			
			if(count != 0)
				cost.put(i, costCount/count);
			else  //ignore the sets that can not cover elements
			{
				cost.put(i, 0.0);
				result[i] = -1;
			}
		}
		
		sortDou.sort(sortedSet, cost, doubleDec);   //sort sets with the cost decreasingly
		sortDou.sort(sortedEle, weight, doubleDec); //sort sets with the weight decreasingly
				
		for(int i = 0; i < numEle; i++)   //make every element get covered decreasingly with their weight
		{
			int indexEle = sortedEle[i]; 
			if(coverCount[indexEle] > 0 || weight.get(indexEle).compareTo(0.0) == 0)  //ignore the elements that already covered or can not be covered
				continue;
			
			double max = 0;
			int index = -1;
			
			//choose the first set that has the max value with weighted greedy method
			for(int j = 0; j < numSet; j++)
			{
				if(result[j] != 0)
					continue;
				
				int indexSet = sortedSet[j];
				
				if(state[indexSet][indexEle] != 0)
				{
					double current = 0;
					int count = 0;
					for(int k = 0; k < numEle; k++)
					{
						if(state[indexSet][k] != 0)
						{
							count++;
							if(coverCount[k] == 0)
								current += weight.get(k);
						}
					}
					current = current / count;
					
					if(max < current && Math.abs(max - current) > 0.000001)
					{
						max = current;
						index = indexSet;
					}
				}
			}
			
			if(index >= 0)
			{
				result[index] = 1;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0)
					{
						coverCount[l]++;
					}
				}
			}
		}
		
		//refresh the weight and cost after the original result is obtained 
		for(int i = 0; i < numEle; i++)
		{
			if(coverCount[i] > 0)
				weight.put(i, 1.0 / coverCount[i]);
		}
		
		for(int i = 0; i < numSet; i++)
		{
			int count = 0;
			double costCount = 0;
			for(int j = 0; j < numEle; j++)
			{
				if(state[i][j] != 0)
				{
					costCount += weight.get(j);
					count++;
				}
			}
			
			cost.put(i, costCount/count);
		}
		
		sortDou.sort(sortedSet, cost, doubleInc);  //sort the result sets with their cost increasingly
		
		//remove the fisrt high cost sets whose elements can be covered with other sets in the result
		for(int i = 0; i < numSet; i++)	
		{
			if(result[i] <= 0)
				continue;
			
			int indexSet = sortedSet[i];
			boolean isDuplicate = true;
			for(int j = 0; j < numEle; j++)
			{
				if(state[indexSet][j] != 0)
				{
					if(coverCount[j] < 2)
					{
						isDuplicate = false;
						break;
					}
				}
			}
			
			if(isDuplicate)
			{
				for(int j = 0; j < numEle; j++)
				{
					if(state[indexSet][j] != 0)
						coverCount[j]--;	
				}
				result[indexSet] = 0;
			}
		}
		
		return result;
		
	}
	
	//the same process of greedy first set covering except during set adding and removing, 
	//if finding multiple sets with the same objective value, randomly select one from them
	//see the comments for greedy first set covering 
	public static int[] weightedStochasticGreedy (int[][] state)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		int[] coverCount = new int[numEle];
		int[] result = new int[numSet];
		
		HashMap<Integer, Double> weight = new HashMap<Integer, Double>();
		
		Integer[] sortedSet = new Integer[numSet]; for(int i = 0 ; i < numSet; i++) sortedSet[i] = i;
		Integer[] sortedEle = new Integer[numEle]; for(int i = 0 ; i < numEle; i++) sortedEle[i] = i;
		
		//initialize cost
		for(int i = 0; i < numSet; i++)
		{
			int count = 0;
			for(int j = 0; j < numEle; j++)
			{
				if(state[i][j] != 0)
				{
					count++;
				}
			}
			
			if(count == 0)
			{
				result[i] = -1;
			}
		}
		
		
		//initialize weight
		for(int i = 0; i < numEle; i++)
		{
			int count = 0;
			for(int j = 0; j < numSet; j++)
			{
				if(state[j][i] != 0)
				{
					count++;
				}
			}
			
			if(count > 0)
				weight.put(i, 1.0 / count);
			else
				weight.put(i, 0.0);
		}
		
		sortDou.sort(sortedEle, weight, doubleDec);
				
		for(int i = 0; i < numEle; i++)
		{
			int indexEle = sortedEle[i]; 
			if(coverCount[indexEle] > 0 || weight.get(indexEle).compareTo(0.0) == 0)
				continue;
			
			double max = 0;
			double[] value = new double[numSet];
			int maxCount = 0;
			int index = -1;
			
			for(int j = 0; j < numSet; j++)
			{
				if(result[j] != 0)
					continue;
				
				int indexSet = sortedSet[j];
				
				if(state[indexSet][indexEle] != 0)
				{
					int count = 0;
					double valueCount = 0;
					for(int k = 0; k < numEle; k++)
					{
						if(state[indexSet][k] != 0)
						{
							count++;
							if(coverCount[k] == 0)
							 valueCount += weight.get(k);
						}
					}
					
					value[indexSet] = valueCount / count;
					
					if(max < value[indexSet] && Math.abs(max - value[indexSet]) > 0.000001)
					{
						max = value[indexSet];
						maxCount = 1;
						index = indexSet;
					} else if(Math.abs(max - value[indexSet]) < 0.000001)
					{
						maxCount++;
					}
				}
			}
			
			if(maxCount > 1)
			{
				int rand = RND.nextInt(maxCount);
				
				for(int j = 0; j < numSet; j++)
				{
					if(Math.abs(max - value[j]) <= 0.000001)
					{
						if(rand > 0)
						{
							rand--;
						} else
						{
							index = j;
							break;
						}
					}
				}
			}
			
			if(index >= 0)
			{
				result[index] = 1;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0)
					{
						coverCount[l]++;
					}
				}
			}
		}
		
		int duplicateCount = 0;
		while(true)
		{
			duplicateCount = 0;
			int index = -1;
			
			for(int k = 0; k < numEle; k++)
			{
				if(coverCount[k] > 0)
					weight.put(k, 1.0 / coverCount[k]);
			}
			
			double[] value = new double[numSet];
			double min = Double.MAX_VALUE; 
			for(int i = 0; i < numSet; i++)	
			{
				if(result[i] <= 0)
					continue;
				
				int indexSet = sortedSet[i];
				boolean isDuplicate = true;
				double valueCount = 0;
				int count = 0;
				
				for(int j = 0; j < numEle; j++)
				{
					if(state[indexSet][j] != 0)
					{
						if(coverCount[j] < 2)
						{
							isDuplicate = false;
							break;
						} else
						{
							count++;
							valueCount += weight.get(j);
						}
					}
				}
				
				if(isDuplicate)
				{
					value[indexSet] = valueCount / count;
					
					if(min > value[indexSet] && Math.abs(min - value[indexSet]) > 0.000001)
					{
						min = value[indexSet];
						duplicateCount = 1;
						index = indexSet;
					} else if(Math.abs(min - value[indexSet]) < 0.000001)
					{
						duplicateCount++;
					}
				}
			}
			
			
			if(duplicateCount > 0)
			{
				if(duplicateCount > 1 )
				{
					int rand = RND.nextInt(duplicateCount);
					for(int j = 0; j < numSet; j++)
					{
						if(Math.abs(min - value[j]) <= 0.000001)
						{
							if(rand > 0)
							{
								rand--;
							} else
							{
								index = j;
								break;
							}
						}
					}
				}
				

				result[index] = 0;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0)
					{
						coverCount[l]--;
					}
				}

			} else
				break;
		} 
		
		return result;
	}
	
	
}

