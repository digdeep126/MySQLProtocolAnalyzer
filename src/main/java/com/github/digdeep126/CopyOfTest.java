//package com.github.digdeep126;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.util.Date;
//
//import javax.xml.bind.DatatypeConverter;
//
//import com.alibaba.fastjson.JSON;
//import com.github.digdeep126.protocol.ServerHandShake;
//
//public class CopyOfTest {
//	@SuppressWarnings("restriction")
//	public static void main(String[] args){
//		InetSocketAddress serverAddr = new InetSocketAddress("192.168.1.3", 3306);
//		ServerHandShake serverHandShake = new ServerHandShake();
//        try (Socket client = new Socket();){
//        	client.connect(serverAddr, 1000*10);
//        	InputStream in = client.getInputStream();
//        	
//        	int readLen = 0;
//            byte[] buff = new byte[1024*16];
//            readLen = in.read(buff);
//            System.out.println("readLen:" + readLen);
//            
//			String hex = DatatypeConverter.printHexBinary(buff);
//    		System.out.println(hex);
//    		
//    		byte[] bin = DatatypeConverter.parseHexBinary(hex);
//    		System.out.println(new String(bin));
//    		System.out.println("===================================");
//    		
//    		// 这里的length是指 payload的长度，没有包括前面4个字节：4E0000 00
//    		// 格式：3*byte,  1*byte
//    		// 表示：消息长度,   包序号
//    		int length = Util.littleBys2Uint32(buff, 0, 3);	// 4E0000
//    		serverHandShake.setPackageLen(length);
//    		System.out.println("payload length:" + length);	
//    		
//    		int sequenceId = Util.littleBys2Uint32(buff, 3, 1); //00
//    		serverHandShake.setPackageSequenceId(sequenceId);
//    		System.out.println("package sequenceId:" + sequenceId);
//    		
//    		// payload begin:
//    		System.out.println("=========payload begin:============");
//    		int protocalVersion = Util.littleBys2Uint32(buff, 4, 1);	// 0A
//    		serverHandShake.setProtocolVersion(protocalVersion);
//    		System.out.println("protocalVersion:" + protocalVersion);	// 0A
//    		
//    		byte[] serverVersion = new byte[11];
//    		System.arraycopy(buff, 5, serverVersion, 0, 11);
//    		String hexStr = DatatypeConverter.printHexBinary(serverVersion);
//    		byte[] bytes = DatatypeConverter.parseHexBinary(hexStr);
//    		serverHandShake.setServerVersion(new String(bytes));
//    		System.out.println("serverVersion:" + new String(bytes));
//    		
//    		int connectionId = Util.littleBys2Uint32(buff, 16, 4);
//    		serverHandShake.setConnectionId(connectionId);
//    		System.out.println("connectionId:" + connectionId);
//    		
//    		byte[] authPlugin = new byte[8];
//    		System.arraycopy(buff, 20, authPlugin, 0, 8);
//    		hexStr = DatatypeConverter.printHexBinary(authPlugin);
//    		serverHandShake.setAuthPluginPart1(authPlugin);
//    		System.out.println("authPlugin1:0x" + new String(hexStr));
//    		
//    		byte[] filler = new byte[1];
//    		System.arraycopy(buff, 28, filler, 0, 1);
//    		hexStr = DatatypeConverter.printHexBinary(filler);
//    		serverHandShake.setFiller(filler);
//    		System.out.println("filler:" + new String(hexStr));
//    		
//    		int lowerCapbility = Util.littleBys2Uint32(buff, 29, 2);	// 2E
//    		System.out.println("lowerCapbility:" + lowerCapbility);
//    		System.out.printf("lowerCapbility:0x%x\n", lowerCapbility);
//    		serverHandShake.setCapbilityLower(lowerCapbility);
////    		  if more data in the packet:
////    			  1              character set
////    			  2              status flags
////    			  2              capability flags (upper 2 bytes)
//    		int charset = Util.littleBys2Uint32(buff, 31, 1);	// 2E
//    		serverHandShake.setCharset(charset);
//    		System.out.println("character:" + charset);	// 46
//    		System.out.printf("character:0x%x\n", charset);
//    		
//    		int statusFlags = Util.littleBys2Uint32(buff, 32, 2);	// 0x0002	auto-commit is enabled
//    		serverHandShake.setStatusFlags(statusFlags);
//    		System.out.println("statusFlags:" + statusFlags);
//    		System.out.printf("statusFlags:0x%x\n", statusFlags);
//    		
////    		byte[] upperCap = new byte[2];
//    		int upperCapbility = Util.littleBys2Uint32(buff, 34, 2);	// 2E
////    		System.arraycopy(buff, 34, upperCap, 0, 2);
////    		hexStr = DatatypeConverter.printHexBinary(upperCap);
//    		serverHandShake.setCapbilityUpper(upperCapbility);
//    		System.out.println("upperCapbility:" + upperCapbility);
//    		System.out.printf("upperCapbility:0x%x\n", upperCapbility);
////    		System.out.println("upperCapbility:0x" + new String(hexStr));
//    		
//    		int capbility = upperCapbility << 16 + lowerCapbility;
//    		serverHandShake.setCapbility(capbility);
//    		System.out.println("capbility:" + capbility);
//    		System.out.printf("capbility:0x%x\n", capbility);
//    		
////    		  if capabilities & CLIENT_PLUGIN_AUTH {
////    			  1              length of auth-plugin-data
////    			    } else {
////    			  1              [00]
////    			    }
//    		int lengthOfAuthPluginData = 0;
//    		if((capbility & CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode()) > 0)
//    			lengthOfAuthPluginData = Util.littleBys2Uint32(buff, 36, 1);	// 0x15
//    		serverHandShake.setAuthPluginDataLen(lengthOfAuthPluginData);
//    		System.out.println("lengthOfAuthPluginData:" + lengthOfAuthPluginData);	// 21
//    		
////    		string[10]     reserved (all [00])
//    		long reservedZero = Util.littleBys2Uint32(buff, 37, 10);	// 0x00000000000000000000
//    		System.out.println("reservedZero:" + reservedZero);	// 21
//    		
////    		if capabilities & CLIENT_SECURE_CONNECTION 
////    			  string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
//    		
//    		if((capbility & CapabilityFlags.CLIENT_SECURE_CONNECTION.getCode()) > 0){
//    			byte[] authPlugin2 = new byte[13];		// 3459442A414B335B5574693300 salt 第二部分，每次不一样
////        		3D7A74552D225E36302E35006D
//        		System.arraycopy(buff, 47, authPlugin2, 0, 13);
//        		hexStr = DatatypeConverter.printHexBinary(authPlugin2);
//        		System.out.println("authPlugin1:0x" + DatatypeConverter.printHexBinary(authPlugin));
//        		System.out.println("authPlugin2:0x" + new String(hexStr));
//        		
//        		String authSalt = DatatypeConverter.printHexBinary(authPlugin) + hexStr;
//        		System.out.println("authSalt:0x" + authSalt);
//        		serverHandShake.setAuthPluginPart2(authPlugin2);
//    		}
//    		
//    		
////    		if capabilities & CLIENT_PLUGIN_AUTH 
////			  string[NUL]    auth-plugin name
//    		
//    		if((capbility & CapabilityFlags.CLIENT_PLUGIN_AUTH.getCode()) > 0){
//    			byte[] authPluginName = new byte[readLen - 47-13];
//        		System.arraycopy(buff, 47+13, authPluginName, 0, readLen - 47-13);
//        		hexStr = DatatypeConverter.printHexBinary(authPluginName);
//        		System.out.println("authPluginName:" + new String(hexStr));	//6D7973716C5F6E61746976655F70617373776F726400 
//        		bytes = DatatypeConverter.parseHexBinary(hexStr);
//        		System.out.println("authPluginName:" + new String(bytes));
//        		serverHandShake.setAuthPluginName(new String(bytes));
//    		}
//    		
////    		{"charset":46,"packageSequenceId":0,"serverVersion":"5.6.27-log\u0000",
////    		"capbility":1077903360,"authPluginDataLen":21,"statusFlags":2,"packageLen":78,
////    		"authPluginPart2":[65,104,103,80,59,115,84,41,108,103,64,75,0],
////    		"authPluginPart1":[97,55,45,123,71,49,92,94],
////    		"authPluginName":"mysql_native_password\u0000",
////    		"filler":[0],"connectionId":18,"protocolVersion":10,
////    		"capbilityLower":63487,"capbilityUpper":32895}
//
//    		System.out.println(JSON.toJSON(serverHandShake));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
////	Protocol::HandshakeV9:
////		Initial Handshake Packet - Protocol Version 9
////		Payload
////		1              [09] protocol_version
////		string[NUL]    server_version
////		4              connection_id
////		string[NUL]    scramble
//	
////		Fields
////		protocol_version (1) -- 0x09 protocol_version
////		server_version (string.NUL) -- human-readable server version
////		connection_id (4) -- connection id
////		auth_plugin_data (string.NUL) -- auth plugin data for Authentication::Old
//}
