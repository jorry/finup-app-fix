package com.iqianjin.client.hotfix.robust;

import android.content.Context;
import android.util.Log;

import com.meituan.robust.Patch;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

/**
 * 补丁验证
 */
public class VerifyUtils {
    public static final int SHA_LENGTH = 64;

    public static String fileMd5(File paramFile) {
        if (!paramFile.isFile()) {
            return "";
        }
        byte[] arrayOfByte = new byte['က'];
            MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            FileInputStream inputStream = new FileInputStream(paramFile);
            for (; ; ) {
                int i = inputStream.read(arrayOfByte, 0, 1024);
                if (i == -1) {
                    break;
                }
                messageDigest.update(arrayOfByte, 0, i);
            }
            inputStream.close();
        } catch (Exception localException) {
            return "";
        }
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    private static String getDecryptMD5(byte[] bytes, Context context)
            throws Exception {
        Cipher localCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        localCipher.init(2, (RSAPublicKey) getPublicKey(context));
        byte[] arrayOfByte = localCipher.doFinal(bytes);
        if (bytes.length <= 0) {
            return "";
        }
        return new String(arrayOfByte);
    }

    private static PublicKey getPublicKey(Context context)
            throws Exception {
        DataInputStream localDataInputStream = new DataInputStream(context.getAssets().open("publickey"));
        byte[] arrayOfByte = new byte[localDataInputStream.available()];
        localDataInputStream.readFully(arrayOfByte);
        localDataInputStream.close();
        X509EncodedKeySpec localX509EncodedKeySpec = new X509EncodedKeySpec(arrayOfByte);
        return KeyFactory.getInstance("RSA").generatePublic(localX509EncodedKeySpec);
    }

    /**
     * 第一个版本没有添加RES加密
     * 创建补丁临时文件，并验证是否为源文件
     * @param patch
     * @param paramContext
     * @return
     */
    public static boolean verifyPatch(Patch patch, Context paramContext) {
        boolean bool = true;
        try {
            ByteArrayOutputStream sourceByteArrayOutPutStream;
            try {
                sourceByteArrayOutPutStream = new ByteArrayOutputStream(4096);
                byte[] arrys = new byte['က'];
                FileInputStream localFileInputStream = new FileInputStream(patch.getLocalPath());
                for (; ; ) {
                    int i = localFileInputStream.read(arrys);
                    if (i == -1) {
                        break;
                    }
                    sourceByteArrayOutPutStream.write(arrys, 0, i);
                }
                localFileInputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            byte[] sourceByte = sourceByteArrayOutPutStream.toByteArray();
            sourceByteArrayOutPutStream.flush();
            sourceByteArrayOutPutStream.close();
            byte[] md5 = Arrays.copyOfRange(sourceByte, -32 + sourceByte.length, sourceByte.length);
            byte[] temp_path = Arrays.copyOfRange(sourceByte, 0, -32 + sourceByte.length);
            FileOutputStream tempFileOutputStream = new FileOutputStream(patch.getTempPath());
            tempFileOutputStream.write(temp_path);
            tempFileOutputStream.flush();
            tempFileOutputStream.close();
            String md5Str = new String(md5);
            Log.d(PatchManipulateImp.TAG,"path md5 = "+md5Str);
            bool = fileMd5(new File(patch.getTempPath())).equals(new String(md5));
        } catch (Exception e) {
            Log.d(PatchManipulateImp.TAG,"verifyPatch. exception = "+e);
            e.printStackTrace();
        }
        return bool;
    }
}