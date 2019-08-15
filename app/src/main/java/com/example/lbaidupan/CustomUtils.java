package com.example.lbaidupan;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import lombok.val;
import lombok.var;

public class CustomUtils {

    public static String PAN = "pan";
    public static String panPath = null;
    public static Map<String, String> md5Map = new HashMap<>();

    public static boolean haveFile(Context context, String subpath) {
        return haveFile(context, subpath, false);
    }

    public static boolean haveFile(Context context, String subpath, boolean isForce) {
        return haveFile(context, subpath, null, isForce);
    }

    public static boolean haveFile(Context context, String subpath, String md5) {
        return haveFile(context, subpath, md5, false);
    }

    public static boolean haveFile(Context context, String subpath, String md5, boolean isForce) {
        return file(context, subpath, md5, isForce) != null;
    }

    public static File file(Context context, String subpath) {
        return file(context, subpath, null, false);
    }

    public static File file(Context context, String subpath, boolean isForce) {
        return file(context, subpath, null, isForce);
    }

    public static File file(Context context, String subpath, String md5) {
        return file(context, subpath, md5, false);
    }

    public static File file(Context context, String subpath, String md5, boolean isForce) {
        val file = getFile(context, subpath);
        val localMd5 = getFileMD5(file, isForce);
        var result = !TextUtils.isEmpty(localMd5);

        if (md5 != null) {
            result &= md5.equals(localMd5);
        }

        return result ? file : null;
    }

    public static File getFile(Context context, String subpath) {

        if (panPath == null) {
            panPath = context.getExternalFilesDir(PAN).getAbsolutePath();
        }

        return new File(panPath + subpath);
    }

    public static String getFileMD5(Context context, String subpath, boolean isForce) {
        return getFileMD5(getFile(context, subpath), isForce);
    }

    public static String getFileMD5(Context context, String subpath) {
        return getFileMD5(getFile(context, subpath));
    }

    public static String getFileMD5(File file) {
        return getFileMD5(file, false);
    }

    public static String getFileMD5(File file, boolean isForce) {
        val MD5 = file.getAbsolutePath();
        var md5 = md5Map.get(MD5);

        if (!isForce) {
            return md5;
        }

        if (!file.exists()) {
            md5Map.remove(MD5);
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
            md5 = bytesToHexString(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        md5Map.put(MD5, md5);
        return md5;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
