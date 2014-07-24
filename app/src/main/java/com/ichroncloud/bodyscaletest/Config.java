package com.ichroncloud.bodyscaletest;

/**
 * Created by lxl on 14-3-11.
 */
public class Config {
    public static final String EXTRAS_DEVICE_MAC = "deviceMac";
    public static final String EXTRAS_DEVICE_CODE_INDEX = "deviceCodeIndex";
    public static final String EXTRAS_DEVICE_COLOR_INDEX = "deviceColorIndex";
    public static final String EXTRAS_DEVICE_CODE_NAME = "deviceCodeName";
    public static final String EXTRAS_DEVICE_COLOR_NAME = "deviceColorName";
    public static final String EXTRAS_PRINT_IP = "ip_address";
    public static final String EXTRAS_RESULT_DEVICE_CODE = "resultDeviceCode";
    public static final String EXTRAS_DEVICE_STATUS_NAME = "deviceStatus";
    public static final String EXTRAS_DEVICE_STATUS_CODE = "deviceStatusCode";


    //            public static final String BASE_URL = "http://192.168.5.105:8080/inf/";
    public static final String BASE_URL = "http://api.ichronocloud.com/";

    public static final String RESULT_SUCCESS = "1";

    public static final String ORDER_CODE = "A5";//查询命令

    public static final String ORDER_SEND_DEVICE_CODE = "A1A5";//写入设备编码协议字符

    public static final String DEVICE_NAME_CHRONOCLOUD = "ChronoCloud";

    public static final String DEVICE_NAME_RYFIT = "RyFit";

    public static final String CONVENTIOS_STR = "894D94361A243577F0A497C4EAB6462A178900022D1D95B2EAE04";

    public static final String ANDROID_DEVICE_TYPE = "1";//android设备号

    /**
     * 汇思科使用的双列标签
     */

//    public static final int PRINT_OFFSET_X = 10;
//    public static final int PRINT_OFFSET_Y = 20;
//    public static final int QR_BITMAP_SIZE = 230;//生成二维码bitmap尺寸

    /**
     * 2014.5.6  19*19单列标签 森诺维
     */

    public static final int PRINT_OFFSET_X = 15;
    public static final int PRINT_OFFSET_Y = 10;
    public static final int QR_BITMAP_SIZE = 160;//生成二维码bitmap尺寸


    public static final String RESERVED_CODE = "00000";//预留码


    public static final String DEVICE_VERSION_CODE = "2";//终端版本号

    //    public static final String DEVICE_PROVIDE_CODE = "01";//外包供应商代码:汇思科
    public static final String DEVICE_PROVIDE_CODE = "02";//外包供应商代码:森诺维


    public static final String DEVICE_STATUS_CODE = "0";//设备状态全新


}
