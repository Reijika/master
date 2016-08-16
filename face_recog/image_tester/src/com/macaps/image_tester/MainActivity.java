package com.macaps.image_tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
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

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity {
		
	//Image processing	
	private Button conversion_but;
	private TextView conversion_status;
	private TextView detect_txtview;
	private TextView success_txtview;
	private TextView failure_txtview;	
	private int successcount = 0;
	private int failurecount = 0;	
	private File [] conversion_array;

	//WEBP COMPRESSION
	private Button webp_but;
	private TextView webp_status;
	private TextView webp_detect;
	private TextView webp_success;
	private TextView webp_failure;	
	private int webp_successcount = 0;
	private int webp_failurecount = 0;	
	private File [] webp_test_array; //for all image files
	private File [] webp_training_set; //for the 1st image of every person

    
	//LBPH testing
	private Button lbph_test_but;
	private TextView lbph_test_status;
	private TextView lbph_test_count;
	private TextView lbph_correct_match;
	private TextView lbph_false_positive;
	private TextView lbph_false_negative;
	private TextView lbph_correct_unknown;	
	private File [] lbph_test_array; //for all image files
	private File [] lbph_training_set; //for the 1st image of every person
	private File [] lbph_input_set; //for the 2nd image of every person
	
	private Point [] lbph_opencv_poi = new Point[3];
	private Point [] lbph_flandmark_poi = new Point[8];
	private File [] lbph_file_set;
	
	//ORB testing
	private Button orb_test_but;
	private TextView orb_test_status;
	private TextView orb_test_count;
	private TextView orb_correct_match;
	private TextView orb_false_positive;
	private TextView orb_false_negative;
	private TextView orb_correct_unknown;	
	private File [] orb_test_array; //for all image files
	private File [] orb_training_set; //for the 1st image of every person
	private File [] orb_input_set; //for the 2nd image of every person
	
	private Point [] orb_opencv_poi = new Point[3];
	private Point [] orb_flandmark_poi = new Point[8];
	private File [] orb_file_set;
	
	//CN testing
	private Point [] opencv_poi = new Point[3];
	private Point [] flandmark_poi = new Point[8];	
	private static final int OUTPUT_WIDTH = 300;
	private static final int OUTPUT_HEIGHT = 300;
	private Button cnn_test_but;
	private TextView cnn_test_status;
	private TextView cnn_test_count;
	private TextView cnn_correct_match;
	private TextView cnn_false_positive;
	private TextView cnn_false_negative;
	private TextView cnn_correct_unknown;	
	private File [] cnn_test_array; //for all image files
	private File [] cnn_training_set; //for the 1st image of every person
	private File [] cnn_input_set; //for the 2nd image of every person
	private File [] cnn_file_set;
	
	
    static {
        if (!OpenCVLoader.initDebug()) {
        	System.exit(0);
        } else {
        	System.loadLibrary("opencv_java");        	
        	System.loadLibrary("contrast_filter");
        	System.loadLibrary("fp_detector");
        	System.loadLibrary("lbph_algorithm");
        	System.loadLibrary("orb_algorithm");    
        	System.loadLibrary("cnn_algorithm");
        }
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Image Processing Views
        conversion_but = (Button) findViewById (R.id.button1);
        conversion_status = (TextView) findViewById (R.id.status);
        detect_txtview = (TextView) findViewById(R.id.detected);
        success_txtview = (TextView) findViewById (R.id.success);
        failure_txtview = (TextView) findViewById (R.id.failed);
        
        //Image Processing Views () WEBH compression
        webp_but = (Button) findViewById (R.id.button5);
        webp_status = (TextView) findViewById (R.id.webp_status);
        webp_detect = (TextView) findViewById(R.id.webp_detected);
        webp_success = (TextView) findViewById (R.id.webp_success);
        webp_failure = (TextView) findViewById (R.id.webp_failed);
        
        //LBPH Test Views
        lbph_test_but = (Button) findViewById(R.id.button2);
        lbph_test_status = (TextView) findViewById(R.id.ltest_status);
        lbph_test_count = (TextView) findViewById(R.id.ltested_images);
        lbph_correct_match = (TextView) findViewById(R.id.lcorrect_match);
        lbph_false_positive = (TextView) findViewById(R.id.lfalse_positives);
        lbph_false_negative = (TextView) findViewById(R.id.lfalse_negatives);
        lbph_correct_unknown = (TextView) findViewById(R.id.lcorrect_unknown);
        
        orb_test_but = (Button) findViewById(R.id.button3);
        orb_test_status = (TextView) findViewById(R.id.otest_status);
        orb_test_count = (TextView) findViewById(R.id.otested_images);
        orb_correct_match = (TextView) findViewById(R.id.ocorrect_match);
        orb_false_positive = (TextView) findViewById(R.id.ofalse_positives);
        orb_false_negative = (TextView) findViewById(R.id.ofalse_negatives);
        orb_correct_unknown = (TextView) findViewById(R.id.ocorrect_unknown);
        
        cnn_test_but = (Button) findViewById(R.id.button4);
        cnn_test_status = (TextView) findViewById(R.id.ctest_status);
        cnn_test_count = (TextView) findViewById(R.id.ctested_images);
        cnn_correct_match = (TextView) findViewById(R.id.ccorrect_match);
        cnn_false_positive = (TextView) findViewById(R.id.cfalse_positives);
        cnn_false_negative = (TextView) findViewById(R.id.cfalse_negatives);
        cnn_correct_unknown = (TextView) findViewById(R.id.ccorrect_unknown);
        
        detectAndPopulate();        
    }
    
    private void detectAndPopulate(){

        //Detect files for conversion
        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/macaps_conversion_input";
        File filepath1 = new File(path1);
        if (!filepath1.exists()){
        	Toast.makeText(getApplicationContext(), "Your macaps_conversion_input folder is missing.",Toast.LENGTH_LONG).show();
        }
        else{
            conversion_but.setOnClickListener(conversionListener);        	
        	List<File> file_list = getListFiles(filepath1);
            conversion_array = (File[]) file_list.toArray(new File[file_list.size()]);            
            int size = conversion_array.length;
            detect_txtview.setText("Files Detected: " + size);
        }
        GenerateFileList("info/conversion_filelist.txt");

        //Detect files for compression testing
        String path5 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/processed_cnn_input";
        File filepath5 = new File(path5);
        if (!filepath5.exists()){
        	Toast.makeText(getApplicationContext(), "Your processed_cnn_input folder is missing.",Toast.LENGTH_LONG).show();
        }
        else{
            webp_but.setOnClickListener(webhcompressionListener);        	
        	List<File> test_queue = getListFiles(filepath5);        	
        	webp_test_array = (File[]) test_queue.toArray(new File[test_queue.size()]);
            int size = webp_test_array.length;
            webp_detect.setText("Files Detected: " + size);
        }
        GenerateFileList("info/webp_filelist.txt");
        
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/lbph_input";
        File filepath2 = new File(path2);
        if (!filepath2.exists()){
        	Toast.makeText(getApplicationContext(), "Your lbph_input is missing.",Toast.LENGTH_LONG).show();
        }
        else{
        	lbph_test_but.setOnClickListener(lbph_testListener);
        	List<File> test_queue = getListFiles(filepath2);
        	lbph_test_array = (File[]) test_queue.toArray(new File[test_queue.size()]);        	        	
        }
        GenerateFileList("info/lbph_filelist.txt");
        
        String path3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/orb_input";
        File filepath3 = new File(path3);
        if (!filepath3.exists()){
        	Toast.makeText(getApplicationContext(), "Your orb_input is missing.",Toast.LENGTH_LONG).show();
        }
        else{
        	orb_test_but.setOnClickListener(orb_testListener);
        	List<File> test_queue = getListFiles(filepath3);
        	orb_test_array = (File[]) test_queue.toArray(new File[test_queue.size()]);        	        	
        }
        GenerateFileList("info/orb_filelist.txt");
        
        String path4 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/cnn_input";
        File filepath4 = new File(path4);
        if (!filepath4.exists()){
        	Toast.makeText(getApplicationContext(), "Your cnn_input is missing.",Toast.LENGTH_LONG).show();
        }
        else{
        	cnn_test_but.setOnClickListener(cnn_testListener);
        	List<File> test_queue = getListFiles(filepath4);
        	cnn_test_array = (File[]) test_queue.toArray(new File[test_queue.size()]);        	        	
        }
        GenerateFileList("info/cnn_filelist.txt");
    }

    private void GenerateFileList(String exit_dir){
	 	 
	try {
		String filelist = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/" + exit_dir;	
		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter(filelist));	
	 
		if (exit_dir == "info/conversion_filelist.txt"){
			for (int i = 0; i < conversion_array.length; i++) {
			 	outputWriter.write(conversion_array[i].toString());
				outputWriter.newLine();			 
			}
		}
		else if (exit_dir == "info/lbph_filelist.txt"){
			for (int i = 0; i < lbph_test_array.length; i++) {
			 	outputWriter.write(lbph_test_array[i].toString());
				outputWriter.newLine();			 
			}			
		}
		else if (exit_dir == "info/orb_filelist.txt"){
			for (int i = 0; i < orb_test_array.length; i++) {
			 	outputWriter.write(orb_test_array[i].toString());
				outputWriter.newLine();			 
			}
		}
		else if (exit_dir == "info/cnn_filelist.txt"){
			for (int i = 0; i < cnn_test_array.length; i++) {
			 	outputWriter.write(cnn_test_array[i].toString());
				outputWriter.newLine();			 
			}
		}	
		else if (exit_dir == "info/webp_filelist.txt"){
			for (int i = 0; i < webp_test_array.length; i++) {
			 	outputWriter.write(webp_test_array[i].toString());
				outputWriter.newLine();			 
			}
		}		

			 
	 
		outputWriter.flush();
		outputWriter.close();
		
		File temp = new File(filelist);
		MediaScannerConnection.scanFile(getApplicationContext(), new String[] { temp.getAbsolutePath() }, null, null);
	 	} catch (IOException e) {e.printStackTrace();}	  
	    
    }
    
    private List<File> getListFiles(File parentDir) {
    	ArrayList<File> inFiles = new ArrayList<File>();
    	File[] files = parentDir.listFiles();
    	for (File file : files) {
    		if (file.isDirectory()) {
    			inFiles.addAll(getListFiles(file));
    		} 
    		else { 
    			if(file.getName().endsWith(".jpeg")){
    				inFiles.add(file);
    			} 
    			if (file.getName().endsWith(".jpg")){
    				inFiles.add(file);
    			}
    			if (file.getName().endsWith(".txt")){
    				inFiles.add(file);
    			}
    			if (file.getName().endsWith(".webp")){
    				inFiles.add(file);
    			}
    		} 
    	}
    	return inFiles;
    }    
    
    private OnClickListener conversionListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			conversion_but.setOnClickListener(null);			
			conversion_status.setText("Status: In progress...");
			conversion_trd.start();
		}
		
	    Thread conversion_trd = new Thread(new Runnable(){
	    	  @Override 
	    	  public void run(){  			
	  			
	  			//Generate output directory
	  			String outputdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/macaps_conversion_output";
	  			File output = new File(outputdir);			
	  			if (!output.exists()) {
	      			output.mkdirs();
	      			MediaScannerConnection.scanFile(getApplicationContext(), new String[] { output.getAbsolutePath() }, null, null);
	  			}
	  			
	  			//if files do exist in the input directory
	  			if (conversion_array.length > 0){
	  				for (File result : conversion_array) {					
	  					final Bitmap temp = BitmapFactory.decodeFile(result.toString());
	  					
	  					//determine the file directory destination 
	  					String filepath = result.getAbsolutePath();
	  					String[] words = filepath.split("/");
	  					String person = File.separator + words[words.length-2];
	  					String photo  = words[words.length-1];
	  					String person_folder = outputdir + person;
	  					String finalpath = outputdir + person + File.separator;
	  					
	  					//Generate person's folder if necessary					
	  					File asdf = new File(person_folder);
	  					if (!asdf.exists()){
	  						asdf.mkdirs();
	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { asdf.getAbsolutePath() }, null, null);
	  					}
	  					//Bitmap scaledBitmap = Bitmap.createScaledBitmap(temp, 384, 640, true);
	  					//Bitmap scaledBitmap = Bitmap.createScaledBitmap(temp, 480, 640, true);
	  					Bitmap rotatedBitmap = alignFaceAndCrop(temp);
	  					
	  					
	  					if (rotatedBitmap == null){
	  						failurecount++;
	  						runOnUiThread(new Runnable() {
	  	  					    public void run() {  	  					    	
	  	  					    	failure_txtview.setText("Files Skipped (Detection Failed): " + failurecount);  					    			    	
	  	  					}});
	  						
	  					}
	  					else{
	  						Bitmap finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, OUTPUT_WIDTH, OUTPUT_HEIGHT, true);						
	  	  					
	  	  					try {
	  	  						//save the cropped image
	  	  						OutputStream fOut = null;
	  	  						File file1 = new File(finalpath + "original_" + photo);						
	  	  						fOut = new FileOutputStream(file1);          
	  	  						finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // 100 means no compression, the lower you go, the stronger the compression			
	  	  						fOut.flush();
	  	  						fOut.close();
	  	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { file1.getAbsolutePath() }, null, null);
	  	  						
	  	  						//saved the marked image
	  	  						Bitmap markedBitmap = markFeaturePoints(finalBitmap);
	  	  						fOut = null;
	  	  						File file2 = new File(finalpath + "marked_" + photo);						
	  	  						fOut = new FileOutputStream(file2);          
	  	  						finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // 100 means no compression, the lower you go, the stronger the compression			
	  	  						fOut.flush();
	  	  						fOut.close();
	  	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { file2.getAbsolutePath() }, null, null);
	  	  						
	  	  						//Record the detected feature points
	  	  						File file3 = new File(finalpath + "info_" + photo + "_o.txt");
	  	  						String temp_path = finalpath + "info_" + photo + "_o.txt";	  	  						
	  	  						BufferedWriter outputWriter  = new BufferedWriter(new FileWriter(temp_path));

	  	  						for (Point s : opencv_poi){
	  	  							outputWriter.write(s.x + " , " + s.y);
	  	  							outputWriter.newLine();
	  	  						}
	  	  						for (Point t : flandmark_poi){
	  	  							outputWriter.write(t.x + " , " + t.y);
	  	  							outputWriter.newLine();
	  	  						}
	  	  						outputWriter.flush();
	  	  						outputWriter.close();
	  	  							  	  						
	  	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { file3.getAbsolutePath() }, null, null);	  	  						
	  	  					}
	  	  					catch (FileNotFoundException e) {} 
	  	  			        catch (IOException e) {}
	  	  					
	  	  					successcount++;			
	  	  					runOnUiThread(new Runnable() {
	  	  					    public void run() {  	  					    	
	  	  					    	success_txtview.setText("Files Successfully Converted: " + successcount);  					    			    	
	  	  					}}); 	  	
	  					}  	
	  					//temp.recycle();
	  					//rotatedBitmap.recycle();
	  				}				
	  			}
	  			else{
	  				return;
	  			}				
	  			  			
	    	} 
	    });
    };
    
    private OnClickListener webhcompressionListener = new OnClickListener(){
    	@Override
		public void onClick(View v) {
    		webp_but.setOnClickListener(null);
    		webp_status.setText("Compression Status: In progress...");
    		webpcompression_trd.start();
		}
    	
    	Thread webpcompression_trd = new Thread(new Runnable(){
			@Override
			public void run() {
				
				//Grab 1st picture for every person.
				cnn_training_set = new File[cnn_test_array.length / 3];						
				int counter2 = 0;
				for (int i = 2; i < cnn_test_array.length; i = i + 3){
					cnn_training_set [counter2] = cnn_test_array[i];
					counter2++;
				}
				
				//Scaled the comparison image down, process it, compress to webh and save
				if (cnn_training_set.length > 0){
	  				for (File result : cnn_training_set) {					
	  						  					
	  					//determine the file directory destination
	  					String outputdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/processed_cnn_input";
	  					
	  					String filepath = result.getAbsolutePath();	  					
	  					String[] words = filepath.split("/");
	  					
	  					String person = File.separator + words[words.length-2]; 			//Subject's folder name	  					
	  					String photo  = words[words.length-1];	  							//Subject photo name
	  					String photo_no_ext = photo.substring(0, photo.lastIndexOf('.'));   //Subject photo name (extension removed)
    	
	  					String person_folder = outputdir + person;							//Path to subject's folder
	  					String finalpath = outputdir + person + File.separator;				
	  					
	  					//Generate person's folder if necessary					
	  					File file1 = new File(person_folder);
	  					if (!file1.exists()){
	  						file1.mkdirs();
	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { file1.getAbsolutePath() }, null, null);
	  					}
	  					
	  					//Additional processing and scaling
	  					Bitmap temp = BitmapFactory.decodeFile(result.toString());
	  					Bitmap processed = processImage(temp, -1);
	  					Bitmap scaled = Bitmap.createScaledBitmap(processed, 90, 90, true);
	  					
	  					//Nullpointer check
	  					if (scaled == null){
	  						webp_failurecount++;
	  					}
  	  					
	  					//Save the new image to the processed_cnn_input directory as a WEBP file
  	  					try {
  	  						//save the cropped image
  	  						OutputStream fOut = null;
  	  						File file2 = new File(finalpath + photo_no_ext + ".webp");						
  	  						fOut = new FileOutputStream(file2);      
  	  						scaled.compress(Bitmap.CompressFormat.WEBP, 45, fOut); // 100 means no compression, the lower you go, the stronger the compression  	  									
  	  						fOut.flush();
  	  						fOut.close();
  	  						MediaScannerConnection.scanFile(getApplicationContext(), new String[] { file2.getAbsolutePath() }, null, null);
  	  						
  	  						webp_successcount++;
  	  						
  	  						runOnUiThread(new Runnable() {
  	  					    public void run() {  	  					    	
  	  					    	webp_success.setText("Files Successfully Converted: " + webp_successcount);  					    			    	
  	  					    }});
  	  					}
  	  					catch(Exception e){}

	  				}
				}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				runOnUiThread(new Runnable() {
					    public void run() {  	  					    	
					    	webp_status.setText("Compression Status: Complete");  					    			    	
					    }});
			}			
    	});
    };
    
    private OnClickListener lbph_testListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			lbph_test_but.setOnClickListener(null);
			lbph_test_status.setText("Test Status: In progress...");
			lbph_test_trd.start();			
		}
		
	    Thread lbph_test_trd = new Thread(new Runnable(){
			@Override
			public void run() {				
				
				///////////////////////////////////////////////////////////////////////////
				//Grab feature point record for the 1st picture for every person.
				lbph_file_set = new File[lbph_test_array.length / 3];						
				int counter3 = 0;
				for (int i = 0; i < lbph_test_array.length; i = i + 3){
					lbph_file_set [counter3] = lbph_test_array[i];
					counter3++;
				}
				///////////////////////////////////////////////////////////////////////////
				
				//Grab 1st picture for every person.
				lbph_training_set = new File[lbph_test_array.length / 3];						
				int counter1 = 0;
				for (int i = 1; i < lbph_test_array.length; i = i + 3){
					lbph_training_set [counter1] = lbph_test_array[i];
					counter1++;
				}
				
				//Grab 2nd picture for every person.
				lbph_input_set = new File[lbph_test_array.length / 3];
				int counter2 = 0;
				for (int i = 2; i < lbph_test_array.length; i = i + 3){
					lbph_input_set [counter2] = lbph_test_array[i];
					counter2++;
				}
				
				int match = 0;
				int fpositive = 0;
				int fnegative = 0;
				int miss = 0;
				int total = 0;
				
				LBPHAlgorithm tester = new LBPHAlgorithm();
				for (int i = 0; i < lbph_input_set.length; i++){ //for (int i = 0; i < input_set.length;i++){
					for (int h = i; h < lbph_training_set.length; h++ ){
										
						///////////////////////////////////////////////////////////////////////////////////////
						//parse the corresponding feature file and load the points into the arrays for later use
						final StringBuilder text = new StringBuilder();					 
						try { 
						    BufferedReader br = new BufferedReader(new FileReader(lbph_file_set[i]));
						    String line;					 
						    while ((line = br.readLine()) != null) {
						        text.append(line);
						        text.append('\n');
						    } 
						    br.close();
						    final String[] split = text.toString().split(",|\\n");
						    final double[] points = new double[split.length];
						    for (int j = 0; j < split.length; j++){
						    	points[j] = Double.parseDouble(split[j]);					    	
						    }
						    
						    int count1 = 0;
						    for (int k = 0; k < lbph_opencv_poi.length; k++){
						    	lbph_opencv_poi[k] = new Point();
						    	lbph_opencv_poi[k].x = points[count1]; 
						    	lbph_opencv_poi[k].y = points[count1+1];
						    	count1 = count1 + 2;
						    }
						    
						    int count2 = 6;
						    for (int l = 0; l < lbph_flandmark_poi.length; l++){					    	
						    	lbph_flandmark_poi[l] = new Point();
						    	lbph_flandmark_poi[l].x = points[count2];
						    	lbph_flandmark_poi[l].y = points[count2+1];
						    	count2 = count2 + 2;
						    }
						} 
						
						catch (IOException e) {}
						
						///////////////////////////////////////////////////////////////////////////////////////
						
						Bitmap training_selection = BitmapFactory.decodeFile(lbph_training_set[h].toString());
						Bitmap input_selection = BitmapFactory.decodeFile(lbph_input_set[i].toString());
						
						Bitmap processed_train = processImage(training_selection, 1); //1
						Bitmap processed_input = processImage(input_selection, 1);	//1					
						Mat training_mat = new Mat (processed_train.getWidth(), processed_train.getHeight(), CvType.CV_8UC1);
						Mat input_mat = new Mat (processed_input.getWidth(), processed_input.getHeight(), CvType.CV_8UC1);						
						Utils.bitmapToMat(processed_train, training_mat);
						Utils.bitmapToMat(processed_input, input_mat);				
						
						
						boolean label = tester.test(training_mat.getNativeObjAddr(), input_mat.getNativeObjAddr());
						
						if (label && (h==i)){ //correct match
							match++;							
						}	
						else if(label && (h!=i)){ //false positive
							fpositive++;
						}
						else if(!label && (h==i)){ //false negative
							fnegative++;
						}
						else if (!label && (h!=i)){ //correct miss
							miss++;
						}
						total++;
						
						final int a = total;
						final int b = match;
						final int c = fpositive;
						final int d = fnegative;
						final int e = miss;
																		
						runOnUiThread(new Runnable() {
						    public void run() {
						    	lbph_test_count.setText("Images Tested: " + a);
						    	lbph_correct_match.setText("Correct Matches (33): " + b);
						    	lbph_false_positive.setText("False Positives(0): " + c);
						    	lbph_false_negative.setText("False Negatives(0): " + d);
						    	lbph_correct_unknown.setText("Correct Unknowns (528): " + e);						    	
						}});
					}
				}	
				
				runOnUiThread(new Runnable() {
				    public void run() {
				    	lbph_test_status.setText("Test Status: Complete.");				    	
				}});
			}    	
	    });		
    };
    
    private OnClickListener orb_testListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			orb_test_but.setOnClickListener(null);
			orb_test_status.setText("Test Status: In progress...");
			orb_test_trd.start();			
		}
		
	    Thread orb_test_trd = new Thread(new Runnable(){
			@Override
			public void run() {				
				
				///////////////////////////////////////////////////////////////////////////////
				//Grab feature point record for the 1st picture for every person.
				orb_file_set = new File[orb_test_array.length / 3];						
				int counter3 = 0;
				for (int i = 0; i < orb_test_array.length; i = i + 3){
					orb_file_set [counter3] = orb_test_array[i];
					counter3++;
				}
				///////////////////////////////////////////////////////////////////////////////
				
				//Grab 1st picture for every person.
				orb_training_set = new File[orb_test_array.length / 3];						
				int counter1 = 0;
				for (int i = 1; i < orb_test_array.length; i = i + 3){
					orb_training_set [counter1] = orb_test_array[i];
					counter1++;
				}
				
				//Grab 2nd picture for every person.
				orb_input_set = new File[orb_test_array.length / 3];
				int counter2 = 0;
				for (int i = 2; i < orb_test_array.length; i = i + 3){
					orb_input_set [counter2] = orb_test_array[i];
					counter2++;
				}
				
				int match = 0;
				int fpositive = 0;
				int fnegative = 0;
				int miss = 0;
				int total = 0;

				//Compare every single picture to every other one - //the correct match is always the comparison when index i == index h
				ORBAlgorithm tester = new ORBAlgorithm();
				String filelist = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/image_tester/info/orb_match_details.txt";
				BufferedWriter outputWriter = null;
				try {
					outputWriter = new BufferedWriter(new FileWriter(filelist));
				} catch (IOException e2) {e2.printStackTrace();}

				for (int i = 0; i < orb_input_set.length; i++){ //for (int i = 0; i < input_set.length;i++){
					for (int h = i; h < orb_training_set.length; h++ ){
						
						//////////////////////////////////////////////////////////////////////////////////////////////
						
						//parse the corresponding feature file and load the points into the arrays for later use
						final StringBuilder text = new StringBuilder();					 
						try { 
						    BufferedReader br = new BufferedReader(new FileReader(orb_file_set[i]));
						    String line;					 
						    while ((line = br.readLine()) != null) {
						        text.append(line);
						        text.append('\n');
						    } 
						    br.close();
						    final String[] split = text.toString().split(",|\\n");
						    final double[] points = new double[split.length];
						    for (int j = 0; j < split.length; j++){
						    	points[j] = Double.parseDouble(split[j]);					    	
						    }
						    
						    int count1 = 0;
						    for (int k = 0; k < orb_opencv_poi.length; k++){
						    	orb_opencv_poi[k] = new Point();
						    	orb_opencv_poi[k].x = points[count1]; 
						    	orb_opencv_poi[k].y = points[count1+1];
						    	count1 = count1 + 2;
						    }
						    
						    int count2 = 6;
						    for (int l = 0; l < orb_flandmark_poi.length; l++){					    	
						    	orb_flandmark_poi[l] = new Point();
						    	orb_flandmark_poi[l].x = points[count2];
						    	orb_flandmark_poi[l].y = points[count2+1];
						    	count2 = count2 + 2;
						    }						    
							
						} 
						catch (IOException e) {}
						
						//////////////////////////////////////////////////////////////////////////////////////////////
						
						
						
						Bitmap training_selection = BitmapFactory.decodeFile(orb_training_set[h].toString());
						Bitmap input_selection = BitmapFactory.decodeFile(orb_input_set[i].toString());
						
						Bitmap processed_train = processImage(training_selection, 2); //2
						Bitmap processed_input = processImage(input_selection, 2);		//2				
						Mat training_mat = new Mat (processed_train.getWidth(), processed_train.getHeight(), CvType.CV_8UC1);
						Mat input_mat = new Mat (processed_input.getWidth(), processed_input.getHeight(), CvType.CV_8UC1);						
						Utils.bitmapToMat(processed_train, training_mat);
						Utils.bitmapToMat(processed_input, input_mat);				

						boolean label = false;
						String code = tester.test(training_mat.getNativeObjAddr(), input_mat.getNativeObjAddr());						
					 	try {
							outputWriter.write(code);
							outputWriter.newLine();
						} catch (IOException e1) {e1.printStackTrace();}
						
						if (code.charAt(code.length()-1) == '0'){
							label = true;
						}						
						if (label && (h==i)){ //correct match
							match++;							
						}	
						else if(label && (h!=i)){ //false positive
							fpositive++;
						}
						else if(!label && (h==i)){ //false negative
							fnegative++;
						}
						else if (!label && (h!=i)){ //correct miss
							miss++;
						}
						total++;
						
						final int a = total;
						final int b = match;
						final int c = fpositive;
						final int d = fnegative;
						final int e = miss;
																		
						runOnUiThread(new Runnable() {
						    public void run() {
						    	orb_test_count.setText("Images Tested: " + a);
						    	orb_correct_match.setText("Correct Matches (33): " + b);
						    	orb_false_positive.setText("False Positives(0): " + c);
						    	orb_false_negative.setText("False Negatives(0): " + d);
						    	orb_correct_unknown.setText("Correct Unknowns (528): " + e);						    	
						}});
					}
					try {
						outputWriter.newLine();
					} catch (IOException e) {e.printStackTrace();}

				}
				try {
					outputWriter.flush();
					outputWriter.close();
					File temp = new File(filelist);
					MediaScannerConnection.scanFile(getApplicationContext(), new String[] { temp.getAbsolutePath() }, null, null);
				} catch (IOException e) {e.printStackTrace();}
				
				runOnUiThread(new Runnable() {
				    public void run() {
				    	orb_test_status.setText("Test Status: Complete.");				    	
				}});
			}    	
	    });		
    };
    
    private OnClickListener cnn_testListener = new OnClickListener(){
    	@Override
		public void onClick(View v) {
			cnn_test_but.setOnClickListener(null);
			cnn_test_status.setText("Test Status: In progress...");
			cnn_test_trd.start();			
		}
    	
    	Thread cnn_test_trd = new Thread(new Runnable(){
			@Override
			public void run() {
				
				//Grab feature point record for the 1st picture for every person.
				cnn_file_set = new File[cnn_test_array.length / 3];						
				int counter1 = 0;
				for (int i = 0; i < cnn_test_array.length; i = i + 3){
					cnn_file_set [counter1] = cnn_test_array[i];
					counter1++;
				}
									
				//Grab 1st picture for every person.
				cnn_input_set = new File[cnn_test_array.length / 3];
				int counter2 = 0;
				for (int i = 1; i < cnn_test_array.length; i = i + 3){
					cnn_input_set [counter2] = cnn_test_array[i];
					counter2++;
				}
				
				//Grab the scaled/compressed version of the 1st picture for every person
				webp_training_set = new File[webp_test_array.length];
				for (int i = 0; i < webp_test_array.length; i++){
					webp_training_set [i] = webp_test_array[i];
				}
			
				
				//Grab 1st picture for every person.
				//cnn_training_set = new File[cnn_test_array.length / 3];						
				//int counter2 = 0;
				//for (int i = 1; i < cnn_test_array.length; i = i + 3){
					//cnn_training_set [counter2] = cnn_test_array[i];
					//counter2++;
				//}
				
				int match = 0;
				int fpositive = 0;
				int fnegative = 0;
				int miss = 0;
				int total = 0;
				
				CNNAlgorithm tester = new CNNAlgorithm();
				for (int i = 0; i < cnn_input_set.length; i++){ //for (int i = 0; i < input_set.length;i++){
					for (int h = i; h < webp_training_set.length; h++ ){ //for (int h = i; h < cnn_training_set.length; h++ ){
						
						//parse the corresponding feature file and load the points into the arrays for later use
						final StringBuilder text = new StringBuilder();					 
						try { 
						    BufferedReader br = new BufferedReader(new FileReader(cnn_file_set[i]));
						    String line;					 
						    while ((line = br.readLine()) != null) {
						        text.append(line);
						        text.append('\n');
						    } 
						    br.close();
						    final String[] split = text.toString().split(",|\\n");
						    final double[] points = new double[split.length];
						    for (int j = 0; j < split.length; j++){
						    	points[j] = Double.parseDouble(split[j]);					    	
						    }
						    
						    int count1 = 0;
						    for (int k = 0; k < opencv_poi.length; k++){
						    	opencv_poi[k] = new Point();
						    	opencv_poi[k].x = points[count1]; 
						    	opencv_poi[k].y = points[count1+1];
						    	count1 = count1 + 2;
						    }
						    
						    int count2 = 6;
						    for (int l = 0; l < flandmark_poi.length; l++){					    	
						    	flandmark_poi[l] = new Point();
						    	flandmark_poi[l].x = points[count2];
						    	flandmark_poi[l].y = points[count2+1];
						    	count2 = count2 + 2;
						    }
						    
						    //Scaling the feature points from 300x300 (when they were records to 100x100 resolution)
						    double NEW_WIDTH = 90; //100
						    double scalefactor = (double)NEW_WIDTH/(double)OUTPUT_WIDTH;							
							for (int z = 0; z < opencv_poi.length; z++){
								opencv_poi[z].x = opencv_poi[z].x * scalefactor; 
								opencv_poi[z].y	= opencv_poi[z].y * scalefactor; 		
							}
							for (int m = 0; m < flandmark_poi.length; m++){
								flandmark_poi[m].x = flandmark_poi[m].x * scalefactor; 
								flandmark_poi[m].y	= flandmark_poi[m].y * scalefactor;			
							}
							
						} 
						catch (IOException e) {}
						
						
						//Process the pair of images prior to comparison
						Bitmap scaled_train = BitmapFactory.decodeFile(webp_training_set[h].toString());
						
						Bitmap input_selection = BitmapFactory.decodeFile(cnn_input_set[i].toString());
						Bitmap processed_input = processImage(input_selection, -1);
						Bitmap scaled_input = Bitmap.createScaledBitmap(processed_input, 90, 90, true);						
						processed_input.recycle();
						input_selection.recycle();
						
						Mat training_mat = new Mat (scaled_train.getWidth(), scaled_train.getHeight(), CvType.CV_8UC1);
						Mat input_mat = new Mat (scaled_input.getWidth(), scaled_input.getHeight(), CvType.CV_8UC1);						
						Utils.bitmapToMat(scaled_train, training_mat);
						Utils.bitmapToMat(scaled_input, input_mat);
						
						
						/*
						//Process the pair of images prior to comparison
						Bitmap training_selection = BitmapFactory.decodeFile(cnn_training_set[h].toString());
						Bitmap input_selection = BitmapFactory.decodeFile(cnn_input_set[i].toString());
						
						//so apparently masking makes hell of a difference. 5,5 or then its 0,7 lol
						Bitmap processed_train = processImage(training_selection, true);
						Bitmap processed_input = processImage(input_selection, true);	
						
						Bitmap scaled_train = Bitmap.createScaledBitmap(processed_train, 100, 100, true);						
						Bitmap scaled_input = Bitmap.createScaledBitmap(processed_input, 100, 100, true);
						processed_train.recycle();
						processed_input.recycle();
						
						Mat training_mat = new Mat (scaled_train.getWidth(), scaled_train.getHeight(), CvType.CV_8UC1);
						Mat input_mat = new Mat (scaled_input.getWidth(), scaled_input.getHeight(), CvType.CV_8UC1);						
						Utils.bitmapToMat(scaled_train, training_mat);
						Utils.bitmapToMat(scaled_input, input_mat);				
						*/
												
						boolean label = false;
						int result = tester.test(training_mat.getNativeObjAddr(), input_mat.getNativeObjAddr(), opencv_poi, flandmark_poi);
						if (result == 0){
							label = true;
						}
						
						if (label && (h==i)){ //correct match
							match++;							
						}	
						else if(label && (h!=i)){ //false positive
							fpositive++;
						}
						else if(!label && (h==i)){ //false negative
							fnegative++;
						}
						else if (!label && (h!=i)){ //correct miss
							miss++;
						}
						total++;
						
						final int a = total;
						final int b = match;
						final int c = fpositive;
						final int d = fnegative;
						final int e = miss;
																		
						runOnUiThread(new Runnable() {
						    public void run() {
						    	cnn_test_count.setText("Images Tested: " + a);
						    	cnn_correct_match.setText("Correct Matches (33): " + b);
						    	cnn_false_positive.setText("False Positives(0): " + c);
						    	cnn_false_negative.setText("False Negatives(0): " + d);
						    	cnn_correct_unknown.setText("Correct Unknowns (528): " + e);						    	
						}});
					}
				}	
				
				runOnUiThread(new Runnable() {
				    public void run() {
				    	cnn_test_status.setText("Test Status: Complete.");				    	
				}});			
				
				
			}
    	});    	
    };   
    
	private Bitmap processImage(Bitmap image, int flag){
		//grayscale -> gamma correction -> face + eye detection -> pose correction -> cropping -> scaling ->
		//gaussian blurring/difference of gaussian -> Contrast Limited Adaptive Histogram Equalization
 		
		Mat greymat = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);				
		Utils.bitmapToMat(image, greymat);

	    Mat mask = new Mat(greymat.rows(), greymat.cols(), CvType.CV_8UC1, new Scalar(0,0,0));
	    Mat result = new Mat (greymat.rows(), greymat.cols(), CvType.CV_8UC1, new Scalar(0,0,0));
	    
	    //Additional processing for LBPH
		if (flag == 1){
			Imgproc.GaussianBlur(greymat, greymat, new Size(5,5),0.7,0.7);			
			double eye_distance = (double)Math.sqrt((lbph_opencv_poi[1].x-lbph_opencv_poi[0].x)*(lbph_opencv_poi[1].x-lbph_opencv_poi[0].x) + (lbph_opencv_poi[1].y-lbph_opencv_poi[0].y)*(lbph_opencv_poi[1].y-lbph_opencv_poi[0].y));
			eye_distance = eye_distance / 2.0d;			
		    Core.ellipse(mask, new Point (lbph_opencv_poi[2].x, lbph_opencv_poi[2].y+eye_distance*0.40), new Size( eye_distance*2.05, eye_distance*2.1*1.3 ), 0.0d, 0.0d, 360.0d, new Scalar( 255, 255, 255), -1, 8,0 );		    
		    greymat.copyTo(result, mask);
		    greymat = result.clone();
		    
		}
		//Additional processing for ORB
		else if (flag == 2){
			Imgproc.GaussianBlur(greymat, greymat, new Size(5,5),0.7,0.7);
			//double eye_distance = (double)Math.sqrt((orb_opencv_poi[1].x-orb_opencv_poi[0].x)*(orb_opencv_poi[1].x-orb_opencv_poi[0].x) + (orb_opencv_poi[1].y-orb_opencv_poi[0].y)*(orb_opencv_poi[1].y-orb_opencv_poi[0].y));
			//eye_distance = eye_distance / 2.0d;			
		    //Core.ellipse(mask, new Point (orb_opencv_poi[2].x, orb_opencv_poi[2].y+eye_distance*0.40), new Size( eye_distance*2.05, eye_distance*2.1*1.3 ), 0.0d, 0.0d, 360.0d, new Scalar( 255, 255, 255), -1, 8,0 );		    
		    //greymat.copyTo(result, mask);
		    //greymat = result.clone();		    			
		}
		
	    result.release();
	    mask.release();
		
		
		
		ContrastFilter filter = new ContrastFilter();
		Mat contrast_output = greymat.clone();				
		contrast_output = filter.EqualizeHistogram(greymat);
		Utils.matToBitmap(contrast_output, image);
		contrast_output.release();
		
		return image;
	}
	
	private double calculateRotation(double leftx, double lefty, double rightx, double righty){
		
		double degree_rotation = 0;
		
		double adjacent = Math.abs(leftx - rightx);
		double opposite = Math.abs(lefty - righty);
		
		if (adjacent == 0){ //to avoid divide by zero exception (unlikely, requires x coordinates to be the same)
			return 0;
		}
		
		degree_rotation = Math.toDegrees(Math.atan(opposite/adjacent)); 
		
		if (degree_rotation == 0){ //in the event the eye y coordinates are on the same pixel level,
			return degree_rotation;
		}
		
		//if some rotation is actually required
		if (lefty > righty){ //rotate counter clockwise				
			degree_rotation = degree_rotation * -1.0d;
		}
		else if (lefty < righty){ //rotate clockwise
			degree_rotation = degree_rotation * 1.0d;
		}		
				
		//Toast.makeText(cc, "The image must be rotated " + degree_rotation + " degrees!",Toast.LENGTH_SHORT).show();
		return degree_rotation;
	}
	
	private Bitmap alignFaceAndCrop(Bitmap picture){ 
	//Gray Scale, Gamma Correction, Eye Detection, Facial Point Detection, Pose Alignment, Cropping 
		
		if (picture == null){
			Toast.makeText(getApplicationContext(), "Bitmap = null. Returning...",Toast.LENGTH_LONG).show();
			return null;
		}
		
		Bitmap bmp;		

		String face_cascade_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/cascades/lbpcascade_frontalface.xml";
		String eyes_cascade_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/cascades/haarcascade_eye_tree_eyeglasses.xml";
		
		CascadeClassifier frontalface_cascade = new CascadeClassifier(face_cascade_path);		
		CascadeClassifier eye_cascade = new CascadeClassifier(eyes_cascade_path);
					
		if (frontalface_cascade.empty() && eye_cascade.empty()){
			Toast.makeText(getApplicationContext(), "CascadeClassifier initialization failed.",Toast.LENGTH_LONG).show();	
			bmp = null;
		}
		else{		
			
			Mat src = new Mat (picture.getWidth(), picture.getHeight(), CvType.CV_8UC1);		
			Mat brightmat = new Mat (picture.getWidth(), picture.getHeight(), CvType.CV_8UC1);
			
			Utils.bitmapToMat(picture, brightmat);	
			Imgproc.cvtColor(brightmat, brightmat, Imgproc.COLOR_RGB2GRAY);
			
			//gamma correction prior to detection
			ContrastFilter gamma = new ContrastFilter();
			src = gamma.GammaCorrection(brightmat);
			brightmat.release();
			
			MatOfRect face = new MatOfRect();
			MatOfRect eyes = new MatOfRect();
			Mat roi_face = null;
			Rect face_rect = null;
						
			frontalface_cascade.detectMultiScale(src, face, 1.05, 3, 0, new Size(200,200), new Size(1280,1280));//50,50,400,400
			for (Rect rect : face.toArray()) {
				face_rect = rect.clone(); //capture the face bounds for later use
	            roi_face = src.submat(rect);	            
	            eye_cascade.detectMultiScale(roi_face, eyes, 1.05, 3, 0, new Size(50,50), new Size (300,300));//5,5,100,100
	        }
			
			//Toast.makeText(cc, "# of faces detected:  " + face.toArray().length + "\n"  + "# of eyes detected:   " + eyes.toArray().length + "\n",Toast.LENGTH_LONG).show();
			
			//If only 1 face and 2 eyes have been found
			if (face.toArray().length == 1 && eyes.toArray().length == 2){
				double midpointx = 0;
				double midpointy = 0;
				double left_eye_x = 0;
				double left_eye_y = 0;
				double right_eye_x = 0;
				double right_eye_y = 0;
				
				//assign left and right eyes
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
								
				//depend on which eye is 'higher', calculate the midpoint
				if (left_eye_y > right_eye_y){ //rotate counter clockwise				
					midpointx = left_eye_x + (Math.abs(right_eye_x - left_eye_x))/2.0d;
					midpointy = right_eye_y + (Math.abs(right_eye_y - left_eye_y))/2.0d;
				}
				else if (left_eye_y < right_eye_y){ //rotate clockwise
					midpointx = left_eye_x + (Math.abs(right_eye_x - left_eye_x))/2.0d;
					midpointy = left_eye_y + (Math.abs(right_eye_y - left_eye_y))/2.0d;
				}		
				
				//calculate the degrees required for the rotation
				double degrees = calculateRotation(left_eye_x, left_eye_y, right_eye_x, right_eye_y);				
								
				//Calculate the distance of the eye from the midpoint in the x-direction
				double eye_distance = (double)Math.sqrt((right_eye_x-left_eye_x)*(right_eye_x-left_eye_x) + (right_eye_y-left_eye_y)*(right_eye_y-left_eye_y));
				eye_distance = eye_distance / 2.0d;				
				
				//Update the midpoint coordinates for src rather than roi_face
				midpointx = midpointx + face_rect.x;
				midpointy = midpointy + face_rect.y;

				//Calculate the new expected eye coordinates
			    left_eye_x = midpointx - eye_distance;//left_difference;
				left_eye_y = midpointy;
				right_eye_x = midpointx + eye_distance;//right_difference;
				right_eye_y = midpointy;
				
								
				//Rotate the matrix image
				Point src_center = new Point(midpointx, midpointy);
				Mat rotationMatrix = Imgproc.getRotationMatrix2D(src_center, degrees, 1.0);				
			    Imgproc.warpAffine(src,src, rotationMatrix, src.size());
			    
			  //Calculate a arbitrary face bound for the Flandmark Detector				
				double offset_pct_x = 1.3; 
				double offset_pct_y = 2.0;
				int top_left_x  = (int)(left_eye_x - (eye_distance*offset_pct_x));
				int top_left_y = (int)(left_eye_y - (eye_distance*offset_pct_y));				
				int top_right_x = (int)(right_eye_x + (eye_distance*offset_pct_x));
				int top_right_y = (int)(right_eye_y - (eye_distance*offset_pct_y));				
				int corner_width = (int)(top_right_x - top_left_x);
				int corner_height = (int)((double)corner_width * 1.1d);				
				
				//Flandmark Detection
				int bounds[] = {top_left_x, top_left_y, (top_left_x + corner_width), (top_left_y + corner_height)};
		    	PointDetector fpdetector = new PointDetector();
		    	float [] feature_points = fpdetector.detectFeaturePoints(src, bounds);
		    	for (int i = 0; i < 16; i = i + 2){
		    		feature_points[i] = feature_points[i];
		    		feature_points[i+1] = feature_points[i+1];
		    	}

				//Update the new crop bounds
			    double offset_pct = 0.8; 
				top_left_x  = (int)(left_eye_x - (eye_distance*offset_pct));
				top_left_y = (int)(left_eye_y - (eye_distance*offset_pct));				
				top_right_x = (int)(right_eye_x + (eye_distance*offset_pct));
				top_right_y = (int)(right_eye_y - (eye_distance*offset_pct));				
				corner_width = (int)(top_right_x - top_left_x);
				corner_height = corner_width;
				
				//if the top left corner coordinates go negative, null
				if (top_left_x < 0 || top_left_y < 0){return null;}				
				//if the top right corner coordinates go negative, null
				if (top_right_x > src.cols() || top_right_y < 0){return null;}
				//if the bottom corners stay within the image heights
				if ((top_left_y + corner_height) > src.rows() || (top_right_y + corner_height) > src.rows()){return null;}
			    			    
				//Add a ellipse mask to remove some of the background
			    //Mat mask = new Mat(src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(0,0,0));
			    //Mat result = new Mat (src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(0,0,0));			    
			    //Core.ellipse(mask, new Point (midpointx, midpointy+eye_distance*0.40), new Size( eye_distance*2.05, eye_distance*2.1*1.3 ), 0.0d, 0.0d, 360.0d, new Scalar( 255, 255, 255), -1, 8,0 );			    
			    //src.copyTo(result, mask);			    
			    
			    //Crop out the face portion of the matrix image
			    Rect roi = new Rect(top_left_x, top_left_y, corner_width, corner_height);
			    Mat roi_mat = src.submat(roi).clone();			    
			    
			    //Calculate the adjusted feature point coordinates			    
			    double scale_factor = (double) OUTPUT_WIDTH/ (double)corner_width;			    
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

	private Bitmap markFeaturePoints(Bitmap processed_input){		
		
		Mat src = new Mat();		
		Utils.bitmapToMat(processed_input, src);
		
		//Mark the feature points
	    Core.circle(src, opencv_poi[0], 4, new Scalar(255, 255, 255)); 
		Core.circle(src, opencv_poi[1],4, new Scalar(255, 255, 255));			
	    Core.circle(src, opencv_poi[2], 4, new Scalar(0, 0, 0));
		Core.circle(src, flandmark_poi[0], 3, new Scalar(0, 0, 255),-1, 8,0);
		for (int i = 1; i < 8; i++){
			Core.circle(src, flandmark_poi[i], 3, new Scalar(255, 0, 0), -1, 8,0);			
		}
		
		Utils.matToBitmap(src, processed_input);
		return processed_input;
	}

	
}
