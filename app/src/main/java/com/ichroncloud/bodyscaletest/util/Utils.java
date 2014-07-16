package com.ichroncloud.bodyscaletest.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.ichroncloud.bodyscaletest.server.BluetoothLeService;
import com.zebra.android.comm.TcpPrinterConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lxl on 14-3-11.
 */
public class Utils {

    /**
     * 注册接收的事件
     *
     * @return
     */
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    /**
     * 生成二维码
     *
     * @param content
     * @param QRWidth  自定义宽度
     * @param QRHeight 自定义高度
     * @return
     * @throws Exception
     */
    public static Bitmap createTwoQRCode(String content, int QRWidth, int QRHeight) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, QRWidth, QRHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bm = Bitmap.createBitmap(width - 10, height - 10, Bitmap.Config.ARGB_8888);
        bm.setPixels(pixels, 0, width, 0, 0, width - 10, height - 10);
        return bm;
    }

    /**
     * 保存二维码
     *
     * @param bitName
     * @param mBitmap
     */
    public static void saveMyBitmap(String bitName, Bitmap mBitmap) {
        File f = new File("/sdcard/" + bitName + ".jpg");
        Log.i("peter","f--------------- " + f.getName());
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] mByte = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            mByte[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return mByte;
    }

    public static boolean setA1(String[] das) {
        // TODO Auto-generated method stub
        if (Integer.parseInt(das[1], 16) == 161) {
           return true;
        }
        return false;
    }

    /**
     *
     * @param hex
     * @return
     */
//    public static byte[] hex2Byte(String... hex) {
//        String digital = "0123456789ABCDEF";
//        byte[] bytes = new byte[hex.length];
//        int index = 0;
//        for (String string : hex) {
//            if (index > 1 && index < 4) {
//                string = Integer.toHexString(Integer.parseInt(string))
//                        .toUpperCase();
//            }
//            if (string.length() == 1) {
//                string = "0" + string;
//            }
//            char[] hex2char = string.toCharArray();
//            int temp;
//            // 其实和上面的函数是一样的 multiple 16 就是右移4位 这样就成了高4位了
//            // 然后和低四位相加， 相当于 位操作"|"
//            // 相加后的数字 进行 位 "&" 操作 防止负数的自动扩展. {0xff byte最大表示数}
//            temp = digital.indexOf(hex2char[0]) * 16;
//            temp += digital.indexOf(hex2char[1]);
//            bytes[index] = (byte) (temp & 0xff);
//            index++;
//        }
//        return bytes;
//    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    /**
     * 得到当前设备号
     *
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        String imei = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
        return imei;

    }

    /**
     * @param plain
     * @return 32位小写密文
     */
    public static String encryption(String plain) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plain.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }

    /**
     * MD5 加密
     */
    public static String getMD5Str(String str, String encoding) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes(encoding));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString().toUpperCase();
    }

    /**
     * 重构获取的MAC以便传入服务器
     *
     * @param getMAC
     * @return
     */
    public static String changeMACStr(String getMAC) {
        String[] temp = null;
        String changeStr = "";
        temp = getMAC.split(":");
        if (null != temp) {

            for (int i = 0; i < temp.length; i++) {
                changeStr += temp[i];
            }

        }
        return changeStr;
    }

    /**
     * 得到当前app版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }

    /**
     * 通过IP连接打印机
     *
     * @param address ip地址
     * @return
     */
    public static TcpPrinterConnection connectPrinter(String address) {
        return new TcpPrinterConnection(address, TcpPrinterConnection.DEFAULT_ZPL_TCP_PORT);
    }

    /**
     * 转换IP，去除0作为占位符;ex:192.168.005.1
     *
     * @return convertIpStr
     */
    public static String convertIp(String currentIp) {
        String ipArrays[] = currentIp.split("\\.");
        String convertIpStr = "";
        for (int i = 0; i < ipArrays.length; i++) {
            convertIpStr += Integer.parseInt(ipArrays[i]) + ".";
        }
        return convertIpStr.substring(0, convertIpStr.length() - 1);
    }

    /**
     * 判断是否为合法ip
     *
     * @param host
     * @return
     */
    public static boolean isIp(String host) {
        if (host.length() < 7 || host.length() > 15 || host.isEmpty()) {
            return false;
        }
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern p = Pattern.compile(rexp);
        Matcher m = p.matcher(host);
        return m.find();
    }


}
