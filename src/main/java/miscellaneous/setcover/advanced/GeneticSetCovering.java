package miscellaneous.setcover.advanced;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

//J.E. Beasley's Genetic Set Covering algorithm
public class GeneticSetCovering {
	private static final Random RND = new Random();  //random operator
	
	//fitness function calculate the cost of a solution
	private static double fitness(int[] candidate, double[] cost)
	{
		double result = 0;
		
		for(int i = 0; i < candidate.length; i++)
		{
			if(candidate[i] < 0)
				continue;
			
			result += candidate[i] * cost[i];
		}
		
		return result;
	}
	
	//crossover process
	//p1: parent1
	//p2: parent2
	//f1: parent1's fitness value
	//f2: parents'2 fitness value
	private static int[] crossover(int[] p1, int[] p2, double f1, double f2)
	{
		int l = p1.length;
		int[] c = new int[l];
		
		double t = f2 / (f1 + f2); //probability to select the gene from p1
		
		for(int i = 0; i < l; i++)  //update all genes (bits)
		{
			if(p1[i] == p2[i]) //if the genes from two parents are the same, choose them 
				c[i] = p1[i];
			else  //otherwise select the gene with certain probability
			{
				double p = RND.nextDouble();
				if( p < t)
					c[i] = p1[i];
				else
					c[i] = p2[i];
			}
		}
		
		return c;
	}
	
	//check if a new solution has already in the current population, previous removed solutions will not be counted
	private static boolean duplicate(int[][] population, int[] candidate)
	{
		boolean result = false;
		
		for(int i = 0; i < population.length; i++)
		{
			boolean isDuplicate = true;
			for(int j = 0; j < candidate.length; j++)
			{
				if(population[i][j] != candidate[j])
				{
					isDuplicate = false;
					break;
				}
			}
			if(isDuplicate)
			{
				result = true;
				break;
			}
		}
			
		return result;
	}
	
	//make a solution feasible, greedy way to have the uncovered rows get covered 
	//candidate: candidate solution
	//state: problem space
	//cost: cost of all columns
	//eleSet: removed columns
	//eleEle: removed rows 
	//sortedEle: rows sorted  
	//sortedSet: columns sorted
	private static void feasible(int[] candidate, int[][] state, double[] cost, int[] eleSet, int[] eleEle, Integer[] sortedEle, Integer[] sortedSet)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		//remove the removed rows
		int[] coverCount = new int[numEle];  //the number of columns that currently cover a row
		for(int i = 0; i < numEle; i++)
		{
			if(eleEle[i] != 0)
				coverCount[i] = -1;
		}
		
		//find out the uncovered rows
		for(int i = 0; i < numSet; i++)
		{
			if(eleSet[i] != 0)
				continue;
			if(candidate[i] > 0)
			{
				for(int j = 0; j < numEle; j++)
				{
					if(eleEle[j] != 0)
						continue;
					
					if(state[i][j] != 0)
						coverCount[j]++;
				}
			}
		}
		
		//first greedy set covering method to cover uncovered rows
		for(int i = 0; i < sortedEle.length; i++)
		{
			int indEle = sortedEle[i];
			if(coverCount[indEle] != 0)
				continue;

			double min = Double.MAX_VALUE; 
			int index = -1;
			for(int j = 0; j < numSet; j++)
			{
				int indSet = sortedSet[j];
				if(candidate[indSet] != 0)
					continue;
				
				int count = 0;
				if(state[indSet][indEle] != 0)
				{
					for(int k = 0; k < numEle; k++)
					{
						if(state[indSet][k] != 0 && coverCount[k] == 0)
						{
							count++;
						}
					}
					double value = cost[indSet] / count;
					
					if(min > value)
					{
						min = value;
						index = indSet;
					} 
				}
			}
			
			if(index >= 0)
			{
				candidate[index] = 1;
				for(int j = 0; j < numEle; j++)
				{
					if(state[index][j] != 0 && coverCount[j] >= 0)
					{
						coverCount[j]++;
					}
				}
			}
		}

