package com.macaps.image_tester;

public class LBPHAlgorithm {
	
	public LBPHAlgorithm(){}
	
	public static native int predict(long training_addr, long input_addr);

	public boolean test(long training_addr, long input_addr){
		int label = predict(training_addr, input_addr);		
		if (label == 0){
			return true;
		}
		else {
			return false;
		}
	}	
}


