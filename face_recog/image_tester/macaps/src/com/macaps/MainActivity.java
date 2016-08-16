/*
LEGAL STUFF - This program utilizes OpenCV 2.4.9 which is distributed under a 3-clause BSD license: 

For an brief explanation of what is allowed to be done with software with a 3-clause BSD license, view the following sites:
https://tldrlegal.com/license/bsd-3-clause-license-(revised)
http://stackoverflow.com/questions/19312308/opencv-for-commercial-use
http://docs.opencv.org/modules/nonfree/doc/nonfree.html

////////////////////////////////////////////////[Copyright Notice]/////////////////////////////////////////////////////////////
IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.

By downloading, copying, installing or using the software you agree to this license. If you do not agree to this license, 
do not download, install, copy or use the software.

												License Agreement
									For Open Source Computer Vision Library
									
Copyright (C) 2000-2008, Intel Corporation, all rights reserved. Copyright (C) 2008-2011, Willow Garage Inc., 
all rights reserved. Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the distribution.

The name of the copyright holders may not be used to endorse or promote products derived from this software without specific 
prior written permission.

This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including, 
but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event 
shall the Intel Corporation or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential
damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business 
interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including 
negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
*/

package com.macaps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.macaps.camera.CameraPreview;
import com.macaps.R;
import com.macaps.network.Client;
import com.macaps.network.Transaction;
import com.macaps.reader.Reader;
import com.macaps.reader.Uart;

import android.Manifest.permission;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;



import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import com.macaps.camera.*;


public class MainActivity extends Activity {

 	//Timeout duration after photo capture has started with no face detection
	private static final int TIMEOUT_DELAY_MILLI = 5000;
	
	//Timeout duration before screen dims due to inactivity
	private static final int SLEEP_DELAY_MILLI = 30000;
	
	//The preferred APN Name
	private static final String NEW_APN_NAME = "MaCaPS HK";
	
	//Tracks the timeout progress before the screen is reset
	private long timeoutcounter = 0;	
	private long sleepercounter = 0;

	RelativeLayout mRelativeLayout;
	private CameraPreview mCameraPreview;
	private TrackBox trackbox;
	    
    public Vector<Transaction> vt = new Vector<Transaction>();
    public static  int width;
    public static  int height;
   
    public static DateChangeBroadcastReceiver dateReceiver;
    public static TextView date;
    public static ImageView image;
    public static DigitalClock clock;
    private TextView iptext;
    private MediaPlayer shutterSound;
       
    private Uart uart;
    private Reader reader;
    private Network network;
    private String cardid;
    
    private Bitmap [] temporary_training_set;
    
    //Load shared libraries
    static {
        if (!OpenCVLoader.initDebug()) {
        	System.exit(0);
        } else {
        	System.loadLibrary("opencv_java");
        	System.loadLibrary("face_recognizer");
        	System.loadLibrary("native_filter");
        	System.loadLibrary("fp_detector");
        }
    }
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        //Generate any resources the application needs from the assets folder
        GenerateResources();
                
        //Set window specifications
        requestWindowFeature(Window.FEATURE_NO_TITLE);        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
                
        setContentView(R.layout.activity_main);
                
        //Lock into portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        //Max screen brightness - between 0 to 1
        setBrightness(1.0f); 
        
        //Capture the width and height of screen for future use
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getRealSize(size);
    	width = size.x;
    	height = size.y;
    	
    	//Debug textview showing details pertaining to the face box custom view
    	TextView trackbox_text = (TextView) findViewById(R.id.coordinates);
    	trackbox_text.setVisibility(View.GONE); //change to View.Visible to see the details
    	
