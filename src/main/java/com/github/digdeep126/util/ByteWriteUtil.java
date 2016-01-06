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
package com.github.digdeep126.util;

import java.nio.ByteBuffer;

/**
 * @author digdeep@126.com
 */
public class ByteWriteUtil {
	
	public static byte[] writeUB1(byte[] buffer, int offset, int i) {
		buffer[offset++] = (byte)(i & 0xff);
		return buffer;
    }

    public static byte[] writeUB2(byte[] buffer, int offset, int i) {
		buffer[offset++] = (byte)(i & 0xff);		// 取低8位
		buffer[offset++] = (byte)((i >>> 8) & 0xff);	// 取高8位
		return buffer;
    }
    
    public static byte[] writeUB3(byte[] buffer, int offset, int i) {
		buffer[offset++] = (byte)(i & 0xff);
		buffer[offset++] = (byte)((i >>> 8) & 0xff);
		buffer[offset++] = (byte)((i >>> 16) & 0xff);
		return buffer;
    }
    
    public static byte[] writeUB4(byte[] buffer, int offset, long i) {
		buffer[offset++] = (byte)(i & 0xff);
		buffer[offset++] = (byte)((i >>> 8) & 0xff);
		buffer[offset++] = (byte)((i >>> 16) & 0xff);
		buffer[offset++] = (byte)((i >>> 24) & 0xff);
		return buffer;
    }
    
    public static byte[] writeUB8(byte[] buffer, int offset, long i) {
    	writeUB4(buffer, offset, i & 0xffffffff);
    	offset += 4;
    	writeUB4(buffer, offset, (i >>> 16) & 0xffffffff);
		return buffer;
    }
    
    public static byte[] writeBytes(byte[] buffer, int offset, byte[] src) {
    	System.arraycopy(src, 0, buffer, offset, src.length);
    	offset += src.length;
		return buffer;
    }
    
    public static byte[] writeBytesWithNULL(byte[] buffer, int offset, byte[] src) {
    	System.arraycopy(src, 0, buffer, offset, src.length);
    	offset += src.length;
    	buffer[offset++] = 0x00;
		return buffer;
    }

    public static byte[] writeWithLength(byte[] buffer, int offset, byte[] src) {
        int length = src.length;
//        System.out.printf("writeWithLength: 0x:%x\n", length);
        if (length < 251) {
        	writeUB1(buffer, offset, length);
        	offset++;
        	System.arraycopy(src, 0, buffer, offset, src.length);
        } else if (length < 0x10000L) {
        	writeUB1(buffer, offset, 252);
        	offset++;
        	writeUB2(buffer, offset, length);
//            buffer.put((byte) 252);
//            writeUB2(buffer, length);
        } else if (length < 0x1000000L) {
        	writeUB1(buffer, offset, 253);
        	offset++;
        	writeUB3(buffer, offset, length);
//            buffer.put((byte) 253);
//            writeUB3(buffer, length);
        } else {
        	writeUB1(buffer, offset, 254);
        	offset++;
        	writeUB8(buffer, offset, length);
//            buffer.put((byte) 254);
//            writeLong(buffer, length);
        }
        return buffer;
    }

    public static final int getLength(byte[] src) {
        int length = src.length;
        if (length < 251) {
            return 1 + length;
        } else if (length < 0x10000L) {
            return 3 + length;
        } else if (length < 0x1000000L) {
            return 4 + length;
        } else {
            return 9 + length;
        }
    }
    
}