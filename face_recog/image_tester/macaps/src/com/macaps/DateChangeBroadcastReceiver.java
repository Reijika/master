package com.macaps;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.TextView;

public class DateChangeBroadcastReceiver extends BroadcastReceiver {
	private Handler handler;
	private TextView newdate;	
	
	public DateChangeBroadcastReceiver(Handler handles, TextView d){
		this.handler = handles;
		this.newdate = d;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 String action = intent.getAction();
		 
		 if (action.equals(Intent.ACTION_DATE_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)){	
			 
			 Runnable datechange = new Runnable() {
					public void run() {
						Calendar datecal = Calendar.getInstance();
				        SimpleDateFormat df = new SimpleDateFormat("E" + "\n" + "MMM dd");
				        String formattedDate = df.format(datecal.getTime());
				        newdate.setText(formattedDate);
					}
			};		
			 
			handler.postDelayed(datechange, 500);
			 
		 }		
	}

}
