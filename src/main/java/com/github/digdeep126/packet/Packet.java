package com.github.digdeep126.packet;

/**
 * MySQL的协议数据包数据分为 header 和 payload(有效负载) 两部分：
 * 
 * header：分为两部分：最开始3字节表示协议包长度；接着的1字节表示协议包系列号；协议包序列号每次交互从0开始，不断递增
 * 		       注意3字节协议包长度，不包含数据包的包头的4字节；
 * 
 * payload: 不同命令对应数据包payload部分各部相同
 * 
 * @author digdeep@126.com
 */
public class Packet {
	// 数据包的包头数据：3字节表示协议包长度，1字节表示协议包系列号。 注意3字节协议包长度，不包含数据包的包头的4字节
	public int packetLen;			// 3字节：		payload length
	public int packetSequenceId;	// 1字节：		协议包序列号每次交互从0开始，不断增长
	
	public static final int HEADER_SIZE = 4;	// 头部4字节
	public static final long MAX_PACKET_SIZE = 16 * 1024 * 1024;
	//=========payload begin:============ 数据包中的数据
	
}
