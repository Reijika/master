package com.macaps.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import com.macaps.nativecv.FacialPointDetector;
import com.macaps.nativecv.NativeFaceRecognizer;
import com.macaps.nativecv.NativeFilter;
import com.macaps.network.Transaction;
import com.macaps.reader.Reader;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.FaceDetectionListener {

	private static final int CAMERA_DELAY_MILLI = 1300; //Delay in milliseconds before photo capture occurs
	private static final int IMG_WIDTH_HEIGHT = 90; //final width and height of image in pixels
	private static final int TMP_WIDTH_HEIGHT = 300; //width and height of image in pixels prior to applying CLAHE
	private static final int DETECTION_WIDTH = 384; //width of image in pixels prior to face detection
	private static final int DETECTION_HEIGHT = 640; //height of image in pixels prior to face detection
	
	//DEBUG - VIEW OBJECTS
	private TextView facebox;
	private ImageView capturedImage;
	
	//DEBUG - TIMING
	private long perfstarttime;
	private long perfendtime;
	
	//feature point arrays
	private Point [] opencv_poi = new Point[3];
	private Point [] flandmark_poi = new Point[8];		
		
    private int winWidth;
    private int winHeight;
	
	public boolean foundface = false;
    public boolean isdetecting = false;        
    
    public Camera mCamera;

    private MediaPlayer shutter;
    private Reader mReader;
	private SurfaceHolder mSurfaceHolder;	
	private TrackBox mTrackBox;
	private String cardID;

	private Vector<Transaction> mVector;
	private SimpleDateFormat sdf;
	private String currentDateandTime;	
		
	private Bitmap [] training_set; //Bitmap array for passing in comparison images
	private Bitmap input; //A debug Bitmap object for manual comparisons
	
	Context cc;
	
    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		cc = context;
		init();
	}
	
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		cc = context;
		init();
	}

	public CameraPreview(Context context) {
		super(context);
		cc = context;
		init();
	}

	public void init() {
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
	}
	
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open(1);
        } catch (Exception e) {
        }
        return camera;
    }
	    
    private void setCameraDisplayOrientation(Camera camera) {
        Camera.Parameters camparams = camera.getParameters();        
        camparams.setRotation(270);        
        camparams.setPictureSize(1280, 720);
        camera.setParameters(camparams);
        camera.setDisplayOrientation(90);
    }

	public void passObjects(TrackBox boxview,  TextView t, MediaPlayer mp, Bitmap [] args, Bitmap a, ImageView imgview) {
		this.mTrackBox = boxview;		
		this.shutter = mp;
		this.input = a;
		
		this.training_set = new Bitmap[args.length];
		for (int i = 0; i < args.length; i++){
			training_set [i] = args[i];
		}
				
		//DEBUG
		this.facebox = t;
		this.capturedImage = imgview;
	}	
	
	public void sendTransactionInfo(String idtext, Vector<Transaction> vector, Reader box) {
		this.cardID = idtext;	
		this.mVector = vector;
		this.mReader = box;
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		mCamera = getCameraInstance();
		setCameraDisplayOrientation(mCamera);
		
		//Get screen resolution
		View parent = (View) mTrackBox.getParent();			
		winWidth = parent.getWidth();
		winHeight = parent.getHeight();
		
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.startPreview();
			mCamera.setFaceDetectionListener(this);		
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if (isdetecting){
			mCamera.stopFaceDetection();
		}
		
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
		// start preview with new settings
		try {
			mCamera.startPreview();
			mCamera.setFaceDetectionListener(this);
		}
		catch (Exception e) {
		}
	}

	private boolean requirementCheck(Bitmap picture){
		
		//Face Detector only works with upright images		
		FaceDetector.Face[] faceArray = new FaceDetector.Face[10];		
		FaceDetector detector = new FaceDetector(picture.getWidth(), picture.getHeight(), 1);
		
		//Convert to RGB_565 image format (mandatory for face detection)
		Bitmap maskBitmap = Bitmap.createBitmap( picture.getWidth(), picture.getHeight(), Bitmap.Config.RGB_565 );
		Canvas c = new Canvas();
		c.setBitmap(maskBitmap);
		Paint p = new Paint();
		p.setFilterBitmap(true); 
		c.drawBitmap(picture,0,0,p);
		
		//Check for exactly 1 face
		boolean faceCheck = false;
		int numFaces = detector.findFaces(maskBitmap, faceArray);
		if (numFaces == 1){
			faceCheck = true;
		}		
		
		//Verify if the face is near the center of the screen + Verify if the face is large enough 
		boolean centerCheck = false;
		boolean sizeCheck = false;
		boolean confidenceCheck = false;
		if (faceCheck){
			FaceDetector.Face f = faceArray[0];			
			PointF point = new PointF();
			f.getMidPoint(point);
			float eyedistance = f.eyesDistance();

			//Force the crop box to maintain an aspect ratio of 4:3
			float EXPANSION_CONSTANT_X = 1.20f; //on both left and right will expand the x direction by 20%
			float EXPANSION_CONSTANT_Y = EXPANSION_CONSTANT_X*(4.0f/3.0f);
			float leftMargin = point.x - (eyedistance*EXPANSION_CONSTANT_X);
			float topMargin= point.y - (eyedistance*EXPANSION_CONSTANT_Y);
			float rightMargin = point.x + (eyedistance*EXPANSION_CONSTANT_X);
			float botMargin = point.y + (eyedistance*EXPANSION_CONSTANT_Y);
						
			//Image is fine if the expanded bounds do not exceed the confines of the camera preview			
			if (leftMargin >= 0 && topMargin >= 0 && rightMargin <= winWidth && botMargin <= winHeight){
				centerCheck = true;
			}	
			
			//This is to ensure there is enough 'face' to work with in the image
			float area = Math.abs(topMargin - botMargin) * Math.abs(leftMargin - rightMargin);
			float screenarea = (float) winWidth * winHeight;
			if (area > (0.10 * screenarea)){
				sizeCheck = true;
			}
			
			//Trivial confidence score check
			if (f.confidence() >= 0.35){
				confidenceCheck = true;
			}
			
		}		
		maskBitmap.recycle();				
		//Toast.makeText(cc, "sizeCheck: " + sizeCheck + "\n" + "faceCheck: " + faceCheck + "\n" + "centerCheck: " + centerCheck + "\n" +"confidenceCheck: " + confidenceCheck ,Toast.LENGTH_LONG).show();
		
		//If the image passes all 4 tests, it has fulfilled phase 1 of the validation
		if (sizeCheck && faceCheck && centerCheck && confidenceCheck){
			return true;
		}
		else{
			return false;
		}	
	}
	
	private Bitmap alignFaceAndCrop(Bitmap picture){ 
				
		if (picture == null){
			Toast.makeText(cc, "Image data is missing. Photo capture failed.",Toast.LENGTH_LONG).show();
			return null;
		}
		
		Bitmap bmp;		

		//Load the haar cascades for face detection and eye detection
		String face_cascade_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/resources/lbpcascade_frontalface.xml";
		String eyes_cascade_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/resources/haarcascade_eye_tree_eyeglasses.xml";
		
		CascadeClassifier frontalface_cascade = new CascadeClassifier(face_cascade_path);		
		CascadeClassifier eye_cascade = new CascadeClassifier(eyes_cascade_path);
					
		if (frontalface_cascade.empty() && eye_cascade.empty()){
			Toast.makeText(cc, "CascadeClassifier initialization failed.",Toast.LENGTH_LONG).show();	
			bmp = null;
		}
		else{		
			
			Mat src = new Mat (picture.getWidth(), picture.getHeight(), CvType.CV_8UC1);		
			Mat brightmat = new Mat (picture.getWidth(), picture.getHeight(), CvType.CV_8UC1);
			
			Utils.bitmapToMat(picture, brightmat);	
			Imgproc.cvtColor(brightmat, brightmat, Imgproc.COLOR_RGB2GRAY);
			
			//Apply gamma correction to the image prior to face detection
			NativeFilter gamma = new NativeFilter();
			src = gamma.GammaCorrection(brightmat);
			brightmat.release();
			
			MatOfRect face = new MatOfRect();
			MatOfRect eyes = new MatOfRect();
			Mat roi_face = null;
			Rect face_rect = null;
						
			//Detect the face, extract the ROI then detect the eyes 
			frontalface_cascade.detectMultiScale(src, face, 1.10, 3, 0, new Size(100,100), new Size(640,640));//50,50,400,400
			for (Rect rect : face.toArray()) {
				face_rect = rect.clone(); //capture the face bounds for later use
	            roi_face = src.submat(rect);	            
	            eye_cascade.detectMultiScale(roi_face, eyes, 1.05, 3, 0, new Size(15,15), new Size (200,200));//5,5,100,100
	        }
			
			//Toast.makeText(cc, "# of faces detected:  " + face.toArray().length + "\n"  + "# of eyes detected:   " + eyes.toArray().length + "\n",Toast.LENGTH_LONG).show();
			
			//Verify there is exactly 1 face and 2 eyes
			if (face.toArray().length == 1 && eyes.toArray().length == 2){
				double midpointx = 0;
				double midpointy = 0;
				double left_eye_x = 0;
				double left_eye_y = 0;
				double right_eye_x = 0;
				double right_eye_y = 0;
				
				//Assign left and right eyes
				if (eyes.toArray()[0].x < eyes.toArray()[1].x){
					left_eye_x = eyes.toArray()[0].x + (eyes.toArray()[0].width / 2.0d);
					left_eye_y = eyes.toArray()[0].y + (eyes.toArray()[0].height / 2.0d);
					right_eye_x = eyes.toArray()[1].x + (eyes.toArray()[1].width / 2.0d);
					right_eye_y = eyes.toArray()[1].y + (eyes.toArray()[1].height / 2.0d);
				}
				else if (eyes.toArray()[0].x > eyes.toArray()[1].x){					
					left_eye_x = eyes.toArray()[1].x + (eyes.toArray()[1].width / 2.0d);
					left_eye_y = eyes.toArray()[1].y + (eyes.toArray()[1].height / 2.0d);
					right_eye_x = eyes.toArray()[0].x + (eyes.toArray()[0].width / 2.0d);
					right_eye_y = eyes.toArray()[0].y + (eyes.toArray()[0].height / 2.0d);
				}				
								
				//Calculate the midpoint between the eyes
				if (left_eye_y > right_eye_y){ //rotate counter clockwise				
					midpointx = left_eye_x + (Math.abs(right_eye_x - left_eye_x))/2.0d;
					midpointy = right_eye_y + (Math.abs(right_eye_y - left_eye_y))/2.0d;
				}
				else if (left_eye_y < right_eye_y){ //rotate clockwise
					midpointx = left_eye_x + (Math.abs(right_eye_x - left_eye_x))/2.0d;
					midpointy = left_eye_y + (Math.abs(right_eye_y - left_eye_y))/2.0d;
				}		
				
				//Calculate the pose alignment angle in degrees
				double degrees = calculateRotation(left_eye_x, left_eye_y, right_eye_x, right_eye_y);				
								
				//Calculate the distance of the eye from the midpoint
				double eye_distance = (double)Math.sqrt((right_eye_x-left_eye_x)*(right_eye_x-left_eye_x) + (right_eye_y-left_eye_y)*(right_eye_y-left_eye_y));
				eye_distance = eye_distance / 2.0d;				
				
				//Update the midpoint coordinates relative to the original image rather than the ROI
				midpointx = midpointx + face_rect.x;
				midpointy = midpointy + face_rect.y;

				//Calculate the expected coordinates of the eyes using the eye_distance
			    left_eye_x = midpointx - eye_distance; //left_difference;
				left_eye_y = midpointy;
				right_eye_x = midpointx + eye_distance; //right_difference;
				right_eye_y = midpointy;
				
				
				//Rotate the image according to the calculated angle
				Point src_center = new Point(midpointx, midpointy);
				Mat rotationMatrix = Imgproc.getRotationMatrix2D(src_center, degrees, 1.0);				
			    Imgproc.warpAffine(src,src, rotationMatrix, src.size());
			    
				//Define an arbitrary face bound for the Flandmark Detector				
				double offset_pct_x = 1.3; 
				double offset_pct_y = 2.0;
				int top_left_x  = (int)(left_eye_x - (eye_distance*offset_pct_x));
				int top_left_y = (int)(left_eye_y - (eye_distance*offset_pct_y));				
				int top_right_x = (int)(right_eye_x + (eye_distance*offset_pct_x));
				int top_right_y = (int)(right_eye_y - (eye_distance*offset_pct_y));				
				int corner_width = (int)(top_right_x - top_left_x);
				int corner_height = (int)((double)corner_width * 1.1d);				
				
				//Detect the facial features using the Flandmark Detector
				int bounds[] = {top_left_x, top_left_y, (top_left_x + corner_width), (top_left_y + corner_height)};
		    	FacialPointDetector fpdetector = new FacialPointDetector();
		    	float [] feature_points = fpdetector.detectFeaturePoints(src, bounds);
		    	for (int i = 0; i < 16; i = i + 2){
		    		feature_points[i] = feature_points[i];
		    		feature_points[i+1] = feature_points[i+1];
		    	}


				//Define the actual face crop bounds
			    double offset_pct = 0.8; 
				top_left_x  = (int)(left_eye_x - (eye_distance*offset_pct));
				top_left_y = (int)(left_eye_y - (eye_distance*offset_pct));				
				top_right_x = (int)(right_eye_x + (eye_distance*offset_pct));
				top_right_y = (int)(right_eye_y - (eye_distance*offset_pct));				
				corner_width = (int)(top_right_x - top_left_x);
				corner_height = corner_width;
				
				//Check if the crop bounds fall outside the image boundaries
				if (top_left_x < 0 || top_left_y < 0){return null;}
				if (top_right_x > src.cols() || top_right_y < 0){return null;}
				if ((top_left_y + corner_height) > src.rows() || (top_right_y + corner_height) > src.rows()){return null;}
			    
			    //Crop out the face
			    Rect roi = new Rect(top_left_x, top_left_y, corner_width, corner_height);
			    Mat roi_mat = src.submat(roi).clone();
			    
			    //Scaled the feature point coordinates for 300 by 300 resolution			    
			    double scale_factor = (double) TMP_WIDTH_HEIGHT/ (double)corner_width;			    
			    left_eye_x = (((double)left_eye_x - (double)top_left_x)*scale_factor) ;		
				left_eye_y = (((double)left_eye_y - (double)top_left_y)*scale_factor);
				right_eye_x = (((double)right_eye_x - (double)top_left_x)*scale_factor);
				right_eye_y = (((double)right_eye_y - (double)top_left_y)*scale_factor);
				midpointx = (((double)midpointx - (double)top_left_x)*scale_factor);
				midpointy = (((double)midpointy - (double)top_left_y)*scale_factor);
				for(int i = 0; i < 16; i=i+2){
					feature_points[i] =   (float) (((double)feature_points[i]  -(double)top_left_x)*scale_factor);
					feature_points[i+1] = (float) (((double)feature_points[i+1]-(double)top_left_y)*scale_factor);
				}
				
				//Store the detected points for later use
			    opencv_poi[0] = new Point (left_eye_x, left_eye_y);
			    opencv_poi[1] = new Point (right_eye_x, right_eye_y);
			    opencv_poi[2] = new Point (midpointx, midpointy);
			    flandmark_poi[0] = new Point((double)feature_points[0], (double)feature_points[1]); //Face Center (S0)
			    flandmark_poi[1] = new Point((double)feature_points[2], (double)feature_points[3]); //CanthusRL - Right corner of left eye (S1)
			    flandmark_poi[2] = new Point((double)feature_points[4], (double)feature_points[5]); //CanthusLR - Left corner of right eye (S2) 
			    flandmark_poi[3] = new Point((double)feature_points[6], (double)feature_points[7]); //MouthCL - Left corner of the mouth (S3)
			    flandmark_poi[4] = new Point((double)feature_points[8], (double)feature_points[9]); //MouthCR - Right corner of the mouth (S4)
			    flandmark_poi[5] = new Point((double)feature_points[10], (double)feature_points[11]); //CanthusLL - Left corner of left eye (S5)
			    flandmark_poi[6] = new Point((double)feature_points[12], (double)feature_points[13]); //CanthusRR - Right corner of right eye (S6)
			    flandmark_poi[7] = new Point((double)feature_points[14], (double)feature_points[15]); //NoseB - Bottom curve of the nose (S7)			    
			    
			    //Convert back to bitmap and return
			    Bitmap temp = Bitmap.createBitmap(roi_mat.cols(), roi_mat.rows(), Bitmap.Config.ARGB_8888);			    
				Utils.matToBitmap(roi_mat, temp);
				roi_mat.release();

				return temp;
			}			
			else{
				bmp = null;
			}
		}
		picture.recycle();	
		return bmp;
	}
	
	private double calculateRotation(double leftx, double lefty, double rightx, double righty){
		
		double degree_rotation = 0;		
		double adjacent = Math.abs(leftx - rightx);
		double opposite = Math.abs(lefty - righty);
		
		//to avoid divide by zero exception (unlikely, requires x coordinates to be the same)
		if (adjacent == 0){return 0;}
		
		degree_rotation = Math.toDegrees(Math.atan(opposite/adjacent)); 
		
		//in the event the eye y coordinates are on the same x-axis,
		if (degree_rotation == 0){return degree_rotation;}
		
		//if rotation is required
		if (lefty > righty){ //Apply a negative -1 if the correction is counter clockwise				
			degree_rotation = degree_rotation * -1.0d;
		}
		else if (lefty < righty){ //Apply nothing if the correction is clockwise
			degree_rotation = degree_rotation * 1.0d;
		}		
				
		//Toast.makeText(cc, "The image must be rotated " + degree_rotation + " degrees!",Toast.LENGTH_SHORT).show();
		return degree_rotation;
	}
	
	private Bitmap applyCLAHE(Bitmap image){
		
		//Converting a color image to CV_8UC1 functions as a grayscale filter 
		Mat greymat = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);				
		Utils.bitmapToMat(image, greymat);
		
		//Apply contract limited adaptive histogram equalization
		//Note: This operation is HIGHLY DEPENDENT on the image resolution due to the kernel operator
		NativeFilter filter = new NativeFilter();
		Mat contrast_output = greymat.clone();				
		contrast_output = filter.EqualizeHistogram(greymat);
		
		Utils.matToBitmap(contrast_output, image);
		contrast_output.release();
		
		return image;
	}	
	
	private void savePhoto(Bitmap picture, Camera camera){
		
		if (picture == null){
			Toast.makeText(cc, "Image data is missing. Save failed.",Toast.LENGTH_LONG).show();
			return;
		}
		
		String sdcardpath = "/DCIM";		
		String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + sdcardpath;

		//Generate the DCIM directory if it is missing
		File dir = new File(fullPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
 		
		try {
			//Save image as a webp file with a 45 percent compression ratio
			OutputStream fOut = null;
			File file = new File(fullPath, cardID + "_" + currentDateandTime + ".webp");
			file.createNewFile();
			fOut = new FileOutputStream(file);          
			picture.compress(Bitmap.CompressFormat.WEBP, 45, fOut); //100 is no quality loss
			fOut.flush();
			fOut.close();
			MediaScannerConnection.scanFile(cc, new String[] { file.getAbsolutePath() }, null, null);			
		}
		catch (FileNotFoundException e) {} 
        catch (IOException e) {}

		return;
	}
	
	private int ExecuteLBPHRecognition(Bitmap processed_input){

		boolean save = false;
		String filename = "/" + cardID + ".yaml";
		String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/facerecognizer";
		File dir = new File(fullPath);
		File ymlmodel = new File (fullPath + filename);
		
		//Check if the facerecognizer directory exists
		if (!dir.exists()) {
			dir.mkdirs();
			save = true; //directory does not exist, FaceRecognizer will have to generate the model in this directory
		}
		else{			
			//if the directory exists, but the YML model data does not, set the flag to generate the model
			if (!ymlmodel.exists()){
				save = true;
			}
		}
		
		//Call the recognition class for a prediction
    	NativeFaceRecognizer lbphFR = new NativeFaceRecognizer(processed_input, training_set);    	
    	int result = lbphFR.LBPHMatchPrediction(fullPath + filename, save);
    	
    	//if save is true, that means a YML file was generated from the recognition process, scan for it
    	if (save){MediaScannerConnection.scanFile(cc, new String[] { ymlmodel.getAbsolutePath() }, null, null);}
    	
    	//Uncomment the lines below to see the image that was passed in for recognition
    	//capturedImage.setImageBitmap(processed_input);
		//capturedImage.setVisibility(View.VISIBLE);
    	
    	return result;
	}
	
	private int ExecuteORBRecognition(Bitmap processed_input){
		
		//Call the recognition class for a prediction
		NativeFaceRecognizer orbFR = new NativeFaceRecognizer(processed_input, training_set);
    	int result = orbFR.ORBMatchPrediction();
    	
    	//Uncomment the lines below to see the concatenated images of the recognition
    	//Mat output = orbFR.ORB_output_mat;
    	//Bitmap matched_bmp =  Bitmap.createBitmap( output.cols(), output.rows(), Bitmap.Config.RGB_565 );
    	//Utils.matToBitmap(output, matched_bmp);    	
    	//capturedImage.setImageBitmap(matched_bmp);
		//capturedImage.setVisibility(View.VISIBLE);
    	
    	return result;
	}
	
	private int ExecuteCNNRecognition(Bitmap processed_input){
		
		//Call the recognition class for a prediction
		NativeFaceRecognizer cnnFR = new NativeFaceRecognizer(processed_input, training_set);		
		int result = cnnFR.CNNMatchPrediction(opencv_poi, flandmark_poi);
		
		//Uncomment the lines below to see the concatenated images of the recognition
		//Mat output = cnnFR.CNN_output_mat;
		//Bitmap matched_bmp =  Bitmap.createBitmap( output.cols(), output.rows(), Bitmap.Config.RGB_565 );
    	//Utils.matToBitmap(output, matched_bmp);    	
    	//capturedImage.setImageBitmap(matched_bmp);
		//capturedImage.setVisibility(View.VISIBLE);		
				
		return result;
	}
	
	private Bitmap displayFeaturePoints(Bitmap processed_input){		
		
		//For visualization purposes		
		Mat src = new Mat();		
		Utils.bitmapToMat(processed_input, src);
		
		//Mark the feature points on the image passed into this method
	    Core.circle(src, opencv_poi[0], 4, new Scalar(255, 255, 255)); 
		Core.circle(src, opencv_poi[1],4, new Scalar(255, 255, 255));			
	    Core.circle(src, opencv_poi[2], 4, new Scalar(0, 0, 0));
		Core.circle(src, flandmark_poi[0], 3, new Scalar(0, 0, 255),-1, 8,0);
		for (int i = 1; i < 8; i++){
			Core.circle(src, flandmark_poi[i], 3, new Scalar(255, 0, 0), -1, 8,0);			
		}		
		
		//Mark the component areas
		Core.rectangle(src, new Point(flandmark_poi[5].x,flandmark_poi[5].y - 30), new Point(flandmark_poi[6].x,flandmark_poi[6].y + 30), new Scalar(255, 255, 255), 2, -1, 0);
		Core.rectangle(src, new Point(flandmark_poi[5].x,opencv_poi[0].y - 50), new Point(flandmark_poi[1].x,flandmark_poi[1].y+20), new Scalar(255, 255, 255), 2, -1, 0);
		Core.rectangle(src, new Point(flandmark_poi[6].x,opencv_poi[1].y - 50), new Point(flandmark_poi[2].x,flandmark_poi[2].y+20), new Scalar(255, 255, 255), 2, -1, 0);		
		Core.rectangle(src, new Point(flandmark_poi[5].x,flandmark_poi[5].y - 10), new Point(flandmark_poi[1].x,flandmark_poi[3].y), new Scalar(255, 255, 255), 2, -1, 0);
		Core.rectangle(src, new Point(flandmark_poi[6].x,flandmark_poi[6].y - 10), new Point(flandmark_poi[2].x,flandmark_poi[4].y), new Scalar(255, 255, 255), 2, -1, 0);
		Core.rectangle(src, new Point(flandmark_poi[3].x-30,flandmark_poi[3].y - 30), new Point(flandmark_poi[4].x+30,flandmark_poi[4].y+30), new Scalar(255, 255, 255), 2, -1, 0);
		Core.rectangle(src, new Point(flandmark_poi[3].x,opencv_poi[2].y), new Point(flandmark_poi[4].x,flandmark_poi[4].y), new Scalar(255, 255, 255), 2, -1, 0);
		
		Utils.matToBitmap(src, processed_input);
		return processed_input;
	}
	
	private Bitmap BMPandFPScaling(Bitmap input_img, int previous_width, int new_width){
		
		//Scale the features point arrays and the image based on the method parameters 
		double scalefactor = (double)new_width/(double)previous_width;
		
		for (int i = 0; i < opencv_poi.length; i++){
			opencv_poi[i].x = opencv_poi[i].x * scalefactor; 
			opencv_poi[i].y	= opencv_poi[i].y * scalefactor; 		
		}
		for (int i = 0; i < flandmark_poi.length; i++){
			flandmark_poi[i].x = flandmark_poi[i].x * scalefactor; 
			flandmark_poi[i].y	= flandmark_poi[i].y * scalefactor;			
		}
		
		Bitmap bmp = Bitmap.createScaledBitmap(input_img, new_width, new_width, true);
		input_img.recycle();
		
		return bmp;
	}
	
	PictureCallback jpegPictureCallback = new PictureCallback() {				
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {	
        	
        		//DEBUG - Record the task start time 
        		Calendar sc = Calendar.getInstance();
        		perfstarttime = sc.getTimeInMillis();
        	
        		//Record the time of the photo capture for transaction purposes
        		sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        		currentDateandTime = sdf.format(new Date()).replace(" ","");

        		//Decode the byte array data into a Bitmap object
				Bitmap pictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				
			    boolean check_1 = requirementCheck(pictureBitmap);			    
			    boolean check_2 = false;			    
			    
				//Fast initial check to see if it is worth running the more computationally expensive check
				if (check_1){		
					
					//Minor rescaling to decrease face/eye detection time during processing
					Bitmap scaledBitmap = Bitmap.createScaledBitmap(pictureBitmap, DETECTION_WIDTH, DETECTION_HEIGHT, true);
					
					//Operations: Grayscale filtering, gamma correction, face/eye detection, feature point detection
					//			  pose alignment, cropping
					Bitmap rotatedBitmap = alignFaceAndCrop(scaledBitmap);
					
					//Face has been confirmed + partially processed = pass phase 2, can process and save to DCIM					
					if (rotatedBitmap != null){check_2 = true;}
					
					if (check_2){
						
							//Another minor rescaling for the CLAHE operation
							Bitmap smallBitmap = Bitmap.createScaledBitmap(rotatedBitmap, TMP_WIDTH_HEIGHT, TMP_WIDTH_HEIGHT, true);
							
							//Apply CLAHE operation (gaussian blur and masking were removed)
							Bitmap processedBitmap = applyCLAHE(smallBitmap);
							
							//Bitmap temp_Bitmap = displayFeaturePoints(processedBitmap);
							//capturedImage.setImageBitmap(temp_Bitmap);
							//capturedImage.setVisibility(View.VISIBLE);
							//savePhoto(temp_Bitmap, camera);
							
							//Final rescaling prior to face recognition
							Bitmap finalBitmap = BMPandFPScaling(processedBitmap, TMP_WIDTH_HEIGHT, IMG_WIDTH_HEIGHT);
							
							savePhoto(finalBitmap, camera);
							
							
							//Execute all 3 face recognition approaches on the processed image against the comparison image
							String match = "";							
							int LBPH = ExecuteLBPHRecognition(finalBitmap);
							if (LBPH == 0){match = match + "LBPH: Simon Chan" + "\n";}
							else{match = match + "LBPH: Unknown person" + "\n";}
							
							int ORB = ExecuteORBRecognition(finalBitmap);
							if (ORB == 0){match = match + "ORB:  Simon Chan" + "\n";}
							else{match = match + "ORB:  Unknown person" + "\n";}
							
							int CNN = ExecuteCNNRecognition(finalBitmap);
							if (CNN == 0){match = match + "CNN:  Simon Chan";}
							else{match = match + "CNN:  Unknown person";}
							
							Toast.makeText(cc, match ,Toast.LENGTH_LONG).show();
							
							mReader.Light(true);
							try {								
								Thread.sleep(800);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}						
					}					
					//Release the processing bitmaps
					if (rotatedBitmap != null){rotatedBitmap.recycle();}					
					scaledBitmap.recycle();
				}
				
				//If either the initial face detection checks failed, throw the signal
				if (!check_1 || !check_2){
					mReader.Beep(1000);
					//Toast.makeText(cc, "Failed. Try again." ,Toast.LENGTH_LONG).show();					
				}
				//Toast.makeText(cc, "Check 1: " + check_1 + "\n" + "Check 2: " + check_2, Toast.LENGTH_LONG).show();
				
				//Attach the results of the task to the transaction vector object regardless of failure or success
				try {
					Date transactionDate = sdf.parse(currentDateandTime);
					Calendar cal = Calendar.getInstance();
					cal.setTime(transactionDate);
					Transaction t = new Transaction(cal, Long.parseLong(cardID,16), 0x0000, 1);
					mVector.add(t);
					Log.d("facedetection", "Transaction:" + t.cardid + " Date:" + t.GetTransaction());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}		
				
				//Capture the task end time
				Calendar ec = Calendar.getInstance();
				perfendtime = ec.getTimeInMillis();
				
				//DEBUG - Display the operation time
				//long runtime = Math.abs(perfendtime-perfstarttime);
				//Toast.makeText(cc, "Execution Time: " + runtime + " ms" ,Toast.LENGTH_LONG).show();

				pictureBitmap.recycle();				
				camera.startPreview();
				foundface = false;
				isdetecting = false;
				mReader.Light(false);
        }
    };
    
    private final ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() { 
        	shutter.start(); //shutter sound
        } 
    }; 
    
	@Override
	public void onFaceDetection(Face[] arg0, Camera arg1) {
		
		if(arg0.length == 1){ //Photo capture only STARTS if it detects 1 face
			if(arg0[0].score >= 50){ //trivial confidence check
				
				//Translate the face bound coordinates to view coordinates
				mTrackBox.ScaleFacetoView(arg0, winWidth, winHeight, facebox);
				
				//Display the tracking box
				mTrackBox.setInvalidate();
				mTrackBox.setVisibility(View.VISIBLE);				
				
				if (foundface == false){
					
					//Disable other photo capture tasks from starting until the current operation has completed
					foundface = true; 
					
					Handler mHandler = new Handler();
					Runnable photorequest = new Runnable() {
						public void run() {
							mTrackBox.clearView();
							mCamera.takePicture(shutterCallback, null, jpegPictureCallback);
						}
					};
					
					//Short delay before camera takes picture
					mHandler.postDelayed(photorequest, CAMERA_DELAY_MILLI);
					
				}
			}
		}
	}

}
