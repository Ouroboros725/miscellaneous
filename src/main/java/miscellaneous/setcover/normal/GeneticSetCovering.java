package miscellaneous.setcover.normal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

//genetic set covering based on J.E. Beasley's method
public class GeneticSetCovering {
	private static final Random RND = new Random();  //random operator
	
	public static class Set   //for sorting purpose
	{
		public double cost = 0;
		public int count = 0;
	}
	
	public static final Comparator<Set> setCmp = new Comparator<Set>()  //sorting comparator for class Set
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
	
	public static final Comparator<Integer> intCmp = new Comparator<Integer>() //sorting comparator for Integer
	{
		public int compare(Integer a, Integer b)
		{
			return a.compareTo(b);
		}
	};
	
	//fitness function calculating the number of duplicate elements
	private static double fitness(int[] candidate, int[][] state, int[] eleEle)  
	{
		double result = 0;
		int[] coverCount = new int[eleEle.length];
		
		for(int i = 0; i < candidate.length; i++)
		{
			if(candidate[i] == 1)
			{
				for(int j = 0; j < eleEle.length; j++)
				{
					if(eleEle[j] != 0)
						continue;
					if(state[i][j] != 0)
					{
						coverCount[j]++;
					}
				}
			}
		}
		
		for(int i = 0; i < coverCount.length; i++)
		{
			if(coverCount[i] > 1)
				result += (coverCount[i] - 1);
		}
		
		return result;
	}
	
	//corssover two results
	private static int[] crossover(int[] p1, int[] p2, double f1, double f2)
	{
		int l = p1.length;
		int[] c = new int[l];
		
		double t = f2 / (f1 + f2);
		
		for(int i = 0; i < l; i++)
		{
			if(p1[i] == p2[i])
				c[i] = p1[i];
			else
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
	
	//check if one result is duplicated
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
	
	//make one result feasible (all elements covered)
	private static void feasible(int[] candidate, int[][] state, double[] cost, int[] coverCount, Integer[] sortedEle, Integer[] sortedSet)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
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
	
	//tournament method selecting two results to crossover
	private static int[] tournament(int[][] population, double[] fitness, int size)
	{
		int[] result = new int[2];
		
		int[][] pool = new int[2][size];
		
		HashSet<Integer> s = new HashSet<Integer>();
		
		int i = 0;
		while(i < size)
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
		
		for(int j = 0; j < population[0].length; j++)
		{
			result[0] = index1;
			result[1] = index2;
		}
		
		return result;
	}
	
	//generate the initial population
	private static void population(int[][] state, int[] eleEle, Integer[] sortedSet, int[] weight, int[][] result)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		for(int i = 0; i < result.length; i++)
		{
			int[] coverCount  = new int[numEle];
			for(int j = 0 ; j < numEle; j++)
			{
				if(eleEle[j] != 0)
				{
					coverCount[j] = -1;
				}
			}
			
			for(int j = 0; j < numEle; j++)
			{
				if(coverCount[j] != 0)
					continue;
				
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
	
	//mutate one result
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
	
	//main process of genetic set covering
	public static int[] genetic(int[][] state, double cost[])
	{
		int numSet = state.length;     //number of sets to cover elements
		int numEle = state[0].length;  //number of elements to be covered
		double totalFit = 0;  //the sum of fitness value of all results in population
		double avgFit = 0;   //the average of fitness value
		int belowFit = 0;   //the number of results below the average fitness value
		double bestFit = Double.MAX_VALUE;  //the best fitness value so far
		int result = -1;  //the index of the final result set in the population
		
		Integer[] sortedSet = new Integer[numSet];   //sorted sets
		for(int i = 0; i < numSet; i++)
			sortedSet[i] = i;
		
		Integer[] sortedEle = new Integer[numEle];   //sorted elements
		for(int i = 0; i < numEle; i++)
			sortedEle[i] = i;
		
		int[] eleEle = new int[numEle];  //sets that never cover any elements
		int[] eleSet = new int[numSet];  //elements that never being covered
		
		int[] weightSet = new int[numSet];   //the number of elements one set cover
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
		
		int[] weightEle = new int[numEle];  //the number of sets that cover one element
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
		
		HashMap<Integer, Integer> cmpSet = new HashMap<Integer, Integer>();
		for(int i = 0; i < numSet; i++)
		{
			cmpSet.put(i, (int)cost[i]);
		}
		
		HashMap<Integer, Integer> cmpEle = new HashMap<Integer, Integer>();
		for(int i = 0; i < numEle; i++)
		{
			cmpEle.put(i, weightEle[i]);
		}
		
		new Quicksort<Integer, Integer>().sort(sortedSet, cmpSet, intCmp);  //sort the sets
		new Quicksort<Integer, Integer>().sort(sortedEle, cmpEle, intCmp);  //sort the elements
		
		int[] mutate = new int[numSet];  //select the sets that are worth mutating
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
		
		int[][] population = new int[100][numSet]; //population
		for(int i = 0; i < numSet; i++)  //eliminate the useless sets
		{
			if(eleSet[i] != 0)
			{
				for(int j = 0; j < 100; j++)
				{
					population[j][i] = -1;
				}
			}
		}
		
		population(state, eleEle, sortedSet, weightEle, population);  //generate the inital population
		
		double[] fitness = new double[100];
		for(int i = 0; i < 100; i++)
		{
			fitness[i] = fitness(population[i], state, eleEle);
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
				result = i;
			}
		}
		
		int t = 0;
		while(t < 100000)  //continue the new result generating process until 100000 non-duplicate results were generated
		{
			int[] parents = tournament(population, fitness, 2);  //select two results to crossover
			
			//crossover
			int[] candidate = crossover(population[parents[0]], population[parents[1]], fitness[parents[0]], fitness[parents[1]]);
			
			mutation(10, 200, 1.3, t, candidate, mutate); //mutate
			
			int[] coverCount = new int[numEle];  //calculate the number of sets that cover each element
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
			
			feasible(candidate, state, cost, coverCount, sortedEle, sortedSet);  //make all elements covered
			
			if(!duplicate(population, candidate))  //if the new result is not duplicated
			{
				int index = -1;
				int k = RND.nextInt(belowFit);
				
				for(int i = 0; i < 100; i++)  //random select one result in the population with a below average fitness value to be replaced by the new result
				{
					if(fitness[i] > avgFit && k > 0)
					{
						k--;
					} else if(fitness[i] > avgFit && k == 0)
					{
						index = i;
					}
				}
				
				//update the information of the population
				totalFit -= fitness[index];
				population[index] = candidate;
				fitness[index] = fitness(candidate, state, eleEle);
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
					bestFit = fitness[index];
					result = index;
				}
				
				t++;
			}
		}
		
		return population[result];
	}
}

