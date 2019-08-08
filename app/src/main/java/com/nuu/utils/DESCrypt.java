package com.nuu.utils;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt and decrypt messages using DES 192 bit
 */
public final class DESCrypt {

    private static final String TAG = "TcpClient";

    public static final String DES_MODE = "DES/ECB/PKCS5Padding";   //"DES/ECB/NoPadding" ,"DES/ECB/PKCS5Padding"

    private byte[] keyCode;

    private static DESCrypt instance;

    public static DESCrypt instance() {
        if (instance == null) {
            instance = new DESCrypt();
        }
        return instance;
    }

    private DESCrypt() {
        keyCode = "hello world".getBytes();
    }

    public byte[] encrypt(byte[] byteContent) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, "DES");
            Cipher desCipher = Cipher.getInstance(DES_MODE);
            desCipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return desCipher.doFinal(byteContent);
        } catch (Exception e) {
            Log.e(TAG, "DES encrypt 加密错误:" + e.toString());
            return byteContent;
        }
    }

    public byte[] decrypt(byte[] encrypt) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, "DES");
            Cipher cipher = Cipher.getInstance(DES_MODE);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(encrypt);
        } catch (Exception e) {
            Log.e(TAG, "DES decrypt 解密错误:" + e.toString());
            return encrypt;
        }
    }


}
