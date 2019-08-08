package com.nuu.utils;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt and decrypt messages using DES
 */
public final class DESCrypt {

    private static final String TAG = "TcpClient";

    private static final String Algorithm = "DESede";
    private static final String DES_MODE = "DESede/ECB/PKCS5Padding";   //"DESede/ECB/NoPadding" ,"DESede/ECB/PKCS5Padding"

    private byte[] keyCode;

    private static DESCrypt instance;

    public static DESCrypt instance() {
        if (instance == null) {
            instance = new DESCrypt();
        }
        return instance;
    }

    private DESCrypt() {
        keyCode = "123456781234567812345678".getBytes();
    }

    public byte[] encrypt(byte[] byteContent) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, Algorithm);
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
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, Algorithm);
            Cipher cipher = Cipher.getInstance(DES_MODE);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(encrypt);
        } catch (Exception e) {
            Log.e(TAG, "DES decrypt 解密错误:" + e.toString());
            return encrypt;
        }
    }


}
