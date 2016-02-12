package com.ouroboros.miscellaneous.setcover.advanced;

import java.util.Random;


public class GreedySetCovering {

	public static int[] stochasticGreedy (int[][] state, double[] cost)
	{
		int numSet = state.length;      //number of sets
		int numEle = state[0].length;   //number of columns
		
		int[] result = new int[numSet]; //final result 
		int[] eleIn = new int[numEle];  //rows that have been covered
		
		int curIn = 0;   //the number of rows that have been covered
		
		//find the rows that only have one column to cover them, and the corresponding columns
		for(int i = 0; i < numEle; i++)
		{
			if(eleIn[i] != 0)  //if row i is covered
				continue;
			
			int t = 0;  //the number of columns to cover row i
			int u = 0;  //the last column to cover row i, if only one column cover row i, directly record its index
			for(int j = 0; j < numSet; j++)
			{
				if(state[j][i] != 0)
				{
					t++;
					u = j;
				}
			}
			
			if(t==1)   //if only exist one column u to cover row i
			{
				for(int k = 0; k < numEle; k++)  //reset the state of all the rows covered by u 
				{
					if(state[u][k] != 0 && eleIn[k] == 0)
					{
						eleIn[k] = 1;
						curIn++;
					}
				}
				result[u] = 2;   //put column u to result
				
			} else if(t==0)  //if no column to cover row i, reset the state of row i
			{
				eleIn[i] = -1;
				curIn++;
			}
		}
		
		//greedy select sets
		while(curIn < numEle)  //if not all rows get covered
		{
			double[] weight = new double[numSet];   //the weight of one column for this iteration, select the column with least weight to the result
			int candi = -1;     //the index of the column to be included in the result for this iteration
			double mini = Double.MAX_VALUE;   //the value of the least weight 
			
			for(int indSet = 0; indSet < numSet; indSet++)   //calculate the weight for all unused columns
			{
				if(result[indSet] != 0)
					continue;
				
				int count = 0;
				for(int indEle = 0; indEle < numSet; indEle++)
				{
					if(state[indSet][indEle] != 0 && eleIn[indEle] == 0)
					{
						count++;
					}
				}

				if(count != 0)
				{
					weight[indSet] = cost[indSet]/count;     //the weight the the cost of the column divided by the number of uncovered rows it covers
					if(Math.abs(weight[indSet] - mini) > 0.000001 && mini > weight[indSet])  //find the least cost
						mini = weight[indSet];   
				}
			}

			int c = 0;  //count the number of columns with least cost
			for(int i = 0; i < numSet; i++)
			{
				if(Math.abs(weight[i] - mini) < 0.000001)
				{
					c++;
					candi = i;  //the last column with the least cost, if only one column with least cost, directly record its index
				}
			}
			
			if(c > 1)  //if there are more than one column with least cost, randomly choose one from them 
			{
				Random rand = new Random();
				int r = rand.nextInt(c);
				int i = 0;

				for(int j = 0; j < numSet; j++)
				{
					if(Math.abs(weight[j] - 0) < 0.000001)
						continue;
					
					if(Math.abs(weight[j] - mini) < 0.000001)
					{
						if(i == r)
						{
							candi = j;
							break;
						}
						else
							i++;
					}
				}
			}
			
			
			for(int k = 0; k < numEle; k++)  //reset the state of rows covered by the column selected in this iteration
			{

				if(state[candi][k] != 0 && eleIn[k] == 0)
				{
					eleIn[k] = 1;
					curIn++;
				}
			}
			
			result[candi] = 1;   //put the column into result
		}
		
		//remove the duplicate columns
		int duplicate = 0;  //the number of duplicate columns for this iteration
		do {
			duplicate = 0;  
			double[] duplicates = new double[numSet];  //record the duplicate weight for all duplicate columns
			double max = 0;   //the max duplicate weight
			for(int indSet = 0; indSet < numSet; indSet++)  //go through all columns in result
			{
				if(result[indSet] == 1)
				{
					boolean n = false;
					
					for(int indEle = 0; indEle < numEle; indEle++)  // if all rows covered by that column have more than one column in result to cover it, the column is duplicate
					{
						if(eleIn[indEle] == -1 || state[indSet][indEle] == 0)
							continue;
						
						boolean t = true;
						for(int i = 0; i < numSet; i++)
						{
							if(i != indSet && result[i] != 0)
							{
								if(state[i][indEle] != 0)  
								{
									t = false;
									break;
								}
							}
						}
						
						if(t)
						{
							n = true;
							break;
						}
					}
					
					if(!n)  //if a column is duplicate, calculate its duplicate weight
					{						
						duplicates[indSet] = cost[indSet];
						if(Math.abs(max - duplicates[indSet]) > 0.000001 && max < duplicates[indSet]) //find the max duplicate value
							max = duplicates[indSet];    
						duplicate++;
					}	
				}
			}
			
			if(duplicate != 0)  //if duplicate exists
			{
				int c = 0;  //the number of columns with max duplicate weight
				int candi = -1;
				for(int i = 0; i < numSet; i++) 
				{
					if(Math.abs(duplicates[i] - max) < 0.000001)
					{
						c++;
						candi = i;
					}
				}
				
				if(c > 1)  //if more than one column with the max duplicate value, randomly select one among them
				{
					Random rand = new Random();
					int r = rand.nextInt(c);
					int i = 0;
						
					for(int j = 0; j < numSet; j++)
					{
						if(Math.abs(duplicates[j] - 0) < 0.000001)
							continue;
						
						if(Math.abs(duplicates[j] - max) < 0.000001)
						{
							if(i == r)
							{
								candi = j;
								break;
							}
							else
								i++;
						}
					}
				}
				
				result[candi] = 0;  // remove that duplicate column
			}
				
		} while(duplicate != 0);  //while we still can find duplicate columns

		return result;
	}
}