    	//Pass the any view references to the custom view to the camera preview object so it can draw/work with it
        shutterSound = MediaPlayer.create(this, R.raw.shutter);
    	mRelativeLayout =  (RelativeLayout) findViewById(R.id.layoutview);
        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);        
        trackbox = (TrackBox) findViewById(R.id.box_view); 
        
    	//Initialize debug ImageView
    	ImageView iv;
    	iv = new ImageView(this);
    	iv.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        iv.setImageResource(R.drawable.ic_launcher);
    	iv.setVisibility(View.GONE);        

    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//Load comparison image for face recognition
    	String comparison_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/comparison.webp";
    	File comparison_file= new File(comparison_path);
    	Bitmap tmp = null;
        if (comparison_file.exists()){        	
        	tmp = BitmapFactory.decodeFile(comparison_path);
        } 
        temporary_training_set = new Bitmap [1];
    	temporary_training_set [0] = tmp;
    	
    	//Attempt to load a secondary image for manual comparison
    	//If you wish to manually compare two images, change:
    	//NativeFaceRecognizer nfr = new NativeFaceRecognizer(processed_input, training_set) into
    	//NativeFaceRecognizer nfr = new NativeFaceRecognizer(input, training_set)    	
        String test_input_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/test_input.webp";        
        File test_input_file = new File(test_input_path);
        Bitmap test_input = null;
        if (test_input_file.exists()){
        	test_input = BitmapFactory.decodeFile(test_input_path);        	
        }
        
        //Null check prior to passing them to the camera preview
        if (test_input != null & tmp != null){
        	Toast.makeText(getApplicationContext(), "Loaded both images from storage.",Toast.LENGTH_LONG).show();
        }        
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        //Pass all the necessary object references and debug views
        mCameraPreview.passObjects(trackbox, trackbox_text, shutterSound, temporary_training_set, test_input, iv); //takes the debugging textview - remove on later builds
        
        //Initialize the background imageview
        image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        image.setImageResource(R.drawable.sino);
       
        //Declare a calendar instance and make a date textview for the time
        Calendar datecal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("E" + "\n" + "MMM dd");
        String formattedDate = df.format(datecal.getTime());
        date = new TextView(this);
        date.setTextColor(Color.GRAY);
        date.setTextSize(35);
        date.setId(1); //provide an id for the clock to anchor to
        date.setText(formattedDate);
        date.setPadding(0, 0, 35, 65);
                
        //Attach a broadcast receiver to the date textview
        dateReceiver = new DateChangeBroadcastReceiver(new Handler(), date);
        IntentFilter s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_DATE_CHANGED);                
        registerReceiver(dateReceiver, s_intentFilter);
                
        //Declare a digital clock view for the time
        clock = new DigitalClock(this);
        clock.setTextColor(Color.GRAY);
        clock.setTextSize(80);
        clock.setId(2);
        clock.setPadding(20,0,0,50);
                
        //Declare a textview for the ip information
    	iptext = new TextView(this);
    	iptext.setText(" Internal IP Address (Wifi): " + "\n" + " External IP Address: ");    	
        
    	//Set RelativeLayout parameters for the clock, date and iptext views
        RelativeLayout.LayoutParams clockRules = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        clockRules.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
          
        RelativeLayout.LayoutParams dateRules = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dateRules.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dateRules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        RelativeLayout.LayoutParams ipRules = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ipRules.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                
        //Attach all the relevant views to the RelativeLayout        
        mRelativeLayout.addView(image);
        mRelativeLayout.addView(clock,clockRules);   
        mRelativeLayout.addView(date, dateRules);
        mRelativeLayout.addView(iptext, ipRules);
        
        // Debug - A debug imageview for displaying the processed image used for comparison in the CameraPreview class
        mRelativeLayout.addView(iv);
        
        // Debug - dummy network for testing purposes - remove later		
		TESTNETWORK testnet = new TESTNETWORK();
		testnet.start();
				
		//Start a separate thread that attempts to establish a network connection every 10 seconds, will automatically choose a connectivity type, prioritizes 3G
		createNetwork.start();
				
		uart = new Uart(this);
		reader = new Reader(uart);
		reader.Beep(10);
		reader.Beep(10);
		reader.Beep(10);		
		CardReader cardreader = new CardReader(reader, image);
		cardreader.start();	
    }

    //Generated all the resources required for face recognition and image normalization
    private void GenerateResources(){
    	try {
    		String flandmark_model = "/DCIM/resources/flandmark_model.dat";
    		String eyes_cascade = "/DCIM/resources/haarcascade_eye_tree_eyeglasses.xml";
    		String lbp_cascade = "/DCIM/resources/lbpcascade_frontalface.xml";    		
            String cascadedir = "/DCIM/resources";            
    		String fullpath = Environment.getExternalStorageDirectory().getAbsolutePath();
    		
    		File flandmark_dat = new File (fullpath + flandmark_model);
			File eyes_xml = new File (fullpath + eyes_cascade);
			File lbp_xml = new File (fullpath + lbp_cascade);			
    		File dir = new File(fullpath + cascadedir);

    		boolean generateflandmarkdat = false;
    		boolean generateeyesxml = false;
    		boolean generatelbpxml = false;
    		    		
    		if (!dir.exists()) {
    			dir.mkdirs();
    			generateflandmarkdat = true;
    			generateeyesxml = true;
    			generatelbpxml = true;
    			Toast.makeText(getApplicationContext(), "Generating resources.",Toast.LENGTH_LONG).show();
    		}
    		else{
    			if (!flandmark_dat.exists()){
    				generateflandmarkdat = true;
    			}    			
    			if (!eyes_xml.exists()){
    				generateeyesxml = true;    				
    			}
    			if (!lbp_xml.exists()){
    				generatelbpxml = true;
    			}    			
    		}
    		
    		AssetManager assetManager = getAssets();
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		FileOutputStream fos;
    		 
    		baos = new ByteArrayOutputStream();
    		fos = null;
    		if (generateflandmarkdat){    			    		        
    			InputStream fland_is = assetManager.open("flandmark_model.dat");    		    	
    	        byte[] fland_buffer = new byte[fland_is.available()];
    	        fland_is.read(fland_buffer);    		        
    	        baos.write(fland_buffer);    	        
    	        
    	        fos = new FileOutputStream(fullpath +  flandmark_model);
    	        
    	        baos.writeTo(fos);
    	        baos.flush();
    	        fos.flush();
    	        
    	        fland_is.close();
    	        baos.close();
    	        fos.close();
    	        MediaScannerConnection.scanFile(getApplicationContext(), new String[] { flandmark_dat.getAbsolutePath() }, null, null);
    	        Toast.makeText(getApplicationContext(), "Generated model",Toast.LENGTH_LONG).show();
    		}
    		
    		baos = new ByteArrayOutputStream();
    		fos = null;
    		if (generateeyesxml){    			    		        
    			InputStream eyes_is = assetManager.open("haarcascade_eye_tree_eyeglasses.xml");    		    	
    	        byte[] eyes_buffer = new byte[eyes_is.available()];
    	        eyes_is.read(eyes_buffer);    		        
    	        baos.write(eyes_buffer);    	        
    	        
    	        fos = new FileOutputStream(fullpath + eyes_cascade);
    	        
    	        baos.writeTo(fos);
    	        baos.flush();
    	        fos.flush();
    	        
    	        eyes_is.close();
    	        baos.close();
    	        fos.close();
    	        MediaScannerConnection.scanFile(getApplicationContext(), new String[] { eyes_xml.getAbsolutePath() }, null, null);
    		}
    		
    		baos = new ByteArrayOutputStream();
    		fos = null;
    		if (generatelbpxml){
    			InputStream lbp_is = assetManager.open("lbpcascade_frontalface.xml");
    			byte[] lbp_buffer = new byte[lbp_is.available()];
    			lbp_is.read(lbp_buffer);
    			baos.write(lbp_buffer);
    			
    			fos = new FileOutputStream(fullpath + lbp_cascade);
    			
    			baos.writeTo(fos);
    			baos.flush();
    			fos.flush();
    			
    			lbp_is.close();
    			baos.close();
    			fos.close();
    			MediaScannerConnection.scanFile(getApplicationContext(), new String[] { lbp_xml.getAbsolutePath() }, null, null);    			
    		}
		} catch (Exception e) {e.printStackTrace();}
    	
    }
 
    //Attach a jpeg file to the imageview
    public void setImage() {
    	image.setImageResource(R.drawable.macaps);
    }

    //Set screen brightness  
    public void setBrightness(float lux){    	
    	WindowManager.LayoutParams lp = getWindow().getAttributes();
    	lp.screenBrightness = lux; 
    	getWindow().setAttributes(lp);    	
    }
    
    //A debug method for showing the contents of the preferred APN configuration setting
    public boolean displayPreferredAPN(TextView t){
    
    	Uri.parse("content://telephony/carriers");
    	final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    
    	//Confirm permissions
        PackageManager pm = getPackageManager();
        if (pm.checkPermission(permission.WRITE_APN_SETTINGS, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
        	        	                    	
            //Display the row contents (always has the original fields, doesn't update)
        	Cursor c = getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
        	c.moveToFirst();     

        	int index = c.getColumnIndex("_id");    //getting index of required column
            Short id = c.getShort(index);           //getting APN's id from        
            index = c.getColumnIndex("name");
            String name = c.getString(index);        
            index = c.getColumnIndex("mcc");
            String mcc = c.getString(index);        
            index = c.getColumnIndex("mnc");
            String mnc = c.getString(index);        
            index = c.getColumnIndex("numeric");
            String numeric = c.getString(index);
        	
        	t.setText(" ID:" + id + "\n" + 
          		  " APN Name: " + name + "\n" +
          		  " MCC: " + mcc + "\n" +
          		  " MNC: " + mnc + "\n" +
          		  " Numeric: " + numeric + "\n"          		 
          		);
        	
        } else { 
        	t.setText(" You don't have permission to do this. ");    
        }
       
    	return true;
    }
    
    //Generates a copy of the current preferred apn record but replaces the name with the value NEW_APN_NAME
    public int InsertAPN(String name){    	
    	
    	//Set the URIs and variables
    	int id = -1;
    	boolean existing = false;
    	final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    	final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    	 	
    	//Check if the specified APN is already in the APN table, if so skip the insertion    	    	    	
    	Cursor parser = getContentResolver().query(APN_TABLE_URI, null, null, null, null);
    	parser.moveToLast();        	
    	while (parser.isBeforeFirst() == false){
            int index = parser.getColumnIndex("name");
            String n = parser.getString(index);   
            if (n.equals(name)){
            	existing = true;   
            	Toast.makeText(getApplicationContext(), "APN already configured.",Toast.LENGTH_SHORT).show();
            	break;
            }            
            parser.moveToPrevious();
    	}    	
    	
        //if the entry doesn't already exist, insert it into the APN table    	
    	if (!existing){    		

    		   //Initialize the Content Resolver and Content Provider
    		   ContentResolver resolver = this.getContentResolver();
    	       ContentValues values = new ContentValues();
    	       
    	       //Capture all the existing field values excluding name
    	       Cursor apu = getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
   	    	   apu.moveToFirst();   	    		
   	    	   int index;    	
	    	       
    	       index = apu.getColumnIndex("apn");
    	       String apn = apu.getString(index);    	        
    	       index = apu.getColumnIndex("type");
    	       String type = apu.getString(index);    	        
    	       index = apu.getColumnIndex("proxy");
    	       String proxy = apu.getString(index);    	        
    	       index = apu.getColumnIndex("port");
    	       String port = apu.getString(index);    	         
    	       index = apu.getColumnIndex("user");
    	       String user = apu.getString(index);    	        
    	       index = apu.getColumnIndex("password");
    	       String password = apu.getString(index);    	        
    	       index = apu.getColumnIndex("server");
    	       String server = apu.getString(index);    	        
    	       index = apu.getColumnIndex("mmsc");
    	       String mmsc = apu.getString(index);    	       
    	       index = apu.getColumnIndex("mmsproxy");
    	       String mmsproxy = apu.getString(index);    	        
    	       index = apu.getColumnIndex("mmsport");
    	       String mmsport = apu.getString(index);    	        
    	       index = apu.getColumnIndex("mcc");
    	       String mcc = apu.getString(index);    	        
    	       index = apu.getColumnIndex("mnc");
    	       String mnc = apu.getString(index);    	        
    	       index = apu.getColumnIndex("numeric");
    	       String numeric = apu.getString(index);
    	       
    	       //Assign them to the ContentValue object
    	       values.put("name", name); //the method parameter
    	       values.put("apn", apn);         	       
    	       values.put("type", type);
    	       values.put("proxy", proxy);
    	       values.put("port", port);
    	       values.put("user", user);
    	       values.put("password", password);
    	       values.put("server", server);
    	       values.put("mmsc", mmsc);
    	       values.put("mmsproxy", mmsproxy);
    	       values.put("mmsport", mmsport);    	       
    	       values.put("mcc", mcc);
		       values.put("mnc", mnc);
		       values.put("numeric", numeric);		       

    	       //Actual insertion into table
    	       Cursor c = null;
    	       try{
    	           Uri newRow = resolver.insert(APN_TABLE_URI, values);
    	           
    	           if(newRow != null){
    	               c = resolver.query(newRow, null, null, null, null);
    	                int idindex = c.getColumnIndex("_id");
    	                c.moveToFirst();
    	                id = c.getShort(idindex);    	                
    	           }
    	       }
    	       catch(SQLException e){}
    	       if(c !=null ) c.close();    		 
    	}

        return id;
    }
 
    //Takes the ID of the new record generated in InsertAPN and sets that particular record the default preferred APN configuration
    public boolean SetPreferredAPN(int id){
    	
    	//If the id is -1, that means the record was found in the APN table before insertion, thus, no action required
    	if (id == -1){
    		return false;
    	}
    	
    	Uri.parse("content://telephony/carriers");
    	final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    	    	
    	boolean res = false;
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();

        values.put("apn_id", id); 
        try{
            resolver.update(PREFERRED_APN_URI, values, null, null);
            Cursor c = resolver.query(PREFERRED_APN_URI, new String[]{"name", "apn"}, "_id="+id, null, null);
            if(c != null){
                res = true;
                c.close();
            }
        }
        catch (SQLException e){}
         return res;
    }
    
    //Dummy test network
    public class TESTNETWORK extends Thread{
    	Socket testsocket;    	
    	public void run(){    		
    		try {
    			InetAddress serverAddr = InetAddress.getByName("203.186.13.204");
    			testsocket = new Socket(serverAddr, 8001);
    		} catch (UnknownHostException e1) {
    			e1.printStackTrace();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
    	}
    }
    
    //Grabs the external ip address from ip2country.sourceforge.net
    public String getExternalIPAddress() {
    	String ip = null;
    	   try { 
    	        HttpClient httpclient = new DefaultHttpClient();
    	        HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON"); 
    	        HttpResponse response;    	 
    	        response = httpclient.execute(httpget);   	 
    	        HttpEntity entity = response.getEntity();
    	        entity.getContentLength();
    	        String str = EntityUtils.toString(entity);
    	        JSONObject json_data = new JSONObject(str);
    	        ip = json_data.getString("ip");
    	    } 
    	    catch (Exception e){
    	    	ip = "Ping failed.";
    	    }
    	 
    	  return ip;
    }
        
    public void makeNetwork(){
    	network = new Network(8080);  
    	network.start();
    }
  
    public void toggleWifi(boolean state){
    	WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);    	
    	wifiManager.setWifiEnabled(state);
    }
   
    public void toggleMobile(boolean state){    	
    	try{
    		final ConnectivityManager conman = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    		final Class conmanClass = Class.forName(conman.getClass().getName());   
    		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
    		iConnectivityManagerField.setAccessible(true);   
    		final Object iConnectivityManager = iConnectivityManagerField.get(conman);
    		final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
    		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled",Boolean.TYPE);
    		setMobileDataEnabledMethod.setAccessible(true);
    		
    		// (true) to enable 3G; (false) to disable it.   
    		setMobileDataEnabledMethod.invoke(iConnectivityManager, state);
    		 
    	}catch(Exception e){}    	
    }
    
    public void displayAlert(final boolean updateiptext, final String message, int duration, final String in, final String ex){
    	
    	runOnUiThread(new Runnable() {
		    public void run() {
		    	
		    	if (updateiptext == true){   		
		    		iptext.setText(" Internal IP Address: " + in + "\n" + " External IP Address: "+ ex);			    	
		    	}		    	
		    	Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();		    	
		    }
		});
    	try { Thread.sleep(duration);} 
    	catch (InterruptedException e) {e.printStackTrace();}
    	
    }
 
    //Thread responsible for attempting to establish a network connection before calling the network initialization thread, 3G is prioritized
    Thread createNetwork = new Thread(){
	    @Override
	    public void run() {
	    	boolean created = false;
	    	boolean enabledWifi = false;
	    	boolean enabledData = false;
	    	boolean disableWifi = false;
	    	
	    	String internal = "Not Available";
	    	String external = "Not Available";
	    	
			while(!created){				
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			    NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   
			    
			    if (mMobile.isConnected() == true){
				    	
			    	toggleWifi(false);
			    	makeNetwork();
			    	created = true;					    	
			    	//these lines only should execute when a internet connection is prepped for the external ip address ping    	      	
			    	WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
			    	internal = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
			    	external = getExternalIPAddress();			    	
			    	displayAlert(true, "3G Network Connection Established + APN Configured.", 1000, internal, external);
			        
			    }
			    //mobile's availability is based on whether or not the card is plugged in, NOT whether the actual data switch is on or not
			    //isAvailable() == true means the sim card is plugged into the phone
			    else if (mMobile.isAvailable() == true && mMobile.isConnected() == false && enabledData == false){
			    	toggleWifi(false);
			    	toggleMobile(true);
			    	enabledData = true;
			    	disableWifi = true;
			    	displayAlert(false, "3G connection is available. Enabling...", 1000, internal, external);		

			    	//Configure the APN network prior to network creation 
			    	runOnUiThread(new Runnable() {
					    public void run() {					    	
					    	int identity = InsertAPN(NEW_APN_NAME); 
					        SetPreferredAPN(identity);					   		    	
					    }
					});			    	
			    }
			    
			    else if (mWifi.isConnected() == true){
			    	toggleMobile(false);
			    	makeNetwork();
			    	created = true;
			    	
			    	//these lines only should execute when a internet connection is prepped for the ipecho ping    	      	
			    	WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
			    	internal = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
			    	external = getExternalIPAddress();
			    	
			    	displayAlert(true, "Wifi Network Connection Established.", 1000, internal, external);			    	
			    }
			    
			    //wifi's availability is based on whether the actual wifi switch is on or not
			    //isAvailable() == false means the wifi switch is in the off position, Wifi capability is assumed to be available by default.
			    else if (mWifi.isAvailable() == false && mWifi.isConnected() == false && enabledWifi == false && disableWifi == false){
			    	toggleMobile(false);
			    	toggleWifi(true);
			    	enabledWifi = true;
			    	displayAlert(false, "No SIM card detected. Enabling Wifi...", 1000, internal, external);
			    }			    
		    	//if everything is unavailable you're just waiting for the connection to come up
			    else {		    	
			    	displayAlert(false, "Preparing connection. Confirming in 15 seconds...", 15000, internal, external);			    	
			    }
			}
	    }
	};
	
	//Thread responsible for creating a new network object (serversocket + socket)
    public class Network extends Thread{
    	ServerSocket serversocket;    	
    	
    	public Network (int port) {
    		try {
    			serversocket = new ServerSocket(port);    
    		} catch (IOException e) {} 
    	}
    	
    	public void run() {
			try {
	    		while (true) {
	    				Socket clientsocket = serversocket.accept();
	    				Client client = new Client(clientsocket, vt);
	    				client.start();
	    		}
    		} catch (IOException e) {}
    	}
    }
    
    public class CardReader extends Thread {
    	
    	Reader reader;
    	ImageView image;
    	
    	public CardReader(Reader reader, ImageView image) {
    		this.reader = reader;
    		this.image = image;
    	}
    	
    	public void run() {
    		try {
    			boolean isfinished = false;  
    			
    			//Camera timeout variables
    			boolean startcountdown = false;
    			boolean getprevioustime = false;    			
    			long previoustime = 0;
    			long currenttime = 0;
    			
    			//Sleep timeout variables
    			boolean active = true; //one to determine if the app is active
    			boolean firstprev = false;
    			long dimprevtime = 0;
    			long dimcurrtime = 0;
    			
	    		while (true) {
	    			Thread.sleep(10);
	    			cardid = reader.GetCardID();
	    			
	    			//screen dim conditional
	    			if (active){
	    				if (!firstprev){
	    					Calendar pc = Calendar.getInstance();
	    					dimprevtime = pc.getTimeInMillis();
	    					firstprev = true;	    					
	    				}
	    				else{
	    					Calendar cc = Calendar.getInstance();
	    					dimcurrtime = cc.getTimeInMillis();
	    					sleepercounter = Math.abs(dimcurrtime - dimprevtime);
	    					if (sleepercounter >= SLEEP_DELAY_MILLI){
	    						runOnUiThread(new Runnable() {
	    		    			    public void run() {
	    		    			    	setBrightness(0.01f);
	    		    			    }
	    		    			});
	    												
	    						active = false;
	    						firstprev = false;
	    						dimprevtime = 0;
	    						dimcurrtime = 0;	    						
	    					}
	    				}
	    			}
	    			
	    			//Camera task timeout conditional
	    			if (startcountdown){
	    				//already finished taking picture, reset the timeout flags
	    				if (!mCameraPreview.isdetecting){
	    					startcountdown = false;
    		    			getprevioustime = false;
    		    			previoustime = 0;
    		    			currenttime = 0;
	    				}
	    				//Get the initial calendar time when you tap the card
	    				else if (!getprevioustime){
	    					Calendar previouscalendar = Calendar.getInstance();
	    					previoustime = previouscalendar.getTimeInMillis();
	    					getprevioustime = true;
	    				}
	    				//Constantly check if 10 seconds has passed	    				
	    				else{
	    					Calendar currentcalendar = Calendar.getInstance();
	    					currenttime = currentcalendar.getTimeInMillis();
	    					timeoutcounter = Math.abs(currenttime - previoustime);
	    					if (timeoutcounter >= TIMEOUT_DELAY_MILLI){
	    						//In the middle of taking a picture, reset the loop flags, takePicture will do the rest
	    						if (mCameraPreview.foundface == true){
		    						startcountdown = false;
		    		    			getprevioustime = false;
		    		    			previoustime = 0;
		    		    			currenttime = 0;	    							
	    						}
	    						//Essentially, nothing happened . Manually reset the isdetecting flag
	    						else if (mCameraPreview.foundface == false){
	    							mCameraPreview.mCamera.stopFaceDetection();
		    						startcountdown = false;
		    		    			getprevioustime = false;
		    		    			previoustime = 0;
		    		    			currenttime = 0;
		    		    			mCameraPreview.isdetecting = false;
		    		    			reader.Beep(1000);
	    						}
	    					}
	    				}
	    			}
	    			
	    			//Reveal camera conditional
	    			if (mCameraPreview.isdetecting == true)
	    			{
		    			runOnUiThread(new Runnable() {
		    			    public void run() {
		    			    	image.setVisibility(View.INVISIBLE);
		    			    	date.setVisibility(View.INVISIBLE);
		    			    	clock.setVisibility(View.INVISIBLE);
		    			    	iptext.setVisibility(View.INVISIBLE);
		    			    }
		    			});
	    			}
	    			
	    			//Hide camera conditional
	    			if (mCameraPreview.isdetecting == false && isfinished == true)
	    			{
	    				active = true; //start up the sleep timeout again
	    				isfinished = false;
	    				Thread.sleep(1000);
		    			runOnUiThread(new Runnable() {
		    			    public void run() {
		    			    	image.setVisibility(View.VISIBLE);
		    			    	date.setVisibility(View.VISIBLE);
		    			    	clock.setVisibility(View.VISIBLE);
		    			    	iptext.setVisibility(View.VISIBLE);
		    			    }
		    			});
	    			}

	    			//Card tap conditional
	    			if (cardid == null || mCameraPreview.isdetecting == true) continue;
	    			Log.d("facedetection", "Starting Facedetection ");
	    			isfinished = true;
	    				    			
	    			mCameraPreview.isdetecting = true;
	    			mCameraPreview.sendTransactionInfo(cardid, vt, reader);
	    	    	mCameraPreview.mCamera.startFaceDetection();
	    	    	startcountdown = true;
	    	    	
	    	    	//reset screen dim variables
	    	    	runOnUiThread(new Runnable() {
	    			    public void run() {	    			   
	    			    	setBrightness(1.0f);
	    			    }
	    			});	    	    	
	    	    	active = false;
					firstprev = false;
					dimprevtime = 0;
					dimcurrtime = 0;
	    	    	
	    		}
    		}
    		catch (Exception e) {
    			Log.d("facedetection", "Error " + e.getMessage());
    		}
    	}
    }
    
   @Override
   public void onDestroy() { 
       super.onDestroy();
       unregisterReceiver(dateReceiver);
   }
   
   @Override
   public void onStop(){
   	super.onStop();
   	
   }
   
   @Override
   protected void onResume() {
       super.onResume();
   }

   @Override
   protected void onPause() {
       super.onPause();
   }

}
