package com.nuu.socket;

import android.util.Log;


import com.nuu.config.AppConfig;
import com.nuu.nuuinfo.BuildConfig;
import com.nuu.util.HexUtil;
import com.nuu.utils.DESCrypt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public abstract class PduUtil {

    private static final String TAG = "TcpClient";

    public abstract void OnRec(PduBase pduBase);

    public abstract void OnCallback(PduBase pduBase);

    public int ParsePdu(ByteBuffer buffer) {
        buffer.order(ByteOrder.BIG_ENDIAN);
        Log.v(TAG, "ByteBuffer limit:" + buffer.limit());

        if (buffer.limit() >= PduBase.PDU_HEADER_LENGTH) {
            //has full header
            int totalLength = buffer.getShort(PduBase.PDU_BODY_LENGTH_INDEX);
            if (totalLength <= buffer.limit()) {
                //has a full pack.
                byte[] packByte = new byte[totalLength];
                buffer.get(packByte);
                PduBase pduBase = buildPdu(packByte);
                buffer.compact();//compact()方法只会清除已经读过的数据
                //ready to read.
                buffer.flip();  //准备从缓冲区中读取数据

                if (pduBase != null) {
                    OnRec(pduBase);
                }

                return totalLength;
            } else {
                Log.v(TAG, "socket未读到完整包，包头完整，包体未读完，继续读socket...");
                buffer.position(buffer.limit());
                buffer.limit(buffer.capacity());
                return -1;
            }

        } else {
            Log.v(TAG, "socket读取数据少于最低包头8字节，包头不完整，继续读socket...");
            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());
            return -1;
        }
    }


    private PduBase buildPdu(byte[] bytes) {
        PduBase units = new PduBase();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);//准备从缓冲区中读取数据
        buffer.flip();

        if (BuildConfig.DEBUG) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            Log.d(TAG, "tcp rec buffer:" + HexUtil.bytes2HexString(data));
            buffer.flip();
        }

        short length = buffer.getShort();
        short commandId = buffer.getShort();
        int seqId = buffer.getInt();

        units.length = length;
        units.commandId = commandId;
        units.seqId = seqId;

        Log.d(TAG, "tcp rec package Length:" + length);

        int packageLen = length - PduBase.PDU_HEADER_LENGTH;

        if (packageLen > 0) {
            if (AppConfig.isEncryption) {
                byte[] data = new byte[packageLen];
                buffer.get(data);

                Log.d(TAG, "tcp rec buffer decrypt before length:" + packageLen);
                Log.d(TAG, "tcp rec buffer decrypt before:" + HexUtil.bytes2HexString(data));
                units.body = DESCrypt.instance().decrypt(data);

                Log.d(TAG, "tcp rec buffer decrypt before length:" + units.body.length);
                Log.d(TAG, "tcp rec buffer decrypt after:" + HexUtil.bytes2HexString(units.body));


            } else {
                units.body = new byte[packageLen];
                buffer.get(units.body);
            }
        }

        return units;
    }


    public ByteBuffer serializePdu(PduBase req) {
        ByteBuffer byteBuffer;
        if (AppConfig.isEncryption) {
            if (req.body != null) {
                Log.d(TAG, "DESCrypt before length:" + req.body.length);
                Log.d(TAG, "tcp send buffer decrypt before:" + HexUtil.bytes2HexString(req.body));

                byte[] data = DESCrypt.instance().encrypt(req.body);

                short length = (short) data.length;
                Log.d(TAG, "DESCrypt after length:" + length);
                Log.d(TAG, "tcp send buffer decrypt after:" + HexUtil.bytes2HexString(data));

                short packageLen = (short) (PduBase.PDU_HEADER_LENGTH + length);

                byteBuffer = ByteBuffer.allocate(packageLen);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);

                byteBuffer.putShort(packageLen);
                byteBuffer.putShort(req.commandId);
                byteBuffer.putInt(req.seqId);
                byteBuffer.put(data);
            } else {
                byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);

                byteBuffer.putShort((short) PduBase.PDU_HEADER_LENGTH);
                byteBuffer.putShort(req.commandId);
                byteBuffer.putInt(req.seqId);
            }

        } else {
            short packageLen = (short) (PduBase.PDU_HEADER_LENGTH + req.length);

            byteBuffer = ByteBuffer.allocate(packageLen);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);

            byteBuffer.putShort(packageLen);
            byteBuffer.putShort(req.commandId);
            byteBuffer.putInt(req.seqId);
            if (req.body != null) {
                byteBuffer.put(req.body);
            }
        }
        return byteBuffer;

    }


    public static String bytes2HexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        int length = b.length;
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append("0x").append(hex.toUpperCase());
            if (i < length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }


    public static String byte2HexString(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return "0x" + hex.toUpperCase();
    }


}
