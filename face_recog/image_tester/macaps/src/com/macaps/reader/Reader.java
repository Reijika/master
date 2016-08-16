package com.macaps.reader;

import android.util.Log;

public class Reader {
	
	Uart uart;
	Mifare mifare;
	
	
	public Reader(Uart uart)
	{
		this.uart = uart;
		mifare = new Mifare(uart);
	}
	
	public void Beep(int millisecond)
	{
		try{
			Log.d("Reader", "Beepping ..." + millisecond);
			mifare.Mif_BuzzerOn();
			Thread.sleep(millisecond);
			mifare.Mif_BuzzerOff();
			Thread.sleep(millisecond);
		}
		catch (Exception e){
			Log.d("Reader", "Err" + e.getMessage());
		}
	}
	

	public void Light(boolean ison)
	{
		try{
			boolean a = (ison) ? mifare.Mif_LedOn() : mifare.Mif_LedOff();
		}
		catch (Exception e){
			Log.d("Reader", "Err" + e.getMessage());
		}
	}

	public String GetCardID()
	{
		try{
			byte[] cardid = new byte[4];
			if (!mifare.Mif_Request()) throw new Exception("Error : Request");
			if (!mifare.Mif_Anticol(cardid)) throw new Exception("Error : Anti-collision");
			if (!mifare.Mif_Select(cardid)) throw new Exception("Error : Selection");
			if (!mifare.Mif_Halt()) throw new Exception("Error : Halt");
			Beep(10);
			return String.format("%02X%02X%02X%02X", cardid[0],cardid[1],cardid[2],cardid[3]);
		}
		catch (Exception e){
			return null;
		}
	}
}
