package com.github.digdeep126.util;

public class MySQLIntegerUtil {
	/**
     * Big-Endian字节数组转uint32
     * @param bys
     * @param off
     * @param len
     * @return
     */
    public static long bigBys2Uint32(byte[] bys,int off,int len){
        long uint32 = 0;
        for(int i=0,end=len-1,c=end; i<=end; i++,c--){
            uint32 |= (0xff&bys[off+i])<<(8*c);
        }
        return uint32;     
    }
     
    /**
     * Little-Endian字节数组转uint32
     * @param bys
     * @param off
     * @param len
     * @return
     */
    public static int littleBys2Uint32(byte[] bys,int off,int len){
        int uint32 = 0;
        for(int i=len-1; i>=0; i--){          
            uint32 |= (0xff&bys[off+i])<<(8*i);
        }
        return uint32;
    }
}
