package com.github.digdeep126.packet;

import javax.xml.bind.DatatypeConverter;

/**
 * Initial Handshake Packet - protocol version 10
 *  1              [0a] protocol version
	string[NUL]    server version
	4              connection id
	string[8]      auth-plugin-data-part-1
	1              [00] filler
	2              capability flags (lower 2 bytes)
	  if more data in the packet:
	1              character set
	2              status flags
	2              capability flags (upper 2 bytes)
	  if capabilities & CLIENT_PLUGIN_AUTH {
	1              length of auth-plugin-data
	  } else {
	1              [00]
	  }
	string[10]     reserved (all [00])
	  if capabilities & CLIENT_SECURE_CONNECTION {
	string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
	  if capabilities & CLIENT_PLUGIN_AUTH {
	string[NUL]    auth-plugin name
	  }
	//==================================
	Protocol::HandshakeV9:
	Initial Handshake Packet - Protocol Version 9
	Payload
	1              [09] protocol_version
	string[NUL]    server_version
	4              connection_id
	string[NUL]    scramble
 * @author Administrator
 * @see http://dev.mysql.com/doc/internals/en/connection-phase-packets.html
 */
public class ServerHandShake extends Packet{
	//=========payload begin:============ 数据包中的数据
	public int	protocolVersion;	// 1字节：		mysql协议版本：v10, v9
	public String serverVersion;	// NUL结尾的字符串：5.6.27-log
	public long connectionId;		// 4字节：		2
	public byte[] authPluginPart1;	// 8字节：	    0x72385F5466272D4C
	public byte filler;				// 1字节：		[00]
	public int capbilityLower;		// 低2字节：		63487，0xf7ff
	public int charset;				// 1字节：		46 对应于: infomation_schema.collations 表的ID列的值
	public int statusFlags;			// 2字节：		 参见：http://dev.mysql.com/doc/internals/en/status-flags.html#packet-Protocol::StatusFlags		
	public int capbilityUpper;		// 高2字节：		32895，0x807f
	public int authPluginDataLen;	// 1字节：		21
	public byte[] reserved;			// 10字节：		[00]
	public byte[] authPluginPart2;	// 12字节：	    0x7837463A322C246C76302771
	public String authPluginName;	// NUL结尾的字符	mysql_native_password
	public int capbility;			// capbilityUpper << 16 + capbilityLower
	
    public ServerHandShake(byte[] bin) {
        MySQLMessage mm = new MySQLMessage(bin);
        packetLen = mm.readUB3();
        packetSequenceId = mm.read();
    	
        protocolVersion = mm.read();
        serverVersion = new String(mm.readBytesWithNull());
    	connectionId = mm.readUB4();
    	authPluginPart1 = mm.readBytes(8);
    	filler = mm.read();
    	capbilityLower = mm.readUB2();
    	
//		  if more data in the packet:
//		  1              character set
//		  2              status flags
//		  2              capability flags (upper 2 bytes)
    	charset = mm.read();
    	statusFlags = mm.readUB2();
    	capbilityUpper = mm.readUB2();
    	capbility = capbilityUpper << 16 + capbilityLower;
    	
//		  if capabilities & CLIENT_PLUGIN_AUTH {
//		  1              length of auth-plugin-data
//		    } else {
//		  1              [00]
//		    }
    	int lengthOfAuthPluginData = 0;
		if((capbility & CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode()) > 0)
			lengthOfAuthPluginData = mm.read();	// 0x15
		else
			mm.skip(1);
		authPluginDataLen = lengthOfAuthPluginData;
		
    	reserved = mm.readBytes(10);	// string[10]     reserved (all [00])
    	
//		if capabilities & CLIENT_SECURE_CONNECTION 
//		  string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
		if((capbility & CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode()) > 0){
			authPluginPart2 = mm.readBytes(12);	// 算出来长度应该是13，但是实际上应该是12
			mm.skip(1);
		}else{
			mm.skip(13);
		}
    	authPluginName = mm.readStringWithNull();
    	
    	System.out.println("******ServerHandShake.packetSequenceId:" + packetSequenceId);
    }
    
//{"charset":46,"packageSequenceId":0,"serverVersion":"5.6.27-log",
//"capbility":1077903360,"authPluginDataLen":21,"statusFlags":2,"packageLen":78,
//"authPluginPart2":[65,104,103,80,59,115,84,41,108,103,64,75],
//"authPluginPart1":[97,55,45,123,71,49,92,94],
//"authPluginName":"mysql_native_password",
//"filler":[0],"connectionId":18,"protocolVersion":10,
//"capbilityLower":63487,"capbilityUpper":32895}
    
	
}
