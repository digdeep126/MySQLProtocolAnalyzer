package com.github.digdeep126;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.xml.bind.DatatypeConverter;

import com.alibaba.fastjson.JSON;
import com.github.digdeep126.packet.AuthPacket;
import com.github.digdeep126.packet.CommandPacket;
import com.github.digdeep126.packet.ERRPacket;
import com.github.digdeep126.packet.HandShakeResponse;
import com.github.digdeep126.packet.OKPacket;
import com.github.digdeep126.packet.ServerHandShake;
import com.github.digdeep126.packet.StatusFlags;
import com.github.digdeep126.util.ByteUtil;
import com.github.digdeep126.util.ByteWriteUtil;

@SuppressWarnings("restriction")
public class Test {
	
	public static void main(String[] args){
		InetSocketAddress serverAddr = new InetSocketAddress("192.168.1.3", 3306);
		
        try (Socket client = new Socket();){
        	client.connect(serverAddr, 1000*10);
        	InputStream in = client.getInputStream();

        	int readLen = 0;
            byte[] buff = new byte[1024*16];
            readLen = in.read(buff);
            System.out.println("readLen1:" + readLen);
            
    		ServerHandShake serverHandShake = new ServerHandShake(buff);
//    		System.out.println(JSON.toJSON(serverHandShake));
    		
    		AuthPacket authPacket = new AuthPacket(serverHandShake);
    		byte[] authPackageBytes = authPacket.getBytes();

    		OutputStream os = client.getOutputStream();
    		os.write(authPackageBytes, 0, authPackageBytes.length);
    		
    		readLen = 0;
    		buff = new byte[1024*16];
    		readLen = in.read(buff);
    		System.out.println("readLen2:" + readLen);
          
			int resultStatus = ByteUtil.readUB1(buff, 4);
			System.out.println("resultStatus:" + resultStatus);
			OKPacket okPacket = null;
			if(resultStatus == OKPacket.OK_STATUS){
				okPacket = new OKPacket(buff);
				System.out.println("okPacket:" + JSON.toJSON(okPacket));
				
				System.out.println("\n***************************");
				if(okPacket.statusFlags == StatusFlags.SERVER_STATUS_AUTOCOMMIT)
					System.out.println("login success, mysql server status: auto-commit is enabled");
				else
					System.out.println("login success, mysql server status: " + okPacket.statusFlags);
				System.out.println("***************************\n");
				
				// 01 00 00 00 01
				byte[] quitPacket = new byte[5];//[0x01, 0x00, 0x00, 0x00, 0x01];
				quitPacket[0] = 0x01;
				quitPacket[1] = 0x00;
				quitPacket[2] = 0x00;
				quitPacket[3] = 0x00;
				quitPacket[4] = 0x01;
				os.write(quitPacket, 0, quitPacket.length);
			}else if(resultStatus == ERRPacket.ERROR_STATUS){ // 0xff
				ERRPacket errPacket = new ERRPacket(buff);
				System.out.println("errPacket:" + JSON.toJSON(errPacket));
				System.out.println("\n***************************");
				System.out.println("login failed: " + errPacket.errorMessage);
				System.out.println("***************************\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
