package com.macaps.nativecv;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.graphics.Bitmap;


public class NativeFaceRecognizer {
		
	static Bitmap input_bmp;
	static Bitmap [] train_bmp;	
	static long[] train_addr;	

	public static Mat ORB_output_mat; 	//Debug mat output for ORB face recognition
	public static Mat CNN_output_mat;   //Debug mat output for CNN face recognition
	
	public NativeFaceRecognizer(Bitmap in, Bitmap [] set){		
		input_bmp = in;
		train_bmp = new Bitmap [set.length];
		for (int i = 0; i < set.length; i++){
			train_bmp[i] = set[i];
		}
	}	
    
    public static native int predictLBPH(long m, long [] n, String fp, boolean sr);
    
    public static native int predictORB(long m , long [] n, long output);
    
    public static native int predictCNN(long m , long [] n, long output, float [] opencv_poi, float []flandmark_poi);
    
	public int LBPHMatchPrediction(String filepath, boolean save_request){
		
		//prep input
		Mat input_mat = new Mat (input_bmp.getWidth(), input_bmp.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(input_bmp, input_mat);
		long inputaddr = input_mat.getNativeObjAddr();
		
		//prep training images
		train_addr = new long [train_bmp.length];
		for (int i = 0; i < train_addr.length; i++){
			Mat temp_mat = new Mat (train_bmp[i].getWidth(), train_bmp[i].getHeight(), CvType.CV_8UC4);
			Utils.bitmapToMat(train_bmp[i], temp_mat);
			train_addr[i] = temp_mat.getNativeObjAddr();
		}		
		return predictLBPH(inputaddr, train_addr, filepath, save_request);
	}
	
	public int ORBMatchPrediction(){
		
		//Convert the input bitmap into a Mat native address
		Mat input_mat = new Mat (input_bmp.getWidth(), input_bmp.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(input_bmp, input_mat);
		long inputaddr = input_mat.getNativeObjAddr();
		
		//Create an empty output Mat native address
		ORB_output_mat = new Mat (input_bmp.getWidth()*2, input_bmp.getHeight(), CvType.CV_8UC4);
		long outputaddr = ORB_output_mat.getNativeObjAddr();
		
		//Convert the training bitmap into a Mat native address
		train_addr = new long [train_bmp.length];
		for (int i = 0; i < train_addr.length; i++){
			Mat temp_mat = new Mat (train_bmp[i].getWidth(), train_bmp[i].getHeight(), CvType.CV_8UC4);
			Utils.bitmapToMat(train_bmp[i], temp_mat);
			train_addr[i] = temp_mat.getNativeObjAddr();
		}
		
		return predictORB(inputaddr, train_addr, outputaddr);
	}
		
	public int CNNMatchPrediction(Point[] opencv_poi, Point[] flandmark_poi){
		
		//Convert the input bitmap into a Mat native address
		Mat input_mat = new Mat (input_bmp.getWidth(), input_bmp.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(input_bmp, input_mat);
		long inputaddr = input_mat.getNativeObjAddr();
		
		//Create an empty output Mat native address
		CNN_output_mat = new Mat (input_bmp.getWidth()*2, input_bmp.getHeight(), CvType.CV_8UC4);
		long outputaddr = CNN_output_mat.getNativeObjAddr();
		
		//Convert the training bitmap into a Mat native address
		train_addr = new long [train_bmp.length];
		for (int i = 0; i < train_addr.length; i++){
			Mat temp_mat = new Mat (train_bmp[i].getWidth(), train_bmp[i].getHeight(), CvType.CV_8UC4);
			Utils.bitmapToMat(train_bmp[i], temp_mat);
			train_addr[i] = temp_mat.getNativeObjAddr();
		}
		
		//Convert the Flandmark facial feature array into a float array
		float [] fland = new float [16];
		int a = 0;
		for (int i = 0; i < flandmark_poi.length; i++){
			fland [a] = (float) flandmark_poi[i].x;
			fland [a+1] = (float) flandmark_poi[i].y;
			a = a + 2;
		}		
		
		//Convert the OpenCV facial feature array into float array
		float [] open = new float [6];
		int b = 0;
		for (int i = 0; i < opencv_poi.length; i++){
			open [b] = (float) opencv_poi[i].x;
			open [b+1] = (float) opencv_poi[i].y;
			b = b + 2;
		}		
		
		return predictCNN(inputaddr, train_addr, outputaddr, open, fland);
	}
	
}
	


