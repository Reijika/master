package com.macaps.network;

public class Protocol {
	public static int MAX_GET_LOG_SIZE = 50;
	
	public int command;
	public int segment;
	public byte[] data;
	
	public Protocol() {
		this.command = 0x00;
		this.segment = 0;
		this.data = new byte[1500];
	}
	
	public Protocol(int command, int segment, String data) {
		this.command = command;
		this.segment = segment;
		this.data = new byte[data.length()];
		char[] array = data.toCharArray();
		for (int i=0; i<array.length; i++) this.data[i] = (byte)array[i];
	}

	public Protocol(int command, int segment, byte[] data) {
		this.command = command;
		this.segment = segment;
		this.data = data;
	}
	
	public Protocol(byte[] buffer, int length) throws Exception {
		if (buffer[0] != 0x02) throw new Exception("Protocol Start byte Error Exception");
		if (buffer[length - 1] != 0x03) throw new Exception("Protocol End byte Error Exception");
		command = Protocol.ConvertNumeric(buffer[1]) * 0x10 + Protocol.ConvertNumeric(buffer[2]); 
		segment = buffer[3] * 100 + buffer[4]; 
		
		this.data = new byte[1500];
		for (int i=5; i< buffer.length; i++) data[i - 5] = buffer[i];		

	}
	
	public byte[] GetBytes() {
		int header = (command != 0xF0 && command != 0xC4) ? 6 : 4; 
		byte[] buffer = new byte[data.length + header];
		int offset = 0;
		buffer[offset++] = 0x02;
		buffer[offset++] = Protocol.ConvertAscii(command / 0x10);
		buffer[offset++] = Protocol.ConvertAscii(command % 0x10);//.getBytes()[1];
		if (command != 0xF0 && command != 0xC4 && command != 0xC5) buffer[offset++] = (byte) ((segment/100) + '0');
		if (command != 0xF0 && command != 0xC4 && command != 0xC5) buffer[offset++] = (byte) ((segment%100) + '0');
		if (buffer[1] == 'F') buffer[1] = 'S';
		for (int i=0; i<data.length; i++) buffer[offset++] = data[i];
		buffer[offset++] = 0x03;
		return buffer;
	}
	
	public static int ConvertNumeric(byte val) {
		if (val >= '0' && val <= '9') return val - '0';
		if (val >= 'A' && val <= 'F') return val - 'A' + 0x0A;
		if (val >= 'a' && val <= 'f') return val - 'a' + 0x0A;
		if (val == 'S') return 0x0F;
		return 0;
	}
	
	public static byte ConvertAscii(int val) {
		if (val >= 0x00 && val <= 0x09) return (byte)(val + '0');
		if (val >= 0x0A && val <= 0x0F) return (byte)(val + 'A' - 0x0A);
		return '\0';
	}
}
