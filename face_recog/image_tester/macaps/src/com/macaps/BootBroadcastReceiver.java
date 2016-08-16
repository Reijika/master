package com.macaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootBroadcastReceiver extends BroadcastReceiver {

	 static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	@Override
	 public void onReceive(Context context, Intent intent) {
		  
		  if (intent.getAction().equals(ACTION)){
			 try {
				Thread.sleep(1000);
				Intent bootIntent=new Intent(context, MainActivity.class);
				bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				   context.startActivity(bootIntent);
				   
				   
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		  }
		 }

}
