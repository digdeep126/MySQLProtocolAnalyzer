package com.github.digdeep126.packet;

/**
 * If CLIENT_PROTOCOL_41 is enabled, the EOF packet contains a warning count and status flags.
 * 
 * Note
	In the MySQL client/server protocol, EOF and OK packets serve the same purpose, 
	to mark the end of a query execution result. Due to changes in MySQL 5.7 in the OK packet 
	(such as session state tracking), and to avoid repeating the changes in the EOF packet, 
	the EOF packet is deprecated as of MySQL 5.7.5.
	
   Caution
	The EOF packet may appear in places where a Protocol::LengthEncodedInteger may appear. 
	You must check whether the packet length is less than 9 to make sure that it is a EOF packet.
	
   Payload

	Type	Name	Description
	int<1>	header	[fe] EOF header
	if capabilities & CLIENT_PROTOCOL_41 {
		int<2>	warnings	number of warnings
		int<2>	status_flags	Status Flags
	}
 * @author digdeep@126.com
 * @see http://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html
 *
 */
public class EOFPacket extends Packet{
	public static final int EOF_STATUS = 0xfe;
	
	public int header;
	public int warnings;
	public int statusFlags;
	
	public EOFPacket(byte[] data){
		MySQLMessage mm = new MySQLMessage(data);
        packetLen = mm.readUB3();
        packetSequenceId = mm.read();
        
        header = mm.readUB2();
        statusFlags = mm.readUB2();
	}
}
