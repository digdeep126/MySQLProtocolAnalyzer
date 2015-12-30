package com.github.digdeep126.packet;

/**
 * <pre>
 * 14.1.3.2 ERR_Packet
	This packet signals that an error occurred. It contains a SQL state value if CLIENT_PROTOCOL_41 is enabled.
	
	Payload
	Type	Name	Description
	int<1>	header	[ff] header of the ERR packet
	int<2>	error_code	error-code
	if capabilities & CLIENT_PROTOCOL_41 {
		string[1]	sql_state_marker	# marker of the SQL State
		string[5]	sql_state	SQL State
	}
	string<EOF>	error_message	human readable error message
 * @see http://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html
 * @author digdeep@126.com
 * </pre>
 */
public class ERRPacket extends Packet{
	public static final int ERROR_STATUS = 0xff;
	
	//===payload===
	public int header;		// int<1>
	public int errorCode;	// int<2>
	
	public String sqlStateMarker;	// string[1]
	public String sqlState;			// string[5]
	public String errorMessage;
	
	public ERRPacket(byte[] data){
		MySQLMessage mm = new MySQLMessage(data);
        packetLen = mm.readUB3();
        packetSequenceId = mm.read();
        
        header = mm.read();
        errorCode = mm.readUB2();
        
        sqlStateMarker = new String(mm.readBytes(1));
        sqlState = new String(mm.readBytes(5));
        errorMessage = mm.readStringWithNull();
        System.out.println("******ERRPacket.packetSequenceId:" + packetSequenceId);
//        errPacket:{"packetSequenceId":2,
//        	"errorMessage":"Access denied for user 'root'@'localhost' (using password: YES)",
//        	"errorCode":1045,"header":-1,"sqlState":"28000","packetLen":77,"sqlStateMarker":"#"}

	}
	
}
