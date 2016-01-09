/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.github.digdeep126.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.digdeep126.util.ByteUtil;

/**
 * <pre>
	0700000200000002000000
    len    id  header aff last status  warn		
	070000 02  00     00   00  0200    0000

	Type	Name	Description
	int<1>	header	[00] or [fe] the OK packet header
	int<lenenc>	affected_rows	affected rows
	int<lenenc>	last_insert_id	last insert-id
	if capabilities & CLIENT_PROTOCOL_41 {
		int<2>	status_flags	Status Flags
		int<2>	warnings	number of warnings
	} elseif capabilities & CLIENT_TRANSACTIONS {
		int<2>	status_flags	Status Flags
	}
	if capabilities & CLIENT_SESSION_TRACK {
		string<lenenc>	info	human readable status information
		if status_flags & SERVER_SESSION_STATE_CHANGED {
			string<lenenc>	session_state_changes	session state info
		}
	} else {
		string<EOF>	info	human readable status information
	}
 * @see http://dev.mysql.com/doc/internals/en/packet-OK_Packet.html
 * </pre>
 */
public class OKPacket extends Packet{
	private static Logger logger = LoggerFactory.getLogger(OKPacket.class);
	
	public static final int OK_STATUS = 0x00;
	
	// ====payload====
	public byte header;			// int<1>		header	[00] or [fe] the OK packet header
	public int affectedRows;	// int<lenenc>	affected_rows	affected rows <lenenc == length encoded>
	public int lastInsertId;	// int<lenenc>	last_insert_id	last insert-id
	public int statusFlags;		// int<2>		status_flags	Status Flags
	public int warnNumber;		// int<2>		warnings	number of warnings
	
	public String info;			// 很少用
	public String sessionStateChanges;	// 很少用

 	public OKPacket(byte[] buff){
//      len    id  header aff last status  warn		
//		070000 02  00     00   00  0200    0000
 		this.packetLen = ByteUtil.readUB3(buff, 0);
		this.packetSequenceId = buff[3] & 0xff;
		
		header = buff[4];
		affectedRows = buff[5];
		lastInsertId = buff[6];
		statusFlags = ByteUtil.readUB2(buff, 7);	// 0x0002	auto-commit is enabled
		warnNumber = ByteUtil.readUB2(buff, 9);
		
		logger.debug("packetSequenceId:" + packetSequenceId);
		
//		okPacket:{"packetSequenceId":2,"lastInsertId":0,"affectedRows":0,"statusFlags":2,
//		"header":0,"packetLen":7,"warnNumber":0}
	}
}