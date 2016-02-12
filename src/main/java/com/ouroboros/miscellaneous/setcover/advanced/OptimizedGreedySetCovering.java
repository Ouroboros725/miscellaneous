package com.ouroboros.miscellaneous.setcover.advanced;

import java.util.Random;


public class OptimizedGreedySetCovering {
	public static final Random RND = new Random();  //random operator
	
	//state: problem space
	//cost: cost of each column
	public static int[] stochasticGreedy (int[][] state, double cost[])
	{
		int numSet = state.length;     //the number of the sets
		int numEle = state[0].length;  // the number of the columns
		
		int[] coverCount = new int[numEle];  //number of columns that currently cover this row
		int[] result = new int[numSet];      //final solution
		double[] weight = new double[numEle];//number of columns that could possibly cover this row
		
		//the result of have the rows sorted by the number of columns that can cover them
		int[] sortedEle = new int[numEle]; 
		for(int i = 0 ; i < numEle; i++) 
			sortedEle[i] = i;
		
		//reduce columns, find if other columns can cover the rows that covered by one column with less cost
		for(int i = 0; i < numSet; i++)
		{
			if(result[i] < 0)  //if current checking column has been reduced
				continue;
			
			double value = 0;
			int[] matchCol = new int[numSet];  //columns that can be used to replace current checking column
			int[] matchRow = new int[numEle];  //rows that can get covered by other columns
			boolean isDuplicate = true;
			
			for(int j = 0; j < numEle; j++)  //check the rows that get covered by current checking column
			{
				if(matchRow[j] != 0)     //if current checking row has been get covered by other columns
					continue;
				
				if(state[i][j] != 0)   //if current checking row can get covered by current checking column
				{
					double min = Double.MAX_VALUE;  //the least greedy value of candidate columns  
					int max = 0;    //the max number of rows that can get covered by both candidate column and current checking column
					int index = -1;  //the index of the candidate column that can be used to cover current checking row 
					
					for(int k = 0; k < numSet; k++)   //go through all columns to find a least cost column to cover current checking row
					{
						if(result[k] < 0 || matchCol[k] != 0)  //if a column has been reduced or has been used to cover this column
							continue;
						
						if(state[k][j] != 0 && k != i)  //if find a different column to cover current checking row
						{
							int count = 0;   //the number of rows that this candidate column and current checking column can both cover
							for(int l = 0; l < numEle; l++)
							{	
								if(state[k][l] == 1 && state[i][l] == 1)
								{
									count++;
								}
							}
							
							double match = cost[k] / count;    //greedy value
							
							if(min > match && Math.abs(min - match) > 0.000001)  //find the least greedy value candidate column
							{
								min = match;
								index = k;
								max = count;
							} else if(Math.abs(min - match) < 0.000001)  //if greedy value is the same, select the candidate column that can maximize the common coverage
							{
								if(max < count)
								{
									index = k;
									max = count;
								} 
							}
						}	
					}
					
					if(index < 0)  //if cannot find any candidate columns to cover current checking row, the current checking column cannot get removed 
					{
						isDuplicate = false;
					} else   //include the candidate column in replacement set 
					{
						matchCol[index] = 1;
						value += cost[index];
						
						for(int k = 0; k < numEle; k++)  //record the common covered rows
						{
							if(state[index][k] == 1 && state[i][k] == 1)
								matchRow[k] = 1;
						}
						
						if(value > cost[i])  //if the sum of cost of the replacement columns is bigger than current checking column, the current checking column cannot get removed 
						{
							isDuplicate = false;
						}
					}
				}
				
				if(!isDuplicate)
					break;
			}
			
			if(value < cost[i] && isDuplicate)  //remove the current checking column 
			{
				result[i] = -1;
			}
		}
		
		//reduce row if the columns covering it can cover another row
		for(int i = 0; i < numEle; i++)  //go through all rows
		{	
			if(coverCount[i] < 0) //if a row has been removed
				continue;
			
			for(int j = 0; j < numEle; j++)  //go through other rows
			{
				if(i == j || coverCount[j] < 0)   //if the same row or a row has been removed
					continue;
				
				boolean isMatch = true;  //check if all columns matches
				
				for(int k = 0; k < numSet; k++)  //go through all columns
				{	
					if(state[k][j] == 0 && state[k][i] != 0)  //if there is a column that can cover current checking row, but cannot cover candidate row  
					{
						isMatch = false;	//this candidate row doesn't match
						break;
					}
				}
				
				if(isMatch)  //if find match rows, remove current checking row
				{
					coverCount[i] = -1;
					break;
				}
			}
		}
		
		//initialize set
		for(int i = 0; i < numSet; i++)  //calculate how many rows one column can cover
		{
			if(result[i] < 0)  //if a column has been removed
			{
				continue;
			}
			
			int count = 0;
			for(int j = 0; j < numEle; j++)  //go through all rows
			{
				if(coverCount[j] < 0)
					continue;
				
				if(state[i][j] != 0)
				{
					count++;
				}
			}
			
			if(count == 0)   //if a column cannot cover any row, remove it
				result[i] = -2;
		}
		
		//initialize weight
		for(int i = 0; i < numEle; i++)   //calculate how many columns that can cover a row
		{
			if(coverCount[i] < 0)  //if a row has been removed
			{
				continue;
			}
			
			int count = 0;    
			for(int j = 0; j < numSet; j++)  //go through all columns
			{
				if(result[j] < 0)
					continue;
				
				if(state[j][i] != 0)
				{
					count++;
				}
			}
				
			weight[i] = count;
			
			if(count == 0)   //if no column can cover this row, remove this row
				coverCount[i] = -2;
		}
		
		QuicksortPrimitive.sort(sortedEle, weight);  //sort the rows by the number of columns that can cover them increasingly
			
		//greedy set covering
		for(int i = 0; i < numEle; i++)   //have each row get covered
		{
			int indexEle = sortedEle[i];   //obtain the sorted rows
			if(coverCount[indexEle] != 0)  //if a row has been covered or removed
				continue;
			
			int index = -1;     //the index of the column to cover current checking row
			if(weight[indexEle] == 1)   //if there is only one column to cover this row, include this column in the solution
			{
				for(int j = 0; j < numSet; j++)  //go through all columns to find out the unique column
				{
					if(result[j] != 0)
						continue;
					
					if(state[j][indexEle] != 0)
					{
						index = j;
						break;
					}
				}
			} else  
			{
				double min = Double.MAX_VALUE;    //the minimum of the greedy value
				double[] value = new double[numSet];  //the greedy value of all candidate columns
				int[] cover = new int[numSet];  //the number uncovered rows that candidate columns can cover
				int minCount = 0;  //the number of candidate columns
				int maxCover = 0;  //the maximum of uncovered rows can be covered
				
				for(int j = 0; j < numSet; j++)  //go through all columns
				{	
					if(result[j] != 0)   //if a column has been removed or included in the solution
						continue;
					
					if(state[j][indexEle] != 0)  //if a column can cover current checking row
					{
						for(int k = 0; k < numEle; k++)  //calculate how many uncovered rows this candidate column can cover
						{
							if(state[j][k] != 0 && coverCount[k] == 0)  
							{
								cover[j]++;
							}
						}
						
						if(cover[j] == 0)  
						{
							value[j] = Double.MAX_VALUE;
							continue;
						}
						
						value[j] = cost[j] / cover[j];  //calculate the greedy value of this candidate column  
						
						if(min > value[j] && Math.abs(min - value[j]) > 0.000001)  //find a column with least greedy value while max uncovered rows
						{
							min = value[j];   
							minCount = 1;
							index = j;
							maxCover = cover[j];
						} else if(Math.abs(min - value[j]) < 0.000001)
						{
							if(maxCover == cover[j])
								minCount++;
							else if(maxCover < cover[j])
							{
								minCount = 1;
								index = j;
								maxCover = cover[j];
							}
						}
					}
				}
				
				if(minCount > 1) //if only multiple candidate columns, randomly select one from them 
				{
					int rand = RND.nextInt(minCount); 

					for(int j = 0; j < numSet; j++)
					{
						if(result[j] != 0)
							continue;
						
						if(Math.abs(min - value[j]) <= 0.000001 && cover[j] == maxCover)
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
			}
			
			if(index >= 0)  //add this candidate column into solution, have all the rows it covers get covered
			{
				result[index] = 1;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0 && coverCount[l] >= 0)
					{
						coverCount[l]++;
					}
				}
			}
		}
		
		//if get the columns sorted by their cost, this process will be better
		while(true)  //remove the redundant columns
		{
			int[] duplicate = new int[numSet];  //all the max cost duplicate columns
			int duplicateCount = 0;  //the number of max cost duplicate columns
			int index = -1;    //the index of the last candidate duplicate column
			double max = 0;   //max cost
			int maxCount = 0;  //the number of candidate duplicate columns
			for(int i = 0; i < numSet; i++)	 //go through all the columns to find out duplicates
			{
				if(result[i] <= 0)
					continue;
				
				boolean isDuplicate = true;
				
				for(int j = 0; j < numEle; j++)
				{
					if(coverCount[j] <= 0)
						continue;
					
					if(state[i][j] != 0)
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
					duplicate[i] = 1;
					duplicateCount++;
					
					if(max < cost[i] && Math.abs(max - cost[i]) > 0.000001)  //find the max cost duplicate column
					{
						max = cost[i];
						maxCount = 1;
						index = i;
					} else if(Math.abs(max - cost[i]) < 0.000001)
					{
						maxCount++;
					}
				}
			}
			
			if(duplicateCount == 0)    //if no duplicates, stop this process, otherwise continue to find other less cost duplicates
				break;
			
			while(duplicateCount > 0)  //try to remove all the max cost duplicate columns
			{
				if(maxCount > 1)  //if more than one duplicate candidates, randomly select one from them to remove
				{
					int rand = RND.nextInt(maxCount);
					for(int k = 0;  k < numSet; k++)
					{
						if(duplicate[k] == 0)
							continue;
						
						if(Math.abs(max - cost[k]) <= 0.000001)
						{
							if(rand > 0)
							{
								rand--;
							} else
							{
								index = k;
								break;
							}
						}
					}
				}
				

				//remove the duplicate column
				result[index] = 0;
				duplicate[index] = 0;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0 && coverCount[l] > 0)
					{
						coverCount[l]--;
					}
				}
				
				int[] duplicateT = new int[numSet];  
				duplicateCount = 0;
				
				max = 0;
				maxCount = 0;
				index = -1;
				
				//check if one column removed, other duplicate candidates are still duplicates
				//this process need to get optimized
				for(int k = 0;  k < numSet; k++)
				{
					if(duplicate[k] == 0)
						continue;
					
					boolean isDuplicate = true;
					
					for(int j = 0; j < numEle; j++)
					{
						if(coverCount[j] <= 0)
							continue;
						
						if(state[k][j] != 0 )
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
						duplicateT[k] = 1;
						duplicateCount++;
						
						if(max < cost[k] && Math.abs(max - cost[k]) > 0.000001)
						{
							max = cost[k];
							maxCount = 1;
							index = k;
						} else if(Math.abs(max - cost[k]) < 0.000001)
						{
							maxCount++;
						}
					}
				}
				
				duplicate = duplicateT;
			} 
		} 
		
		return result;
	}
	
	
	//the same process as stochastic greedy set covering, except selecting the first column to include into solution or remove from solution, not randomly select one
	public static int[] firstGreedy (int[][] state, double cost[])
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		int[] coverCount = new int[numEle];
		int[] result = new int[numSet];
		double[] weight = new double[numEle];
		
		int[] sortedSet = new int[numSet]; 
		for(int i = 0 ; i < numSet; i++) 
			sortedSet[i] = i;
		
		int[] sortedEle = new int[numEle]; 
		for(int i = 0 ; i < numEle; i++) 
			sortedEle[i] = i;
		
		QuicksortPrimitive.sort(sortedSet, cost);
		QuicksortPrimitive.reverse(sortedSet);
		
		//reduce columns 1
		for(int i = 0; i < numSet; i++)
		{
			int index = sortedSet[i];
			
			if(result[index] < 0)
				continue;
			
			int j = 0;
			for(int k = 0; k < numSet; k++)
			{
				if(cost[sortedSet[k]] == cost[index])
				{
					j = k;
					break;
				}
			}
			
			for(; j < numSet; j++)
			{
				int indMatch = sortedSet[j];
				
				if(indMatch == index || result[indMatch] < 0)
					continue;
				
				boolean isMatch = true;
				
				for(int k = 0; k < numEle; k++)
				{	
					if(state[indMatch][k] == 0 && state[index][k] != 0)
					{
						isMatch = false;	
						break;
					}
				}
				
				if(isMatch)
				{
					result[index] = -1;
					break;
				}
			}
		}

		//reduce columns 2
		for(int i = 0; i < numSet; i++)
		{
			if(result[i] < 0)
				continue;
			
			double value = 0;
			int[] matchCol = new int[numSet];
			int[] matchRow = new int[numEle];
			boolean isDuplicate = true;
			
			for(int j = 0; j < numEle; j++)
			{
				if(matchRow[j] != 0)
					continue;
				
				if(state[i][j] != 0)
				{
					double min = Double.MAX_VALUE;
					int max = 0;
					int index = -1;
					
					for(int k = 0; k < numSet; k++)
					{
						if(result[k] < 0 || matchCol[k] != 0)
							continue;
						
						if(state[k][j] != 0 && k != i)
						{
							int count = 0;
							for(int l = 0; l < numEle; l++)
							{	
								if(state[k][l] == 1 && state[i][l] == 1)
								{
									count++;
								}
							}
							
							double match = cost[k] / count;
							
							if(min > match && Math.abs(min - match) > 0.000001)
							{
								min = match;
								index = k;
								max = count;
							} else if(Math.abs(min - match) < 0.000001)
							{
								if(max < count)
								{
									index = k;
									max = count;
								} 
							}
						}	
					}
					
					if(index < 0)
					{
						isDuplicate = false;
					} else
					{
						matchCol[index] = 1;
						value += cost[index];
						
						for(int k = 0; k < numEle; k++)
						{
							if(state[index][k] == 1 && state[i][k] == 1)
								matchRow[k] = 1;
						}
					}
				}
				
				if(!isDuplicate)
					break;
			}
			
			if(value < cost[i] && isDuplicate)
			{
				result[i] = -1;
			}
		}
		
		//reduce row
		for(int i = 0; i < numEle - 1; i++)
		{	
			if(coverCount[i] < 0)
				continue;
			
			for(int j = 0; j < numEle; j++)
			{
				if(i == j || coverCount[j] < 0)
					continue;
				
				boolean isMatch = true;
				
				for(int k = 0; k < numSet; k++)
				{	
					if(state[k][j] == 0 && state[k][i] != 0)
					{
						isMatch = false;	
						break;
					}
				}
				
				if(isMatch)
				{
					coverCount[i] = -1;
					break;
				}
			}
		}
		
		
		//initialize set
		for(int i = 0; i < numSet; i++)
		{
			if(result[i] < 0)
			{
				continue;
			}
			
			int count = 0;
			for(int j = 0; j < numEle; j++)
			{
				if(coverCount[j] < 0)
					continue;
				
				if(state[i][j] != 0)
				{
					count++;
				}
			}
			
			if(count == 0)
				result[i] = -2;
		}
		
		//initialize weight
		for(int i = 0; i < numEle; i++)
		{
			if(coverCount[i] < 0)
			{
				continue;
			}
			
			int count = 0;
			for(int j = 0; j < numSet; j++)
			{
				if(result[j] < 0)
					continue;
				
				if(state[j][i] != 0)
				{
					count++;
				}
			}
				
			weight[i] = count;
			
			if(count == 0)
				coverCount[i] = -2;
		}
		
		QuicksortPrimitive.sort(sortedEle, weight);
				
		for(int i = 0; i < numEle; i++)
		{
			int indexEle = sortedEle[i]; 
			if(coverCount[indexEle] != 0 || weight[indexEle] <= 0)
				continue;
			
			int index = -1;
			if(weight[indexEle] == 1)
			{
				for(int j = 0; j < numSet; j++)
				{
					if(result[j] != 0)
						continue;
					
					if(state[j][indexEle] != 0)
					{
						index = j;
						break;
					}
				}
			} else
			{
				double min = Double.MAX_VALUE;
				int maxCover = 0;
				
				for(int j = 0; j < numSet; j++)
				{	
					if(result[j] != 0)
						continue;
					
					double value = 0;
					int cover = 0;
					
					if(state[j][indexEle] != 0)
					{
						for(int k = 0; k < numEle; k++)
						{
							if(state[j][k] != 0 && coverCount[k] == 0)
							{
								cover++;
							}
						}
						
						if(cover == 0)
						{
							continue;
						}
						
						value = cost[j] / cover;
						
						if(min > value && Math.abs(min - value) > 0.000001)
						{
							min = value;
							index = j;
							maxCover = cover;
						} else if(Math.abs(min - value) < 0.000001)
						{
							if(maxCover < cover)
							{
								index = j;
								maxCover = cover;
							}
						}
					}
				}
			}
			
			if(index >= 0)
			{
				result[index] = 1;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0 && coverCount[l] >= 0)
					{
						coverCount[l]++;
					}
				}
			}
		}
		
		while(true)
		{
			int[] duplicate = new int[numSet];
			int duplicateCount = 0;
			int index = -1;
			double max = 0; 
			for(int i = 0; i < numSet; i++)	
			{
				if(result[i] <= 0)
					continue;
				
				boolean isDuplicate = true;
				
				for(int j = 0; j < numEle; j++)
				{
					if(coverCount[j] <= 0)
						continue;
					
					if(state[i][j] != 0)
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
					duplicate[i] = 1;
					duplicateCount++;
					
					if(max < cost[i] && Math.abs(max - cost[i]) > 0.000001)
					{
						max = cost[i];
						index = i;
					} 
				}
			}
			
			if(duplicateCount == 0)
				break;
			
			while(duplicateCount > 0)
			{
				result[index] = 0;
				duplicate[index] = 0;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0 && coverCount[l] > 0)
					{
						coverCount[l]--;
					}
				}
				
				int[] duplicateT = new int[numSet];
				duplicateCount = 0;
				
				max = 0;
				index = -1;
				
				for(int k = 0;  k < numSet; k++)
				{
					if(duplicate[k] == 0)
						continue;
					
					boolean isDuplicate = true;
					
					for(int j = 0; j < numEle; j++)
					{
						if(coverCount[j] <= 0)
							continue;
						
						if(state[k][j] != 0 )
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
						duplicateT[k] = 1;
						duplicateCount++;
						
						if(max < cost[k] && Math.abs(max - cost[k]) > 0.000001)
						{
							max = cost[k];
							index = k;
						} 
					}
				}
				
				duplicate = duplicateT;
			} 
		} 
		
		return result;
	}
}

