package com.github.digdeep126;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.digdeep126.packet.AuthPacket;
import com.github.digdeep126.packet.CommandPacket;
import com.github.digdeep126.packet.ERRPacket;
import com.github.digdeep126.packet.OKPacket;
import com.github.digdeep126.packet.ServerHandShake;
import com.github.digdeep126.packet.StatusFlags;
import com.github.digdeep126.util.ByteUtil;
import com.github.digdeep126.util.ByteWriteUtil; 

@SuppressWarnings("restriction")
public class LoginTest {
	private static Logger logger = LoggerFactory.getLogger(LoginTest.class);
	public static void main(String[] args){
		// MySQL地址
		InetSocketAddress serverAddr = new InetSocketAddress("192.168.1.3", 3306);
		logger.info("login begin...");
        try (Socket client = new Socket();){
        	client.connect(serverAddr, 1000*10);
        	InputStream in = client.getInputStream(); 

        	int readLen = 0;
            byte[] buff = new byte[1024*16];
            readLen = in.read(buff);
            System.out.println("readLen1:" + readLen);
            
    		ServerHandShake serverHandShake = new ServerHandShake(buff);
    		System.out.println(JSON.toJSONString(serverHandShake));
    		
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
				
				CommandPacket packet = new CommandPacket();
	    		packet.packetSequenceId = 0;

	    		packet.commandType = 0x03;
	    		packet.arg = "select 1".getBytes("utf8");	// select @@version_comment limit 1
	    		
	    		byte[] buffer = new byte[137];
	    		int size = 1 + packet.arg.length;
	    		System.out.println("size:::::::::" + size);	// 9
	    		int offset = 0;
	    		offset += 3;
	    		ByteWriteUtil.writeUB1(buffer, offset, packet.packetSequenceId);
	    		offset += 1;
	    		ByteWriteUtil.writeUB1(buffer, offset, packet.commandType);	// command
	    		offset += 1;
	    		System.arraycopy(packet.arg, 0, buffer, offset, packet.arg.length);
	    		offset += packet.arg.length;
	    		
	    		ByteWriteUtil.writeUB3(buffer, 0, offset-4);	// 头部的开始3字节表示payload长度：头部4字节不计算在内
	    		
	    		System.out.println("offset:" + offset);
	    		os.write(buffer, 0, offset);
	    		os.flush();
	    		System.out.println("os.flush()");
	    		
	            readLen = in.read(buff);		// 此处被阻塞了，无法读取到mysqld执行的结果
	            
	            System.out.println("readLen3:" + readLen);
	            resultStatus = ByteUtil.readUB1(buff, 4);
				System.out.println("resultStatus2:" + resultStatus);
				
				if(resultStatus == ERRPacket.ERROR_STATUS){ // 执行失败
					 errorHandle(buff);
				}else{
					okPacket = new OKPacket(buff);
					System.out.println("okPacket:" + JSON.toJSON(okPacket));
				}
			}else if(resultStatus == ERRPacket.ERROR_STATUS){ // 登录失败
				errorHandle(buff);
			}
		} catch (IOException e) {
			logger.warn(e.getMessage());
		}
	}
	
	private static void errorHandle(byte[] buff){
		ERRPacket errPacket = new ERRPacket(buff);
		System.out.println("errPacket:" + JSON.toJSON(errPacket));
		System.out.println("\n***************************");
		System.out.println("error: " + errPacket.errorMessage);
		System.out.println("***************************\n");
	}
}
