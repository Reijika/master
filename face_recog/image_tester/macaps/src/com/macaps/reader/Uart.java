package com.macaps.reader;

import java.util.*;

import com.macaps.MainActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.util.Log;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;




public class Uart {
	protected static final String ACTION_USB_PERMISSION = "com.macaps.android.USB";
	protected static final int vid = 0x10c4;//0x1a86;//0x0557;
	byte[] buffer = new byte[64];
	
	UsbManager manager = null;
	UsbDevice device = null;
	UsbEndpoint epi = null;
	UsbEndpoint epo = null;
	UsbDeviceConnection connection = null;
	UsbInterface interfaces = null;

	Vector<Byte> vb = new Vector<Byte>();

	boolean receiving = false;
	private static HashMap<String, IBinder> sCache = new HashMap<String, IBinder>();
	
	public Uart(MainActivity m) {
		
		//Intent intent = m.getIntent();
		//String action = intent.getAction();
		//m.intent = m.getIntent();
        //m.action = m.intent.getAction();
		//UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		//Log.d("FTDI_USB", "Starting Uart ...");
		manager = (UsbManager) m.getSystemService(Context.USB_SERVICE);
		PendingIntent pi = PendingIntent.getBroadcast(m, 0, new Intent(ACTION_USB_PERMISSION), 0);
		Intent intent = new Intent();
		intent.setAction(ACTION_USB_PERMISSION);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		m.registerReceiver(receiver, filter);
		// Request permission
		for (UsbDevice d : manager.getDeviceList().values()) {
			if (vid == d.getVendorId()) {
				//Log.d("FTDI_USB", ">==< " + d.getDeviceName() + "");
				device = d;
				intent.putExtra(UsbManager.EXTRA_DEVICE, device);
				intent.putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true);
	
				final PackageManager pm = m.getPackageManager();
				try {
					ApplicationInfo aInfo = pm.getApplicationInfo(m.getPackageName(), 0);

					try {
						IBinder b = ServiceManager.getService(m.USB_SERVICE);
						//Log.d("FTDI_USB", "ServiceManager.getService(m.USB_SERVICE)");
						IUsbManager service = IUsbManager.Stub.asInterface(b);
						//Log.d("FTDI_USB", "IUsbManager.Stub.asInterface(b)");
						service.grantDevicePermission(device, aInfo.uid);
						Log.d("FTDI_USB", "service.grantDevicePermission(device, aInfo.uid)");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						Log.d("FTDI_USB", "Remote Exception:");
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d("FTDI_USB", "All Exception:");
						e.printStackTrace();
					}
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					Log.d("FTDI_USB", "NameNotFoundException:");
					e.printStackTrace();
				}
				//Log.d("FTDI_USB", "UsbManager");
				m.getApplicationContext().sendBroadcast(intent);
				while (!Connect(9600));
			}
		}
	}
	
	public final BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Log.d("FTDI_USB", "action = " + action);
			
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					//Log.d("FTDI_USB", "UsbManager.EXTRA_DEVICE 22222222222222222 ========" + intent.getParcelableExtra(UsbManager.EXTRA_DEVICE));
					//Log.d("FTDI_USB", "Have Permission: " + manager.hasPermission(device));
					if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Log.d("FTDI_USB","Permission not granted :(");
					}
					else
					{
						if (dev != null) {
							if (vid == dev.getVendorId()) {
								device = dev;
							}
						}
						else
						{
							Log.d("FTDI_USB" , "device not present!");
						}
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				synchronized (this) {
					// Close reader
					Log.d("FTDI_USB" , "device close");
				}
			}
		}
	};
	
	public synchronized boolean Connect(int baudrate) {
		try
		{
			//if (success == false) return false;
			connection = manager.openDevice(device);
	
			//Log.d("FTDI_USB", "Interface Count: " + device.getInterfaceCount());
			//Log.d("FTDI_USB", "Using "+String.format("%04X:%04X", mDevice.getVendorId(), mDevice.getProductId()));
			//Log.d("FTDI_USB", "Using "+ device.getVendorId() + " " + device.getProductId());
			
			//if (manager.hasPermission(device)) return false;
			//return true;
			if(!connection.claimInterface(device.getInterface(0), true)) return false;
			//Log.d("FTDI_USB111", "Using "+ device.getVendorId()+" "+ device.getProductId());
			
			//connection.controlTransfer(0x40, 0xa1, 0, 0, null, 0, 0);
			//connection.controlTransfer(0x40, 0x9a, 0x1312, 0xb202, null, 0, 0);
			
			//// Arduino USB serial converter setup
			//connection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
			//connection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
			
			// Silab CP210x
			//connection.controlTransfer(REQTYPE_HOST_TO_INTERFACE, request, value, 0x0 /* index */, buffer, length, USB_CTRL_SET_TIMEOUT_MILLIS); 
			connection.controlTransfer(0x41, 0x00, 0x0001, 0x0, null, 0, 100); 
			connection.controlTransfer(0x41, 0x19, 0x0000, 0x0, new byte[] {0x04, 0x00, 0x00, 0x0A, 0x00, 0x00}, 6, 100); 
			connection.controlTransfer(0x41, 0x13, 0x0000, 0x0, new byte[] {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, 16, 100); 
			byte[] buffer = new byte[4];
			for (int i = 0; i < buffer.length; i++) { 
				buffer[i] = (byte) ((baudrate >> (8 * i)) & 0xFF);
			} 
			connection.controlTransfer(0x41, 0x1E, 0x0000, 0x0, buffer, buffer.length, 100); 
			connection.controlTransfer(0x41, 0x03, 0x0800, 0x0, null, 0, 100); 
			connection.controlTransfer(0x41, 0x07, 0x0200, 0x0, null, 0, 100); 
			connection.controlTransfer(0x41, 0x07, 0x0101, 0x0, null, 0, 100);
			connection.controlTransfer(0x41, 0x05, 0x0000, 0x0, null, 0, 100); 
	
	
			// CH341
			//byte[] buffer = new byte[8];
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN , 0x5f, 0, 0, buffer, 8, 1000);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0xa1, 0, 0, null, 0, 1000);
			
			//long factor = 1532620800 / baudrate;
			//int divisor = 3;
			//while((factor > 0xfff0) && (divisor > 0)) {
			//	factor >>=3;
			//	divisor--;
			//}
			//factor = 0x10000-factor;
			//int a = (int)((factor & 0xff00) | divisor);
			//int b = (int)(factor & 0xff);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0x9a, 0x1312, a, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0x9a, 0x0f2c, b, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN, 0x95, 0x2518, 0, buffer, 8, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0x9a, 0x2518, 0x0050, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN , 0x95, 0x0706, 0, buffer, 8, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0xa1, 0x501f, 0xd90a, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0x9a, 0x1312, a, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0x9a, 0x0f2c, b, null, 0, 100);
			//connection.controlTransfer(UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT , 0xa4, 0x00 , 0, null, 0, 100);
			
			//// FTDI USB serial converter setup
			//connection.controlTransfer(0x40, 0, 0, 0, null, 0, 0);
			//connection.controlTransfer(0x40, 0, 1, 0, null, 0, 0);
			//connection.controlTransfer(0x40, 0, 2, 0, null, 0, 0);
			//connection.controlTransfer(0x40, 2, 0x0000, 0, null, 0, 0);
			//connection.controlTransfer(0x40, 3, 0x4138, 0, null, 0, 0);//baudrate 9600
			//connection.controlTransfer(0x40, 4, 0x0008, 0, null, 0, 0);//data bit 8, parity none, stop bit 1, tx off
			
			interfaces = device.getInterface(0);
			for(int i = 0; i < interfaces.getEndpointCount(); i++){
				if(interfaces.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					//Log.d("FTDI_USB", "getDirection: "+interfaces.getEndpoint(i).getDirection());
					if(interfaces.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) epo = interfaces.getEndpoint(i);
					else epi = interfaces.getEndpoint(i);
				}
			}
			//br.start();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	public synchronized void Send(byte[] b) {
		try {
			int cc = connection.bulkTransfer(epo, b, b.length, 100);
			//Log.d("FTDI_USB", "cc: "+cc);
		}
		catch (Exception ex)
		{
			
		}
	}

	public synchronized int Receive(byte[] b)
	{
		try {
			byte[] buf = new byte[64];
			int offset = 0;
			while (true)
			{
				int i = 0;
				int received = connection.bulkTransfer(epi, buf, 1, 0);
				while (received != i) b[offset++] = buf[i++]; 
				if (b[offset - 1] == 0x03) return offset;
			}
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	

	public synchronized String SendProtocol(String sendbuf)
	{
		//byte[] cmd = command.getBytes();
		//byte[] val = data.getBytes();
		//byte[] len = String.format("%02X", data.length()).getBytes();
		try
		{
			byte[] data = sendbuf.getBytes();
			byte[] buffer = new byte[sendbuf.length() + 2];
			String s = "";
	
			int offset = 0;
			buffer[offset++] = 0x02;
			//Log.d("FTDI_USB", "Protocol: "+buffer.toString());
			for (int i=0; i<data.length; i++) buffer[offset++] = data[i];
			buffer[offset++] = 0x03;
			for (int i=0; i<offset; i++) s = s +  String.format("%02X ", buffer[i]);
			//Log.d("FTDI_USB", "Send Protocol: " + s);
			Send(buffer);
			//Thread.sleep(200);
			buffer = new byte[64];
			offset = Receive(buffer);
			s = "";
			for (int i=0; i<offset; i++) s = s +  String.format("%02X ", buffer[i]);
			//Log.d("FTDI_USB", "Rece Protocol:" + s + ", Offset:" + offset);
			if (buffer[0] != 0x02 || buffer[offset - 1] != 0x03 || offset < 4) return null;
			String recbuf = "";
			for (int i=0; i<offset; i++) recbuf = recbuf +  String.format("%c", buffer[i]);
			return recbuf;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public synchronized void Close() {
		connection.close();
	}
}