		//remove the duplicate columns in the solution
		for(int i = 0; i < numSet; i++)
		{
			int index = sortedSet[numSet-i-1];
			if(candidate[index] <= 0)
				continue;
			
			boolean isDuplicate = true;
			
			for(int j = 0; j < numEle; j++)
			{
				if(coverCount[j] <= 0)
					continue;
				
				if(state[index][j] != 0)
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
				candidate[index] = 0;
				for(int l = 0; l < numEle; l++)
				{
					if(state[index][l] != 0 && coverCount[l] > 0)
					{
						coverCount[l]--;
					}
				}
			}
		} 
	}
	
	//binary tournament way to select two solutions from the population to crossover
	private static int[] tournament(int[][] population, double[] fitness, int size)
	{
		int[] result = new int[2];
		
		int[][] pool = new int[2][size];
		
		HashSet<Integer> s = new HashSet<Integer>();
		
		int i = 0;
		while(i < size)  //randomly divide the population into two candidate pools
		{
			int k = RND.nextInt(population.length);
			int j = RND.nextInt(population.length);
			if(s.add(k) && s.add(j))
			{
				int l =RND.nextInt(2);
				pool[l][i] = k;
				pool[1-l][i] = j;
				i++;
			} else
			{
				s.remove(k);
				s.remove(j);
			}
		}
		
		//select one best candidate from each candidate pool
		double max1 = 0;
		double max2 = 0;
		int index1 = 0;
		int index2 = 0;
		for(int j = 0; j < size; j++)
		{
			double fit1 = fitness[pool[0][j]];
			double fit2 = fitness[pool[1][j]];
			
			if(max1 < fit1)
			{
				max1 = fit1;
				index1 = pool[0][j];
			}
			
			if(max2 < fit2)
			{
				max2 = fit2;
				index2 = pool[1][j];
			}
		}
		
		//generate the parents to crossover
		for(int j = 0; j < population[0].length; j++)
		{
			result[0] = index1;
			result[1] = index2;
		}
		
		return result;
	}
	
	//generate the initial population
	//state: problem space
	//eleEle: removed rows
	//sortedSet: sorted columns
	//weight: the number of columns that cover a row
	//result: result population
	private static void population(int[][] state, int[] eleEle, Integer[] sortedSet, int[] weight, int[][] result)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		for(int i = 0; i < result.length; i++)  //generate every solution in population
		{
			//eliminate the removed rows
			int[] coverCount  = new int[numEle];  //the number of columns that currently cover a row
			for(int j = 0 ; j < numEle; j++)
			{
				if(eleEle[j] != 0)
				{
					coverCount[j] = -1;
				}
			}
			
			//find a column to cover a row
			for(int j = 0; j < numEle; j++)
			{
				if(coverCount[j] != 0)
					continue;
				
				//randomly find the column among elite columns
				int k = 0;
				if(weight[j] >= 5)
				{
					k = RND.nextInt(5);
				} else
				{
					k = RND.nextInt(weight[j]);
				}
					
				for(int l = 0; l < numSet; l++)
				{
					int index = sortedSet[l];
					if(result[i][index] != 0)
						continue;
					
					if(state[index][j] != 0 && k >0)
					{
						k--;
					} else if(state[index][j] != 0 && k == 0)
					{
						result[i][index] = 1;
						for(int m = 0; m < numEle; m++)
						{
							if(state[index][m] != 0 && coverCount[m] >= 0)
								coverCount[m]++;
						}
						break;
					}
				}
			}
			
			//count the number of columns in the solution
			int k = 0;
			int[] c = new int[numSet];
			for(int j = 0; j < numSet; j++)
			{
				if(result[i][j] == 1)
				{
					c[j] = 1;
					k++;
				}
			}
			
			//randomly go through all columns in the solution, remove the duplicates
			while(k > 0)
			{
				int d = RND.nextInt(k);
				for(int j = 0; j < numSet; j++)
				{
					if(c[j] == 1 && d > 0)
					{
						d--;
					} else if(c[j] == 1 && d == 0)
					{
						boolean isDuplicate = true;	
						for(int m = 0; m < numEle; m++)
						{
							if(coverCount[m] <= 0)
								continue;
							
							if(state[j][m] != 0)
							{
								if(coverCount[m] < 2)
								{
									isDuplicate = false;
									break;
								}
							}
						}
							
						if(isDuplicate)
						{
							result[i][j] = 0;
							for(int l = 0; l < numEle; l++)
							{
								if(state[j][l] != 0 && coverCount[l] > 0)
								{
									coverCount[l]--;
								}
							}
						}
						
						c[j] = 0;
						k--;
					}	
				}
			}
		}
	}
	
	//mutation
	//mutate the elite columns with a certain probability, according to the number of new descendants generated
	private static void mutation(int mf, int mc, double mg, int t, int[] candidate, int[] mutate)
	{	
		double rate = Math.ceil(mf / (1 + Math.exp( - 4 * mg * (t - mc) / mf))) / (candidate.length * 1.0);
		
		for(int i = 0; i < mutate.length; i++)
		{
			if(mutate[i] != 0)
			{
				double r = RND.nextDouble();
				if(r < rate)
					candidate[i] = 1 - candidate[i];
			}
		}
	}
	
	//main genetic algorithm process
	public static double[] genetic(int[][] state, double cost[], double[] parameters)
	{
		//record the start cpu time
		long time = 0;
		long bestTime = 0;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    if(bean.isCurrentThreadCpuTimeSupported())
	        time = bean.getCurrentThreadCpuTime();
	    
	    //get the mutation parameters
		int mf = (int)parameters[0];
		int mc = (int)parameters[1]; 
		double mg = parameters[2];
		
		int numSet = state.length;  //number of columns
		int numEle = state[0].length;  //number of rows
		double totalFit = 0;  //total fitness value of the population
		double avgFit = 0;  //the average fitness value of the population
		int belowFit = 0;  //the number of solutions in the population with a fitness below the average fitness of the population 
		double bestFit = Double.MAX_VALUE;  //the best fitness value of the population
		double[] results = new double[3];  //the results of the genetic set covering
		
		//sort the columns by their costs increasingly, same costs by the number of rows they can cover decreasingly
		Integer[] sortedSet = new Integer[numSet];
		for(int i = 0; i < numSet; i++)
			sortedSet[i] = i;
		
		//sort the rows by the number of columns that can cover them
		Integer[] sortedEle = new Integer[numEle];
		for(int i = 0; i < numEle; i++)
			sortedEle[i] = i;
		
		int[] eleEle = new int[numEle];  //the rows removed
		int[] eleSet = new int[numSet];  //the columns removed
		
		//the number of columns that cover a row
		int[] weightSet = new int[numSet];
		for(int i = 0; i < numSet; i++)
		{
			for(int j = 0; j < numEle; j++)
			{
				if(state[i][j] != 0)
				{
					weightSet[i]++;
				}
			}
			
			if(weightSet[i] == 0)
				eleSet[i] = 1;
		}
		
		//the number of rows that a column can cover
		int[] weightEle = new int[numEle];
		for(int i = 0; i < numEle; i++)
		{
			for(int j = 0; j < numSet; j++)
			{
				if(state[j][i] != 0)
				{
					weightEle[i]++;
				}
			}
			
			if(weightEle[i] == 0)
				eleEle[i] = 1;
		}
		
		// sort the rows and columns
		HashMap<Integer, Tools.Set> cmpSet = new HashMap<Integer, Tools.Set>();
		for(int i = 0; i < numSet; i++)
		{
			Tools.Set s = new Tools.Set();
			s.cost = cost[i];
			s.count = weightSet[i];
			cmpSet.put(i, s);
		}
		
		HashMap<Integer, Integer> cmpEle = new HashMap<Integer, Integer>();
		for(int i = 0; i < numEle; i++)
		{
			cmpEle.put(i, weightEle[i]);
		}
		
		new Quicksort<Tools.Set, Integer>().sort(sortedSet, cmpSet, Tools.setCmp);
		new Quicksort<Integer, Integer>().sort(sortedEle, cmpEle, Tools.intCmp);
		
		//generate the elite columns
		int[] mutate = new int[numSet];
		for(int i = 0; i < numEle; i++)
		{
			if(eleEle[i] != 0)
				continue;
			
			int k = 0;
			if(weightEle[i] >= 5)
				k = 5;
			else
				k = weightEle[i];
			
			for(int j = 0 ; j < numSet; j++)
			{
				int index = sortedSet[j];
				if(eleSet[i] != 0)
					continue;
				
				if(state[index][i] != 0)
				{
					mutate[index] = 1;
					k--;
				}
				if(k == 0)
					break;
			}
		}
		
		//generate the initial population
		int[][] population = new int[100][numSet];
		for(int i = 0; i < numSet; i++)
		{
			if(eleSet[i] != 0)
			{
				for(int j = 0; j < 100; j++)
				{
					population[j][i] = -1;
				}
			}
		}
		
		population(state, eleEle, sortedSet, weightEle, population);
		
		//calculate the fitness information of the population
		double[] fitness = new double[100];
		for(int i = 0; i < 100; i++)
		{
			fitness[i] = fitness(population[i], cost);
			totalFit += fitness[i];
		}
		
		avgFit = totalFit / 100;
		for(int i = 0; i < 100; i++)
		{
			if(fitness[i] > avgFit)
				belowFit++;
			else if(fitness[i] < bestFit)
			{
				bestFit = fitness[i];
			}
		}
		
		//main genetic body
		int t = 0;  //terminate until 100000 new descendants generated
		while(t < 100000)
		{
			int[] parents = tournament(population, fitness, 2); //tournament generate two parents
			
			//crossover to generate the child 
			int[] candidate = crossover(population[parents[0]], population[parents[1]], fitness[parents[0]], fitness[parents[1]]);
			
			//mutate the child
			mutation(mf, mc, mg, t, candidate, mutate);
			
			//make the child a feasible solution
			int[] coverCount = new int[numEle];
			for(int i = 0; i < numEle; i++)
			{
				if(eleEle[i] != 0)
					coverCount[i] = -1;
			}
			for(int i = 0; i < numSet; i++)
			{
				if(eleSet[i] != 0)
					continue;
				if(candidate[i] > 0)
				{
					for(int j = 0; j < numEle; j++)
					{
						if(eleEle[j] != 0)
							continue;
						
						if(state[i][j] != 0)
							coverCount[j]++;
					}
				}
			}
			
			feasible(candidate, state, cost, eleSet, eleEle, sortedEle, sortedSet);
			
			//check if the child is a duplicate of the population, if it is not, update the fitness information of the population
			//randomly select a solution in population with a below average fitness value, replace this solution with the new child 
			if(!duplicate(population, candidate))
			{
				int index = -1;
				int k = RND.nextInt(belowFit);
				
				for(int i = 0; i < 100; i++)
				{
					if(fitness[i] > avgFit && k > 0)
					{
						k--;
					} else if(fitness[i] > avgFit && k == 0)
					{
						index = i;
					}
				}
				
				totalFit -= fitness[index];
				population[index] = candidate;
				fitness[index] = fitness(candidate, cost);
				totalFit += fitness[index];
				avgFit = totalFit / 100;
				belowFit = 0;
				for(int i = 0; i < 100; i++)
				{
					if(fitness[i] > avgFit)
						belowFit++;
				}
				if(fitness[index] < bestFit)
				{
					//record the cpu time that find the best solution
					if(bean.isCurrentThreadCpuTimeSupported())
				        bestTime = bean.getCurrentThreadCpuTime() - time;
					bestFit = fitness[index];
				}
				
				t++;
			}
		}
		
		//calculate the end cpu time
		if(bean.isCurrentThreadCpuTimeSupported())
	        time = bean.getCurrentThreadCpuTime() - time;
		
		//return the cpu time and best result
		results[0] = bestFit;
		results[1] = bestTime;
		results[2] = time;
		
		return results;
	}
}

