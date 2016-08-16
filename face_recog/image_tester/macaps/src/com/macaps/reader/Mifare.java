package com.macaps.reader;

public class Mifare {
	
	Uart uart;
	
	public Mifare(Uart uart)
	{
		this.uart = uart;
	}
	
	public boolean Mif_Request()
	{
		String s = uart.SendProtocol("810100");
		if (s == null || s.equals("")) return false;
		if (!s.substring(1, 3).equals("90")) return false;
		if (!s.substring(3, 5).equals("04")) return false;
		return true;
	}
	
	public boolean Mif_RequestAll()
	{
		String s = uart.SendProtocol("810101");
		if (s == null || s.equals("")) return false;
		if (!s.substring(1, 3).equals("90")) return false;
		if (!s.substring(3, 5).equals("04")) return false;
		return true;
	}
	
	public boolean Mif_Anticol(byte[] cardid)
	{
		String s = uart.SendProtocol("8200");
		byte checksum = 0;
		if (s == null || s.equals("")) return false;
		if (!s.substring(1, 3).equals("90")) return false;
		for (int i=0; i<4; i++) 
		{
			cardid[i] = (byte)Integer.parseInt(s.substring(i*2+3, i*2+5), 16);
			checksum ^= cardid[i];
		}
		if (checksum != (byte)Integer.parseInt(s.substring(11, 13), 16)) return false;
		return true;
	}

	public boolean Mif_Select(byte[] cardid)
	{
		String s = "8305";
		byte checksum = 0;
		for (int i=0; i<4; i++) 
		{
			checksum ^= cardid[i];
			s = s + String.format("%02X", cardid[i]);
		}
		s = s + String.format("%02X", checksum);
		s = uart.SendProtocol(s);
		if (!s.substring(1, 3).equals("90")) return false;
		return true;
	}

	public boolean Mif_Auth(byte key, byte sector)
	{
		String s = String.format("8%x02%02x%02x", (key == 2) ? 0x0C: 0x04, (key == 0) ? 0x00: 0x04, sector).toUpperCase();
		s = uart.SendProtocol(s);
		if (!s.substring(1, 3).equals("90")) return false;
		return true;	
	}
	
	public boolean Mif_Read(byte sector, byte block, byte[] data)
	{
		String s = uart.SendProtocol(String.format("8611%02x", sector * 4 + block));
		if (!s.substring(1, 3).equals("90")) return false;
		for (int i=0; i<16; i++) data[i] = (byte)Integer.parseInt(s.substring(i*2+3, i*2+5), 16);//Byte.parseByte(s.substring(i*2+2, i*2+3), 16);
		return true;
	}
	
	public boolean Mif_Write(byte sector, byte block, byte[] data)
	{
		String s = String.format("8701%02x", sector * 4 + block);
		for (int i=0; i<16; i++) s = s + String.format("%02X", data[i]);
		s = uart.SendProtocol(s);
		if (!s.substring(1, 3).equals("90")) return false;
		return true;
	}
	
	public boolean Mif_Halt()
	{
		String s = uart.SendProtocol("8500");
		if (!s.substring(1, 3).equals("90")) return false; 
		return true;
	}
	
	public boolean Mif_BuzzerOn()
	{
		String s = uart.SendProtocol("A000");
		if (!s.substring(1, 3).equals("90")) return false; 
		return true;
	}
	
	public boolean Mif_BuzzerOff()
	{
		String s = uart.SendProtocol("A100");
		if (!s.substring(1, 3).equals("90")) return false; 
		return true;
	}
	
	public boolean Mif_LedOn()
	{
		String s = uart.SendProtocol("A200");
		if (!s.substring(1, 3).equals("90")) return false; 
		return true;
	}
	
	public boolean Mif_LedOff()
	{
		String s = uart.SendProtocol("A300");
		if (!s.substring(1, 3).equals("90")) return false; 
		return true;
	}
	
}
