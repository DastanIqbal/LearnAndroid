package com.flow.framework.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionDecryption {
    private static String strDefaultKey = "gmp";
    private Cipher decryptCipher;
    private Cipher encryptCipher;

    public static String byteArr2HexStr(byte[] arrB) {
        int iLen = arrB.length;
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++) {
            int intTmp = arrB[i];
            // 把负数转换为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            // 小于0F的数需要在前面补0
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    public static byte[] hexStr2ByteArr(String strIn) {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[(iLen / 2)];
        for (int i = 0; i < iLen; i += 2) {
            arrOut[i / 2] = (byte) Integer.parseInt(new String(arrB, i, 2), 16);
        }
        return arrOut;
    }

    public EncryptionDecryption() {
        this(strDefaultKey);
    }

    public EncryptionDecryption(String strKey) {
        this.encryptCipher = null;
        this.decryptCipher = null;
        Key key = getKey(strKey.getBytes());
        try {
            this.encryptCipher = Cipher.getInstance("DES");
            this.encryptCipher.init(1, key);
            this.decryptCipher = Cipher.getInstance("DES");
            this.decryptCipher.init(2, key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (InvalidKeyException e3) {
            e3.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] arrB) {
        byte[] bytes = null;
        try {
            bytes = this.encryptCipher.doFinal(arrB);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e2) {
            e2.printStackTrace();
        }
        return bytes;
    }

    public String encrypt(String strIn) {
        return byteArr2HexStr(encrypt(strIn.getBytes()));
    }

    public byte[] decrypt(byte[] arrB) {
        byte[] bytes = null;
        try {
            bytes = this.decryptCipher.doFinal(arrB);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e2) {
            e2.printStackTrace();
        }
        return bytes;
    }

    public String decrypt(String strIn) {
        try {
            return new String(decrypt(hexStr2ByteArr(strIn)));
        } catch (Exception e) {
            return "";
        }
    }

    private Key getKey(byte[] arrBTmp) {
        byte[] arrB = new byte[8];
        int i = 0;
        while (i < arrBTmp.length && i < arrB.length) {
            arrB[i] = arrBTmp[i];
            i++;
        }
        return new SecretKeySpec(arrB, "DES");
    }
}
