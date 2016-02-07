package miscellaneous.setcover.advanced;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Random;

public class PSOSetCovering {

	private static final Random RND = new Random(); //random operator
	
	//calculate the fitness value, the cost of a solution
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
	
	//update the velocity and position of a particle
	//candidate: a particle in population
	//velocity: the velocity of this particle
	//pl: this particle's best ever position
	//pg: whole population's best ever position
	//pn: this particle's neighbor's best ever position
	//w, c1, c2, c3, vmax: control parameters
	//mutate: elite columns
	private static void updateParticle(int[] candidate, double[] velocity, int[] pl, int[] pg, int[] pn, double w, double c1, double c2, double c3, double vmax, int[] mutate)
	{
		int numSet = candidate.length;  //the number of columns
		
		for(int i = 0; i< numSet; i++)  //update each bit of a solution
		{
			if(candidate[i] < 0)  //if a column is removed
				continue;
			
			//velocity update function and random parameters
			double r1 = RND.nextDouble();
			double r2 = RND.nextDouble();
			double r3 = RND.nextDouble();
			double vid = w * velocity[i] + c1 * r1 * (pl[i] - candidate[i]) + c2 * r2 * (pg[i] - candidate[i]) + c3 * r3 * (pn[i] - candidate[i]);
			
			//restrict the max velocity on the elite columns
			if(mutate[i] == 1)
			{
				if(vid > vmax)
				{
					vid = vmax;
				} else if(vid < -vmax)
				{
					vid = -vmax;
				}
			}
			
			//update the velocity and position
			velocity[i] = vid;
			double poss = 1.0 / (1 + Math.exp(-vid));
			
			if(RND.nextDouble() < poss)
			{
				candidate[i] = 1;
			} else
			{
				candidate[i] = 0;
			}
		}
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

	//make a solution feasible, greedy way to have the uncovered rows get covered 
	//candidate: candidate solution
	//state: problem space
	//cost: cost of all columns
	//eleSet: removed columns
	//eleEle: removed rows 
	//sortedEle: rows sorted  
	//sortedSet: columns sorted
	private static void feasible(int[] candidate, int[][] state, double[] cost, int[] eleSet,int[] eleEle, Integer[] sortedEle, Integer[] sortedSet)
	{
		int numSet = state.length;
		int numEle = state[0].length;
		
		//remove the removed rows
		int[] coverCount = new int[numEle];  //the number of columns that currently cover a row
		for(int j = 0; j < numEle; j++)
		{
			if(eleEle[j] != 0)
				coverCount[j] = -1;
		}
		
		//find out the uncovered rows
		for(int j = 0; j < numSet; j++)
		{
			if(eleSet[j] != 0)
				continue;
			if(candidate[j] > 0)
			{
				for(int k = 0; k < numEle; k++)
				{
					if(eleEle[k] != 0)
						continue;
					
					if(state[j][k] != 0)
						coverCount[k]++;
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

	//main pso process
	public static double[] pso(int[][] state, double[] cost, double[] parameters)
	{
		//record the inital cpu time
		long time = 0;
		long bestTime = 0;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    if(bean.isCurrentThreadCpuTimeSupported())
	        time = bean.getCurrentThreadCpuTime();
		
	    //record the results
		double[] results = new double[20];
		
		int numSet = state.length;  //number of columns
		int numEle = state[0].length; //number of rows
		
		//get the input parameters
		int size = (int)parameters[0]; //the size of the population
		int ite = (int)parameters[1];  //non-improvement iterations to terminate the loop
		double w = parameters[2];  //control parameter
		double c1 = parameters[3]; //control parameter
		double c2 = parameters[4]; //control parameter
		double c3 = parameters[5]; //control parameter
		double vmax = parameters[6]; //max velocity restriction
		//decided to use local PSO, commented the code to control the mode of pso
		//--------------------------------------------------------------------------------------------------
		//int modeShift = (int)parameters[7];
		//int mode = (int)parameters[8];
		//--------------------------------------------------------------------------------------------------
		int reduce = (int)parameters[9];  //whether to reduce the problem instance

		int[] eleEle = new int[numEle];  //rows removed
		int[] eleSet = new int[numSet];  //columns removed
		
		//---------------------------------------------------------------------
		//reduce columns, find if other columns can cover the rows that covered by one column with less cost
		int reduce2 = 0;
		if(reduce == 1)
		{
			//record the cpu time to start reduce 
			long timeR1 = 0;
			if(bean.isCurrentThreadCpuTimeSupported())
		        timeR1 = bean.getCurrentThreadCpuTime();
			
			//reduce columns 
			for(int i = 0; i < numSet; i++)
			{
				if(eleSet[i] > 0)   //if current checking column has been reduced
					continue;
				
				double value = 0;
				int[] matchCol = new int[numSet];  //columns that can be used to replace current checking column
				int[] matchRow = new int[numEle];   //rows that can get covered by other columns
				boolean isDuplicate = true;
				
				for(int j = 0; j < numEle; j++)  //check the rows that get covered by current checking column
				{
					if(matchRow[j] != 0)   //if current checking row has been get covered by other columns
						continue;
					
					if(state[i][j] != 0)  //if current checking row can get covered by current checking column
					{
						double min = Double.MAX_VALUE;  //the least greedy value of candidate columns  
						int max = 0;      //the max number of rows that can get covered by both candidate column and current checking column
						int index = -1;   //the index of the candidate column that can be used to cover current checking row 
						
						for(int k = 0; k < numSet; k++)    //go through all columns to find a least cost column to cover current checking row
						{
							if(eleSet[k] > 0 || matchCol[k] != 0)  //if a column has been reduced or has been used to cover this column
								continue;
							
							if(state[k][j] != 0 && k != i)   //if find a different column to cover current checking row
							{
								int count = 0;    //the number of rows that this candidate column and current checking column can both cover
								for(int l = 0; l < numEle; l++)
								{	
									if(state[k][l] == 1 && state[i][l] == 1)
									{
										count++;
									}
								}
								
								double match = cost[k] / count;  //greedy value
								
								if(min > match && Math.abs(min - match) > 0.000001)  //find the least greedy value candidate column
								{
									min = match;
									index = k;
									max = count;
								} else if(Math.abs(min - match) < 0.000001)   //if greedy value is the same, select the candidate column that can maximize the common coverage
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
						} else  //include the candidate column in replacement set 
						{
							matchCol[index] = 1;
							value += cost[index];
							
							for(int k = 0; k < numEle; k++)  //record the common covered rows
							{
								if(state[index][k] == 1 && state[i][k] == 1)
									matchRow[k] = 1;
							}
							
							if(value > cost[i])  //if the sum of cost of the replacement columns is bigger than current checking column, the current checking column cannot get removed 
								isDuplicate = false;
						}
					}
					
					if(!isDuplicate)
						break;
				}
				
				if(value < cost[i] && isDuplicate)  //remove the current checking column 
				{
					eleSet[i] = 1;
				}
			}
			
			
			//count the number of columns get removed
			for(int i = 0; i < numSet; i++)
			{
				if(eleSet[i] > 0)
					reduce2++;
			}
			
			//record the result
			results[0] = reduce2;
			
			//record the cpu time used to reduce problem
			long timeR2 = 0;
			if(bean.isCurrentThreadCpuTimeSupported())
		        timeR2 = bean.getCurrentThreadCpuTime();
			
			results[1] = timeR2 - timeR1;
		}
		//---------------------------------------------------------------------------------------------
		
		//sort the columns by their costs increasingly, same costs by the number of rows they can cover decreasingly
		Integer[] sortedSet = new Integer[numSet];
		for(int i = 0; i < numSet; i++)
			sortedSet[i] = i;
		
		//sort the rows by the number of columns that can cover them
		Integer[] sortedEle = new Integer[numEle];
		for(int i = 0; i < numEle; i++)
			sortedEle[i] = i;
		
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
		
		//generate the initial population
		int[][] population = new int[size][numSet];
		for(int i = 0; i < numSet; i++)
		{
			if(eleSet[i] == 1)
			{
				for(int j = 0; j < size; j++)
				{
					population[j][i] = -1;
				}
			}
			
			if(eleSet[i] == 2)
			{
				for(int j = 0; j < size; j++)
				{
					population[j][i] = 1;
				}
			}
		}
		population(state, eleEle, sortedSet, weightEle, population);
		
		double bestFit = Double.MAX_VALUE;   //the best fitness value of the swarm
		int[][] pl = new int[size][numSet];   //best ever positon of each particle
		int[] pg = new int[numSet];        //best position of the swarm
		double[] fitness = new double[size];  //fitness value of each particle
		
		//calculate the fitness information, PSO information of the population
		int resultI = -1;
		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < numSet; j++)
			{
				pl[i][j] = population[i][j];
			}
			
			fitness[i] = fitness(population[i], cost);
			if(fitness[i] < bestFit)
			{
				resultI = i;
				bestFit = fitness[i];
			}
		}

		for(int i = 0; i < numSet; i++)
		{
			pg[i] = population[resultI][i];
		}
		
		double[][] velocity = new double[size][numSet];  //velocity of each particle
		
		//information about the initial population
		results[2] = Tools.average(fitness);  //average fitness
		results[3] = Tools.stdDev(fitness);  //standard deviation of fitness
		results[4] = Tools.max(fitness);    //max fitness
		results[5] = Tools.min(fitness);   //mini fitness
		
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
		
		int t = 0;  //total number of iterations takes
		int c = 0;  //variable to judge the termination criteria
		int f = 0;  //number of iterations takes to find the best solution
		int v = 0;  //number of iterations takes the swarm to converge
		//int o = 0;
		double verg = 0;  //the fitness value that the swarm start to converge to
		
		//the neighborhood of each particle, decide by their order
		int[] k = new int[size];
		for(int i = 0; i < size; i++)
		{
			k[i] = (i+1)%size;
		}
		
		//decided to use local PSO, commented the code to control the mode of pso
		//--------------------------------------------------------------------------------------------------
		//boolean shift = false;
		//boolean shifted = false;
		//--------------------------------------------------------------------------------------------------
		
		//main pso body
		while(c < ite)  //termination criteria
		{
			int resultT = -1; //if the best solution of the swarm improved during this iteration, record it
			
			for(int i = 0; i < size; i++) // update all particles' position and velocity
			{
				//update a particle
				updateParticle(population[i], velocity[i], pl[i], pg, pl[k[i]], w, c1, c2, c3, vmax, mutate);
				
				//make a particle feasible
				feasible(population[i], state, cost, eleSet, eleEle, sortedEle, sortedSet);
				
				//calculate a particle's fitness value
				double fitnessT = fitness(population[i], cost);
				
				//check if a particle's best ever position improved
				if(fitnessT < fitness[i])
				{
					fitness[i] = fitnessT;
					for(int j = 0; j < numSet; j++)
					{
						pl[i][j] = population[i][j];
					}
				}
				
				//check if the swarm's best ever position improved
				if(fitnessT <  bestFit)
				{
					bestFit = fitnessT;
					resultT = i;
				}
			}
			
			t++;
			c++;
			if(resultT > 0)  //if the swarm's best ever position improved, update it
			{
				//record the cpu time to find the best solution
				if(bean.isCurrentThreadCpuTimeSupported())
			        bestTime = bean.getCurrentThreadCpuTime() - time;
				c = 0;
				f = t;
				for(int i = 0; i < numSet; i++)
				{
					pg[i] = population[resultT][i];
				}				
			}
			
			
			if(v == 0)  //record the iteration tha the swarm start to converge
			{
				double max = 0;
				double min = Double.MAX_VALUE;
				for(int i = 0; i < size; i++)
				{
					if(fitness[i] < min)
						min = fitness[i];
					if(fitness[i] > max)
						max = fitness[i];
				}
				
				if((max - min) < 10)
				{
					v = t;
					verg = bestFit;			
				}
			}
			
			//decided to use local PSO, commented the code to control the mode of pso
			//--------------------------------------------------------------------------------------------------
			//mode 1: global first, after converging local order
			//mode 2: golbal first, after converging local Euclidean
			//mode 3: local Euclidean
			/*if((mode == 1 || mode == 2) && v > 0 && !shifted)  //if the swarm converged, change the neighborhood 
			{
				if(mode == 2)     //update the Euclidean distance and neighborhood every iteration
					shift = true;

				if(o==0)   //iteration that start to change mode
					o = t;
				c3 = c2;
				c2 = 0.0;
				shifted = true;
			} 
			
			if(shift || mode == 3)  //update the Eclidean distance and neighborhood
			{
				for(int i = 0; i < size; i++)
				{
					int max = 0;
					for(int j = 0; j < size; j++)
					{
						if(i == j)
							continue;
						int value = 0;
						for(int l = 0; l < numSet; l++)
						{
							value += (population[i][l]-population[j][l])^2;
						}
						if(value > max)
						{
							max = value;
							k[i] = j;
						}
					}
				}
			} */
			//--------------------------------------------------------------------------------------------------
		}
		
		//record the termination cpu time
		if(bean.isCurrentThreadCpuTimeSupported())
	        time = bean.getCurrentThreadCpuTime() - time;
		
		
		//return the results
		results[6] = v;
		results[7] = verg;
		//results[10] = o;
		results[8] = f;
		results[9] = t;
		results[10] = bestFit;
		results[11] = time;
		results[12] = bestTime;
		
		return results;
	}
}

