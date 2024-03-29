package com.nuu.socket;

/**
 * 命令码      包体长度    协议序号  包体参数
 * 2Bytes	    2Bytes	   4Bytes   NByte
 */
public class PduBase {

    /****************************************************
     * basic unit of data type length
     */
    public static final int PDU_BODY_LENGTH_INDEX = 0; //标识包体数据长度的位置索引
    public static final int PDU_HEADER_LENGTH = 8;  //包头长度 （包头固定数据域长度总和，不包括动态数据域）

    /****************************************************
     * index 1. pos:[0-2) 参数长度
     * 包体长度
     */
    public short length;

    /****************************************************
     * index 0. pos:[2-4) 命令码
     */
    public short commandId;


    /****************************************************
     * index 4. pos:[4-8)
     */
    public int seqId;


    /****************************************************
     * index 3. pos:[8-8+n) 参数
     */
    public byte[] body;

}


