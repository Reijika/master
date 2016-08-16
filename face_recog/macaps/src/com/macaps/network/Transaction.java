package com.macaps.network;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.macaps.network.Protocol;

public class Transaction {
	public long cardid;
	public long password;
	public int accesstype;
	public Calendar datetime;

	
	public Transaction(Calendar datetime, long cardid, long password, int accesstype) {
		this.datetime = datetime;
		this.cardid = cardid;
		this.password = password;
		this.accesstype = accesstype;
	}
	
	public Transaction(long cardid, long password, int accesstype) {
		this.datetime = Calendar.getInstance();
		this.cardid = cardid;
		this.password = password;
		this.accesstype = accesstype;
	}
	
	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public byte[] GetByte() {
		
		byte[] buffer = new byte[52];

		int offset = 0;
		buffer[offset++] = '0';				// door status
		buffer[offset++] = '0';
		
		for (int i=0; i<6; i++)buffer[offset++] = (byte)((this.cardid >> ((5 - i) * 8)) & 0xFF);
		buffer[offset++] = (byte)0x00;	// password
		buffer[offset++] = (byte)0x00;
		for (int i=0; i<4; i++) buffer[offset++] = '0';	// resevered
		buffer[offset++] = '0';			// resevered
		buffer[offset++] = '0';
		buffer[offset++] = '1';			// door id
		buffer[offset++] = '1';
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.YEAR) - 1900) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.YEAR) - 1900) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.MONTH) + 1) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.MONTH) + 1) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.DAY_OF_MONTH) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.DAY_OF_MONTH) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.HOUR_OF_DAY) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.HOUR_OF_DAY) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.MINUTE) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.MINUTE) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.SECOND) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.SECOND) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(0);
		buffer[offset++] = Protocol.ConvertAscii(1);
		buffer[offset++] = 'F';
		buffer[offset++] = 'F';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		buffer[offset++] = '0';
		return buffer;
	}
	
	public String GetTransaction() {
		byte[] buffer = new byte[32];

		int offset = 0;
		buffer[offset++] = '0';				// door status
		buffer[offset++] = '0';
		
		for (int i=0; i<6; i++)buffer[offset++] = (byte)((this.cardid >> ((5 - i) * 8)) & 0xFF);
		buffer[offset++] = (byte)0x00;	// password
		buffer[offset++] = (byte)0x00;
		for (int i=0; i<4; i++) buffer[offset++] = '0';	// reserved
		buffer[offset++] = '0';			// reserved
		buffer[offset++] = '0';
		buffer[offset++] = '1';			// door id
		buffer[offset++] = '1';
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.YEAR) - 1900) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.YEAR) - 1900) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.MONTH) + 1) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii((datetime.get(Calendar.MONTH) + 1) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.DAY_OF_MONTH) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.DAY_OF_MONTH) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.HOUR_OF_DAY) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.HOUR_OF_DAY) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.MINUTE) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.MINUTE) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.SECOND) / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(datetime.get(Calendar.SECOND) % 0x10);
		buffer[offset++] = Protocol.ConvertAscii(0);
		buffer[offset++] = Protocol.ConvertAscii(1);
		return new String(buffer);
	}
}
