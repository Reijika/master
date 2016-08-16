package com.macaps.nativecv;

import org.opencv.core.Mat;


public class NativeFilter {
	
	public NativeFilter(){}
	
	public static native boolean applyCLAHE(long input_addr, long output_addr);
	
	public static native boolean applyGamma(long input_addr, long output_addr);
	
	public Mat EqualizeHistogram(Mat input_mat){
		Mat output_mat = input_mat.clone();
		
		long input_addr = input_mat.getNativeObjAddr();
		long output_addr = output_mat.getNativeObjAddr();
		
		boolean result = applyCLAHE(input_addr, output_addr);
		if (result){
			return output_mat;
		}
		else{
			return null;
		}
	}	
	
	public Mat GammaCorrection(Mat input_mat){
		Mat output_mat = input_mat.clone();
		
		long input_addr = input_mat.getNativeObjAddr();
		long output_addr = output_mat.getNativeObjAddr();
				
		boolean result = applyGamma(input_addr, output_addr);
		if (result){
			return output_mat;
		}
		else{
			return null;
		}
	}

}
