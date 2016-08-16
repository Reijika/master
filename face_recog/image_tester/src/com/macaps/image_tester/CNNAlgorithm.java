package com.macaps.image_tester;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class CNNAlgorithm {
	
	CNNAlgorithm(){}
	
	public static native int predictCNN(long m , long n, float [] opencv_poi, float []flandmark_poi);
	
	public int test(long train_addr, long inputaddr, Point[] opencv_poi, Point[] flandmark_poi){
		
		//convert flandmark Point array to float array
		float [] fland = new float [16];
		int a = 0;
		for (int i = 0; i < flandmark_poi.length; i++){
			fland [a] = (float) flandmark_poi[i].x;
			fland [a+1] = (float) flandmark_poi[i].y;
			a = a + 2;
		}		
		
		//convert opencv Point array to float array
		float [] open = new float [6];
		int b = 0;
		for (int i = 0; i < opencv_poi.length; i++){
			open [b] = (float) opencv_poi[i].x;
			open [b+1] = (float) opencv_poi[i].y;
			b = b + 2;
		}
		
		return predictCNN(inputaddr, train_addr, open, fland);
		
	}
}
