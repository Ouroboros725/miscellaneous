package fsk.ouroboros725.miscellaneous.stack;

import java.util.ArrayList;
import java.util.List;

public class RearrangeRailroadCars {

	//TODO int overflow
	//TODO ArrayList and StringBuilder initial size
	//TODO Boolean as Object in the List
    public static void main(String[] args) {
    	// Train number
		int trainNum = Integer.parseInt(args[0]);
		if (trainNum <= 0) return;
		
		// Represent station operating sequence
		// 0 means a train enters station, 1 means a train leaves station
		int possNum = (int) Math.pow(2, trainNum * 2) - 1;
		
		List<Integer> result = new ArrayList<Integer>();
		
		for (int i = 0; i <= possNum; i++) {
			int j = i;  // Test one sequence
			
			// State of trains in the station 
			List<Boolean> station = new ArrayList<Boolean>();
			
			// State of trains about to enter station
			List<Boolean> outer = new ArrayList<Boolean>();
			for (int k = 0; k < trainNum; k++) {
				outer.add(true);
			}
			
			for (int k = 0, n = trainNum * 2; k < n; k++) {
				if ((j & 1) == 1) {
					// Enter station
					if (outer.isEmpty()) { 
						break;
					} else {
						outer.remove(0);
						station.add(true);
					}
				} else {
					// Leave station
					if (station.isEmpty()) {
						break;
					} else {
						station.remove(0);
					}
				}
				
				j >>>= 1;
			}
			
			// Successful sequence
			if (station.isEmpty() && outer.isEmpty()) {
				result.add(i);
			}
		}
		
		System.out.println("Total Count: " + result.size());
		for (Integer i : result) {
			int j = i.intValue();
			List<Integer> station = new ArrayList<Integer>();
			List<Integer> outer = new ArrayList<Integer>();
			for (int k = 1; k <= possNum; k++) {
				outer.add(k);
			}
			
			StringBuilder s = new StringBuilder();
			s.append("Case" + " " + i + ": ");
			for (int k = 0, n = trainNum * 2; k < n; k++) {
				if ((j & 1) == 1) {
					int t = outer.remove(0);
					station.add(t);
					s.append(t + " in, ");
				} else {
					int t = station.remove(station.size() - 1);
					s.append(t + "  out, ");
				}
				
				j >>>= 1;
			}
			
			System.out.println(s.substring(0, s.length() - 2));
		}
	}
}
