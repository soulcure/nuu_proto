package com.nuu.utils;

import android.util.Log;

import com.nuu.socket.PduUtil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt and decrypt messages using AES 192 bit
 */
public final class AESCrypt {

    private static final String TAG = "TcpClient";

    private static final String AES_KEY_STR = "Y39rL18flE6H9fLbbuM9cJYi";  //AES key

    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    //private static final String AES_MODE = "AES";  //the default cipher for AES is AES/ECB/PKCS5Padding

    private static AESCrypt instance;

    private byte[] keyCode;

    private AESCrypt() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            //此处解决mac，linux报错
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(AES_KEY_STR.getBytes());
            keyGen.init(192, random);
            SecretKey secretKey = keyGen.generateKey();
            keyCode = secretKey.getEncoded();

            Log.d(TAG, "AES keyCode:" + PduUtil.bytes2HexString(keyCode));
            Log.d(TAG, "AES IV:" + PduUtil.bytes2HexString(getIV()));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static AESCrypt instance() {
        if (instance == null) {
            instance = new AESCrypt();
        }
        return instance;
    }

    /**
     * 指定一个初始化向量 (Initialization vector，IV)，IV 必须是16位
     */
    private byte[] getIV() {
        byte iv[] = new byte[16];
        System.arraycopy(keyCode, 0, iv, 0, iv.length);
        return iv;
    }

    public void setKeyCode(byte[] keyCode) {
        this.keyCode = keyCode;
    }

    public byte[] getKeyCode() {
        return keyCode;
    }


    public byte[] encrypt(byte[] byteContent) {
        try {
            byte[] iv = getIV();
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, "AES");
            Cipher cipher = Cipher.getInstance(AES_MODE);// 创建密码器

            //指定一个初始化向量 (Initialization vector，IV)， IV 必须是16位
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

            return cipher.doFinal(byteContent);
        } catch (Exception e) {
            Log.e(TAG, "AES encrypt 加密错误:" + e.toString());
            return byteContent;
        }

    }

    public byte[] decrypt(byte[] encrypt) {
        try {
            byte[] iv = getIV();
            SecretKeySpec keySpec = new SecretKeySpec(keyCode, "AES");
            Cipher cipher = Cipher.getInstance(AES_MODE);// 创建密码器

            //指定一个初始化向量 (Initialization vector，IV)， IV 必须是16位
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return cipher.doFinal(encrypt);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "AES encrypt 解密错误:" + e.toString());
            return encrypt;
        }
    }


}
