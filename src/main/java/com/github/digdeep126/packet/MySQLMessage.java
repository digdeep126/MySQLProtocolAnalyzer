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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

/**
 * 从协议数据包中按照要求读取各种信息(String和Integer) 
 * mysql协议包中只含有两种数据类型的数据：String 和 Integer，分别有6种：
 * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
 * @author mycat
 */
public class MySQLMessage {
    public static final long NULL_LENGTH = -1;
    private static final byte[] EMPTY_BYTES = new byte[0];

    private final byte[] data;	// 整个数据包的字节
    private final int length;	// 整个数据包的字节长度，包括数据包包头的4字节
    private int position;		// 遍历读取 data 时记录读取到的位置

    public MySQLMessage(byte[] data) {
        this.data = data;
        this.length = data.length;
        this.position = 0;
    }

    /**
     * 读取指定位置： index 的一个字节
     */
    public byte readIndex(int index) {
        return data[index];
    }

    /**
     * 等价于 readUB1() 读取1字节
     */
    public byte read() {
        return data[position++];
    }

    public int readUB2() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        return i;
    }

    public int readUB3() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        return i;
    }

    public long readUB4() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        return l;
    }

    public int readInt() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        i |= (b[position++] & 0xff) << 24;
        return i;
    }

    public byte[] readBytes() {
        if (position >= length) {
            return EMPTY_BYTES;
        }
        byte[] ab = new byte[length - position];
        System.arraycopy(data, position, ab, 0, ab.length);
        position = length;
        return ab;
    }

    /**
     * 读取 string-EOF 对应的 byte[]
     * @see http://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::RestOfPacketString
     * Protocol::RestOfPacketString
		If a string is the last component of a packet, 
		its length can be calculated from the overall packet length minus the current position.
		Implemented By string<EOF>
     */
    public byte[] readBytes(int length) {
        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, length);
        position += length;
        return ab;
    }

    /**
     * 读取 string-NUL 对应的字节	Protocol::NulTerminatedString
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     */
    public byte[] readBytesWithNull() {
        final byte[] b = this.data;
        if (position >= length) {
            return EMPTY_BYTES;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
        case -1:
            byte[] ab1 = new byte[length - position];
            System.arraycopy(b, position, ab1, 0, ab1.length);
            position = length;
            return ab1;
        case 0:
            position++;
            return EMPTY_BYTES;
        default:
            byte[] ab2 = new byte[offset - position];
            System.arraycopy(b, position, ab2, 0, ab2.length);
            position = offset + 1;
            return ab2;
        }
    }
    
    /**
     * 读取 lenenc字符串：length encoded string 对应的字节
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     * @see http://dev.mysql.com/doc/internals/en/string.html
     */
    public byte[] readBytesWithLength() {
        int length = (int) readLength(); // 先从string的第一个字节读取并计算出length
        if(length == NULL_LENGTH){
            return null;
        }
        if (length <= 0) {
            return EMPTY_BYTES;
        }

        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, ab.length);
        position += length;
        return ab;
    }

    /**
     * 读取 string-EOF 对应的 byte[]
     * @see http://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::RestOfPacketString
     * Protocol::RestOfPacketString
		If a string is the last component of a packet, 
		its length can be calculated from the overall packet length minus the current position.
		Implemented By string<EOF>
     */
    public String readString() {
        if (position >= length) {
            return null;
        }
        String s = new String(data, position, length - position);
        position = length;
        return s;
    }

    /**
     * 读取 string-EOF 对应的 byte[]
     * @see http://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::RestOfPacketString
     * Protocol::RestOfPacketString
		If a string is the last component of a packet, 
		its length can be calculated from the overall packet length minus the current position.
		Implemented By string<EOF>
     */
    public String readString(String charset) throws UnsupportedEncodingException {
        if (position >= length) {
            return null;
        }
        
        String s = new String(data, position, length - position, charset);
        position = length;
        return s;
    }

    /**
     * 读取 string-NUL	Protocol::NulTerminatedString
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     */
    public String readStringWithNull() {
        final byte[] b = this.data;
        if (position >= length) {
            return null;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        if (offset == -1) {
            String s = new String(b, position, length - position);
            position = length;
            return s;
        }
        if (offset > position) {
            String s = new String(b, position, offset - position);
            position = offset + 1;
            return s;
        } else {
            position++;
            return null;
        }
    }
    
    /**
     * 读取 string-NUL	Protocol::NulTerminatedString
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     */
    public String readStringWithNull(String charset) throws UnsupportedEncodingException {
        final byte[] b = this.data;
        if (position >= length) {
            return null;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
        case -1:
            String s1 = new String(b, position, length - position, charset);
            position = length;
            return s1;
        case 0:
            position++;
            return null;
        default:
            String s2 = new String(b, position, offset - position, charset);
            position = offset + 1;
            return s2;
        }
    }

    /**
     * 读取 lenenc字符串：length encoded string
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     * @see http://dev.mysql.com/doc/internals/en/string.html
     */
    public String readStringWithLength() {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length);
        position += length;
        return s;
    }

    /**
     * 读取 lenenc字符串：length encoded string
     * @see http://dev.mysql.com/doc/internals/en/describing-packets.html
     * @see http://dev.mysql.com/doc/internals/en/string.html
     */
    public String readStringWithLength(String charset) throws UnsupportedEncodingException {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length, charset);
        position += length;
        return s;
    }
    
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public long readLong() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        l |= (long) (b[position++] & 0xff) << 32;
        l |= (long) (b[position++] & 0xff) << 40;
        l |= (long) (b[position++] & 0xff) << 48;
        l |= (long) (b[position++] & 0xff) << 56;
        return l;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public java.sql.Time readTime() {
        skip(6);
        int hour = read();
        int minute = read();
        int second = read();
        Calendar cal = getLocalCalendar();
        cal.set(0, 0, 0, hour, minute, second);
        return new Time(cal.getTimeInMillis());
    }

    public java.util.Date readDate() {
        byte length = read();
        int year = readUB2();
        byte month = read();
        byte date = read();
        int hour = read();
        int minute = read();
        int second = read();
        if (length == 11) {
            long nanos = readUB4();
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            Timestamp time = new Timestamp(cal.getTimeInMillis());
            time.setNanos((int) nanos);
            return time;
        } else {
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            return new java.sql.Date(cal.getTimeInMillis());
        }
    }

    public BigDecimal readBigDecimal() {
        String src = readStringWithLength();
        return src == null ? null : new BigDecimal(src);
    }
    
    /**
     * 用于读取lenenc字符串：先从string的第一个字节读取并计算出字符串的length
     * @see http://dev.mysql.com/doc/internals/en/string.html
     * @see http://dev.mysql.com/doc/internals/en/integer.html#length-encoded-integer
     */
    public long readLength() {
        int length = data[position++] & 0xff;
        switch (length) {
        case 251:	// 应该不会出现此种情况？？？
            return NULL_LENGTH;
        case 252:	// 0xfc=252, 此时用2字节来保存字符串长度（需要1+2个字节）
            return readUB2();
        case 253:	// 0xfd=253, 此时用3字节来保存字符串长度（需要1+2个字节）
            return readUB3();
        case 254:	// 0xfe=254, 此时用8字节来保存字符串长度（需要1+8个字节）
            return readLong();
        default:
            return length;	// If the value is < 251, it is stored as a 1-byte integer. 
            				// 此时用1字节来保存字符串长度，就是第一个字节length本身的值（只需要1个字节，而不是1+1个字节）
        }
    }

    public String toString() {
        return new StringBuilder().append(Arrays.toString(data)).toString();
    }

    private static final ThreadLocal<Calendar> localCalendar = new ThreadLocal<Calendar>();

    private static final Calendar getLocalCalendar() {
        Calendar cal = localCalendar.get();
        if (cal == null) {
            cal = Calendar.getInstance();
            localCalendar.set(cal);
        }
        return cal;
    }
    
    public boolean hasRemaining() {
        return length > position;
    }
    public int length() {
        return length;
    }
    public int position() {
        return position;
    }
    public byte[] bytes() {
        return data;
    }
    public void skip(int i) {
        position += i;
    }
    public void position(int i) {
        this.position = i;
    }
}