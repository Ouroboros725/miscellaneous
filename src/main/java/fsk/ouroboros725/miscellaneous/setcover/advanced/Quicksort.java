package fsk.ouroboros725.miscellaneous.setcover.advanced;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
 
//quick sort for any type of cost
public class Quicksort <T, S> {
	 public static final Random RND = new Random();
	 
     private void swap(S[] array, int i, int j) {
    	 S tmp = array[i];
         array[i] = array[j];
         array[j] = tmp;
     }
 
     private int partition(S[] array, HashMap<S, T> weight, int begin, int end, Comparator<T> cmp) {
         int index = begin + RND.nextInt(end - begin + 1);
         T pivot = weight.get(array[index]);
         swap(array, index, end);	
         for (int i = index = begin; i < end; ++ i) {
             if (cmp.compare(weight.get(array[i]), pivot) <= 0) {
                 swap(array, index++, i);
             }
         }
         swap(array, index, end);	
         return (index);
     }
 
     private void qsort(S[] array, HashMap<S, T> weight, int begin, int end, Comparator<T> cmp) {
         if (end > begin) {
             int index = partition(array, weight, begin, end, cmp);
             qsort(array, weight, begin, index - 1, cmp);
             qsort(array, weight, index + 1,  end,  cmp);
         }
     }
 
     public void sort(S[] array, HashMap<S, T> weight, Comparator<T> cmp) {
         qsort(array, weight, 0, array.length - 1, cmp);
     }
     
     public void reverse(S[] array) {
    	 S temp = null;
    	 
    	 for(int i = 0, j = array.length-1; i < j; i++, j--)
    	 {
    		 temp = array[i];
    		 array[i] = array[j];
    		 array[j] = temp;
    	 }
     }
}

