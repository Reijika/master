package com.macaps.image_tester;

import org.opencv.core.Mat;

import android.os.Environment;

public class PointDetector {
	
	public PointDetector(){}
	
	public native static float[] detectFLAND(long input_addr, int[] bounds, String model_path);
	
	public float[] detectFeaturePoints(Mat img, int[] bbox){
		long input_addr = img.getNativeObjAddr();		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/resources/flandmark_model.dat";		
					
		return detectFLAND(input_addr, bbox, path);		
	}


}
