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
import com.github.digdeep126.packet.FieldPacket;
import com.github.digdeep126.packet.OKPacket;
import com.github.digdeep126.packet.ServerHandShake;
import com.github.digdeep126.packet.StatusFlags;
import com.github.digdeep126.util.ByteUtil;
import com.github.digdeep126.util.ByteWriteUtil; 

@SuppressWarnings("restriction")
public class LoginTest {
	private static Logger logger = LoggerFactory.getLogger(LoginTest.class);
	
	public static void main(String[] args){
		// MySQL Server 地址
		InetSocketAddress serverAddr = new InetSocketAddress("192.168.1.3", 3306);
		
		logger.info("login mysqld begin...");
		
        try (Socket client = new Socket();){
        	client.connect(serverAddr, 1000*10);
        	InputStream in = client.getInputStream(); 

        	int readLen = 0;
            byte[] buff = new byte[1024*16];
            readLen = in.read(buff);
            logger.debug("readLen1:" + readLen);
            
    		ServerHandShake serverHandShake = new ServerHandShake(buff);
    		logger.debug(JSON.toJSONString(serverHandShake));
    		
    		AuthPacket authPacket = new AuthPacket(serverHandShake);
    		byte[] authPackageBytes = authPacket.getBytes();

    		OutputStream os = client.getOutputStream();
    		os.write(authPackageBytes, 0, authPackageBytes.length);
    		
    		readLen = 0;
    		buff = new byte[1024*16];
    		readLen = in.read(buff);
    		logger.debug("readLen2:" + readLen); 
          
			int resultStatus = ByteUtil.readUB1(buff, 4);
			logger.debug("resultStatus:" + resultStatus);
			OKPacket okPacket = null;
			if(resultStatus == OKPacket.OK_STATUS){
				okPacket = new OKPacket(buff);
				logger.debug("okPacket:" + JSON.toJSON(okPacket));
				
				logger.debug("***************************");
				if(okPacket.statusFlags == StatusFlags.SERVER_STATUS_AUTOCOMMIT)
					logger.debug("login success, mysql server status: auto-commit is enabled");
				else
					logger.debug("login success, mysql server status: " + okPacket.statusFlags);
				logger.debug("***************************\n");
				
				CommandPacket packet = new CommandPacket();
	    		packet.packetSequenceId = 0;

	    		packet.commandType = 0x03;
	    		packet.arg = "select 1".getBytes("utf8");	// select @@version_comment limit 1
	    		
	    		byte[] buffer = new byte[137];
	    		int size = 1 + packet.arg.length;
	    		int offset = 0;
	    		offset += 3;
	    		ByteWriteUtil.writeUB1(buffer, offset, packet.packetSequenceId);
	    		offset += 1;
	    		ByteWriteUtil.writeUB1(buffer, offset, packet.commandType);	// command
	    		offset += 1;
	    		System.arraycopy(packet.arg, 0, buffer, offset, packet.arg.length);
	    		offset += packet.arg.length;
	    		
	    		ByteWriteUtil.writeUB3(buffer, 0, offset-4);	// 头部的开始3字节表示payload长度：头部4字节不计算在内
	    		
	    		os.write(buffer, 0, offset);
	    		os.flush();
	    		
	            readLen = in.read(buff);
	            
	            logger.debug("readLen3:" + readLen);
	            resultStatus = ByteUtil.readUB1(buff, 4);
	            logger.debug("resultStatus2:" + resultStatus);
				
				if(resultStatus == ERRPacket.ERROR_STATUS){ // 执行失败
					 errorHandle(buff);
				}else{
					// 参见：http://dev.mysql.com/doc/internals/en/com-query-response.html
					if(resultStatus == 0){
						okPacket = new OKPacket(buff);
						logger.debug("okPacket:" + JSON.toJSON(okPacket));
					}else{
						// 参见：http://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::LengthEncodedInteger
						int columnNumber = 0;
						if(resultStatus < 251)
							columnNumber = resultStatus;
						FieldPacket field = new FieldPacket(buff);
						logger.debug("columnNumber:" + columnNumber);
						logger.debug("FieldPacket:" + JSON.toJSONString(field));
						logger.debug("catalog:" + new String(field.catalog));
						logger.debug("name:" + new String(field.name));
//						FieldPacket:{"catalog":"Fw==","charsetIndex":0,"db":"","decimals":0,"definition":"",
//							"flags":0,"length":0,
//							"name":"ZgAAAAExAAw/AAEAAAAIgQAAAAAFAAAD/gAAAgACAAAEATEFAAAF/gAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
//						"orgName":"","orgTable":"A2Q=","packetLen":1,"packetSequenceId":1,"table":"","type":0}

					}
					
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
