package com.macaps.image_tester;

public class ORBAlgorithm {

	public ORBAlgorithm(){}
	
	public static native String predict	(long training_addr, long input_addr);
	
	public String test(long training_addr, long input_addr){
		String label = predict(training_addr, input_addr);		
		return label;
	}			
	
}
