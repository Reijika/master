package com.macaps.nativecv;

import org.opencv.core.Mat;
import android.os.Environment;

public class FacialPointDetector {
	
	public FacialPointDetector(){}
	
	public native static float[] detectFLAND(long input_addr, int[] bounds, String model_path);
	
	public float[] detectFeaturePoints(Mat img, int[] bbox){
		long input_addr = img.getNativeObjAddr();		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/resources/flandmark_model.dat";		
					
		//Returns 8 xy coordinate pairs, 16 values total		
		//index 0 and 1 : Face Center (S0)
		//index 2 and 3 : CanthusRL - Right corner of left eye (S1)
		//index 4 and 5 : CanthusLR - Left corner of right eye (S2)
		//index 6 and 7 : MouthCL - Left corner of the mouth (S3)
		//index 8 and 9 : MouthCR - Right corner of the mouth (S4)
		//index 10 and 11: CanthusLL - Left corner of left eye (S5)
		//index 12 and 13: CanthusRR - Right corner of right eye (S6)
		//index 14 and 15: NoseB - Bottom curve of the nose (S7)
		
		return detectFLAND(input_addr, bbox, path);		
	}

}
