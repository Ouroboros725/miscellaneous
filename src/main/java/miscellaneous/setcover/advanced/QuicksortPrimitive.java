package miscellaneous.setcover.advanced;

import java.util.Random;

//quick sort for array with double cost
public class QuicksortPrimitive  {
	 public static final Random RND = new Random();
	 
   private static void swap(int[] array, int i, int j) {
  	 int tmp = array[i];
       array[i] = array[j];
       array[j] = tmp;
   }

   private static int partition(int[] array, double[] weight, int begin, int end) {
       int index = begin + RND.nextInt(end - begin + 1);
       double pivot = weight[array[index]];
       swap(array, index, end);	
       for (int i = index = begin; i < end; ++ i) {
           if (weight[array[i]] <= pivot && pivot - weight[array[i]] > 0.000001) {
               swap(array, index++, i);
           }
       }
       swap(array, index, end);	
       return (index);
   }

   private static void qsort(int[] array, double[] weight, int begin, int end) {
       if (end > begin) {
           int index = partition(array, weight, begin, end);
           qsort(array, weight, begin, index - 1);
           qsort(array, weight, index + 1,  end);
       }
   }

   public static void reverse(int[] array)
   {
  	 int temp = 0;
  	 
  	 for(int i = 0, j = array.length-1; i < j; i++, j--)
  	 {
  		 temp = array[i];
  		 array[i] = array[j];
  		 array[j] = temp;
  	 }
   }
   
   public static void sort(int[] array, double[] weight) {
       qsort(array, weight, 0, array.length - 1);
   }
}

