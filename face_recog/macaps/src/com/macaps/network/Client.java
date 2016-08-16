package com.macaps.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import android.os.Environment;
import android.util.Log;


public class Client extends Thread {
	public static int CMD_LOGIN = 0xF0;
	private static String status = "FF000000000000000000";
	
	private static String PUBLIC_KEY = "!@#$%^&*()";
	public Vector<Transaction> vt;
	byte[] buffer = new byte[1500];
	
	private Socket socket;
	
	public Client(Socket socket, Vector<Transaction> vt) {
		this.socket = socket;
		this.vt = vt;
	}
	
	public void SendFile(File file) throws IOException {
		String s = "";
		byte[] buffer = new byte[(int)file.length()];
		Log.d("facedetection", "Sent File: " + buffer.length + " bytes");
		FileInputStream fis = new FileInputStream(file);
	    BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(buffer,0,buffer.length);
        OutputStream out = socket.getOutputStream();
        out.write(buffer,0,buffer.length);
        out.flush();
	}
	
	public void Send(Protocol protocol) throws IOException {
		String s = "";
		OutputStream out = socket.getOutputStream();
		
		int len = 0;
		len = protocol.GetBytes().length;
		
		for (int i=0; i<protocol.GetBytes().length; i++) s += String.format("%02X ", protocol.GetBytes()[i]);		
		Log.d("facedetection", "Sent: " + s + ", Length: " + len);		
		out.write(protocol.GetBytes());
		out.flush();
	}
	
	public Protocol Receive() throws Exception {
		String s = "";	
		InputStream in = socket.getInputStream();

		in.read(buffer, 0, 3);
		
		int length = 0;

		if (buffer[0] == 0x02 && buffer[1] == 0x41 && buffer[2] == 0x31){
			for (length = 3; length < 13; length++){
				in.read(buffer, length, 1);
			}
		}
		else{
			for (length = 3; length < buffer.length; length++) {
				in.read(buffer, length, 1);
				if (buffer[length] == 0x03) break;
			}
			length++;
		}
		
		// for debug
		for (int i=0; i < length; i++) s += String.format("%02X ", buffer[i]);
		Log.d("facedetection", "Received : " + s + ", Length: " + length);
		return new Protocol(buffer, length);
	}	

	public void UpdateDateTime(Calendar cal) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd.HHmmss");
		
		String datetime = sdf.format(cal.getTime());  

		ArrayList<String> envlist = new ArrayList<String>();
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			envlist.add(envName + "=" + env.get(envName));
		}
		Log.d("facedetection", "New Date Time: " + datetime);
		String[] envp = (String[]) envlist.toArray(new String[0]);
		String command;
		command = "date -s\""+datetime+"\"";
		try {
			Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getFileExtension(String filename) {
		String filenameArray[] = filename.split("\\.");
		if (filenameArray == null) return "";
	    String extension = filenameArray[filenameArray.length-1];	
	    return extension;
	}
	
	public void run() {
		try {
			String s = "";
			File file = null;
			for (int i=0; i< PUBLIC_KEY.length(); i++) s = s + String.format("%02X", PUBLIC_KEY.getBytes()[i]);
			Log.d("facedetection", "Connection Successful");
			Send(new Protocol(0xF0, 0x00, s));
			Log.d("facedetection", "Send S0 ");
			while (true) {
				Protocol p = Receive();
				switch (p.command) {
					case 0xF0:		
						Log.d("facedetection", "Command: 0xF0 - Login");
						Send(new Protocol(0xF1, 0x00, ""));
						break;
						
					case 0xA0:
						Log.d("facedetection", "Command: 0xA0 - Unknown");
						Send(new Protocol(0xA1, 0x00, ""));
						break;
						
					case 0xA1:
						Log.d("facedetection", "Command: 0xA1 - Update Datetime ");
						Calendar d = Calendar.getInstance();
						Calendar c = Calendar.getInstance();
						c.set(Calendar.YEAR, p.data[0] + 1900);
					    c.set(Calendar.MONTH, p.data[1] - 1);
					    c.set(Calendar.DAY_OF_MONTH, p.data[2]);
				        c.set(Calendar.HOUR_OF_DAY, p.data[3]);
				        c.set(Calendar.MINUTE, p.data[4]);
				        c.set(Calendar.SECOND, p.data[5]);
				        if (Math.abs(d.getTimeInMillis() - c.getTimeInMillis()) > 10000) UpdateDateTime(c);
				        Send(new Protocol(0xA1, 0x00, ""));
												
						File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM");
						File[] files = folder.listFiles();
						file = null;
						for (int i = 0; files != null && i < files.length; i++) {
							if (getFileExtension(files[i].getName()).equals("jpg")) {
								file = files[i];
								break;
							}
						}						
						
						//Send the photo and record
						if (file != null) Send(new Protocol(0xC3, 0x00, String.format("%08d%s", file.length(), file.getName())));
						if (vt.size() != 0) Send(new Protocol(0xC4, 0x00, String.format("%05d", vt.size()) + Client.status));						
						break;
						
					case 0xA2: //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
						Log.d("facedetection", "Command: 0xA2 - Write Door Database");
						Send(new Protocol(0xA2, p.segment, ""));
						break;
						
					case 0xA3: //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
						Log.d("facedetection", "Command: 0xA3 - Unknown");
						Send(new Protocol(0xA3, p.segment, ""));
						break;
						
					case 0xA4: 	
						Log.d("facedetection", "Command: 0xA4 - Write User Record");
						Send(new Protocol(0xA4, p.segment, ""));
						//Receive(p);					
						
						break;
						
					case 0xA5:
						Log.d("facedetection", "Command: 0xA5 - Write User Database");
						Send(new Protocol(0xA5, p.segment, ""));
						break;
						
					case 0xA6: //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
						Log.d("facedetection", "Command: 0xA6 - Write TimeZone Database");
						Send(new Protocol(0xA6, p.segment, ""));
						break;
						
					case 0xA7: //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
						Log.d("facedetection", "Command: 0xA7 - Write Holiday Database");
						Send(new Protocol(0xA7, p.segment, ""));
						break;
						
					case 0xA8: //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
						Log.d("facedetection", "Command: 0xA8 - Write Group Database");
						Send(new Protocol(0xA8, p.segment, ""));
						break;						
						
					case 0xC5:
						Log.d("facedetection", "Command: 0xC5 - Get Transaction Log ");
						int logsize = (vt.size() <= Protocol.MAX_GET_LOG_SIZE) ? vt.size() : Protocol.MAX_GET_LOG_SIZE;
						Send(new Protocol(0xC8, logsize, ""));
						for (int i=0; i< logsize; i++) {
							Send(new Protocol(0xC5, 0x00, vt.get(i).GetByte()));
							vt.remove(0);
						}
						break;
						
					case 0xC6:
						Log.d("facedetection", "Command: 0xC6 - Get Transaction Log OK");
						for (int i=0; i<p.segment && vt.size() > 0; i++) vt.remove(0);
						Send(new Protocol(0xC6, 0x00, ""));
						break;
						
					case 0xCA:
						Log.d("facedetection", "Command: 0xCA - Get Photo ");
						if (file != null){
							Send(new Protocol(0xCA, 0x00, ""));
							SendFile(file);
						}
						break;
						
					case 0xCB:
						Log.d("facedetection", "Command: 0xCB - Delete Photo ");
						Send(new Protocol(0xCB, 0x00, ""));
						if (file != null) file.delete();
						break;
				}
			}
			
		} catch (Exception e) {
			Log.d("facedetection", e.getMessage());
			e.printStackTrace();
		}
	}
}