package com.ichroncloud.bodyscaletest.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ichroncloud.bodyscaletest.Config;
import com.ichroncloud.bodyscaletest.R;
import com.ichroncloud.bodyscaletest.data.UploadData;
import com.ichroncloud.bodyscaletest.network.BodyScaleRestClient;
import com.ichroncloud.bodyscaletest.util.Utils;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnectionException;
import com.zebra.android.printer.ZebraPrinter;
import com.zebra.android.printer.ZebraPrinterFactory;
import com.zebra.android.printer.ZebraPrinterLanguageUnknownException;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by lxl on 14-3-11.
 */
public class PrintQRCodeActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getCanonicalName();
    private Button printBtn;
    private Button printBack;
    private TextView mTextView;
    private TextView mParamsStatus;
    private ProgressBar getQrCodeBar;

    //    private String addressStr;
    private String deviceMAC;
    private String mResultDeviceCode;
    private String getQRCodeStr = "";
    private String mCurrentIpAddress;
    private String mDeviceStatusName;
    private int mDeviceCodeIndex;
    private int mDeviceColorIndex;
    private int mDeviceStatusCode;

    private String mDeviceCodeName;
    private String mDeviceColorName;
    private Bitmap mQRBitmap;

    private ZebraPrinterConnection mZpc;

    private SharedPreferences mSharedPreferences;


    private static final int PRINTER_SUCCESS = 0;
    private static final int PRINTER_FAIL = 1;

    private static final int UPLOAD_DATA_SUCCESS = 3;
    private static final int UPLOAD_DATA_FAIL = 4;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PRINTER_SUCCESS:
                    makeToast(getResources().getString(R.string.print_toast_success));
                    break;
                case PRINTER_FAIL:
                    makeToast(getResources().getString(R.string.print_toast_fail));
                    break;
                case UPLOAD_DATA_SUCCESS:
                    getQrCodeBar.setVisibility(View.GONE);
                    mTextView.setText(getQRCodeStr);
                    try {
                        mQRBitmap = Utils.createTwoQRCode(getQRCodeStr
                                , Config.QR_BITMAP_SIZE, Config.QR_BITMAP_SIZE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UPLOAD_DATA_FAIL:
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_code_layout);
        init_data();
        init_view();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mZpc && mZpc.isConnected()) {
            try {
                mZpc.close();
            } catch (ZebraPrinterConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    private void init_view() {
//        mImageView = (ImageView) findViewById(R.id.test_image);
        printBtn = (Button) findViewById(R.id.print_code_btn);
        printBtn.setOnClickListener(this);
        printBack = (Button) findViewById(R.id.print_back);
        printBack.setOnClickListener(this);
        mTextView = (TextView) findViewById(R.id.result_text);
        mParamsStatus = (TextView) findViewById(R.id.display_params);
        if (TextUtils.isEmpty(mDeviceCodeName) ||
                TextUtils.isEmpty(mDeviceColorName)
                || TextUtils.isEmpty(mDeviceStatusName)) {
            mParamsStatus.setText(getResources().getString(R.string.get_params_failed));
            return;
        }
        mParamsStatus.setText(mDeviceCodeName + " " + mDeviceColorName + "  " +
                mDeviceStatusName + " 版本号："
                + Config.DEVICE_VERSION_CODE);
        getQrCodeBar = (ProgressBar) findViewById(R.id.get_qrCode_bar);
        upLoadQRCode();

    }

    private void init_data() {
        Intent intent = getIntent();
        deviceMAC = intent.getStringExtra(Config.EXTRAS_DEVICE_MAC);
        mResultDeviceCode = intent.getStringExtra(Config.EXTRAS_RESULT_DEVICE_CODE);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrintQRCodeActivity.this);
        mDeviceCodeIndex = mSharedPreferences.getInt(Config.EXTRAS_DEVICE_CODE_INDEX, -1);
        mDeviceColorIndex = mSharedPreferences.getInt(Config.EXTRAS_DEVICE_COLOR_INDEX, -1);
        mDeviceStatusCode = mSharedPreferences.getInt(Config.EXTRAS_DEVICE_STATUS_CODE, -1);
//        Log.i(TAG,"mDeviceCodeIndex is : " + mDeviceCodeIndex);
//        Log.i(TAG,"mDeviceColorIndex is : " + mDeviceColorIndex);
        mDeviceCodeName = mSharedPreferences.getString(Config.EXTRAS_DEVICE_CODE_NAME, "");
        mDeviceColorName = mSharedPreferences.getString(Config.EXTRAS_DEVICE_COLOR_NAME, "");
        mCurrentIpAddress = mSharedPreferences.getString(Config.EXTRAS_PRINT_IP, "");
        mDeviceStatusName = mSharedPreferences.getString(Config.EXTRAS_DEVICE_STATUS_NAME, "");

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.print_code_btn:
                if (null == mQRBitmap) {
                    makeToast(getResources().getString(R.string.print_no_bitmap));
                    break;
                }
                if (mDeviceCodeIndex < 0 || mDeviceColorIndex < 0 || mCurrentIpAddress.equals("")) {
                    makeToast(getResources().getString(R.string.print_params_error));
                    break;
                }


                printBitmap(mQRBitmap, mCurrentIpAddress, Config.PRINT_OFFSET_X,
                        Config.PRINT_OFFSET_Y);
                break;
            case R.id.print_back:
                finish();
                break;
            default:
                break;
        }
    }


    /**
     * @param bitmap
     * @param printOffsetX
     * @param printOffsetY
     * @param address
     */
    private void printBitmap(final Bitmap bitmap, final String address, final int printOffsetX,
                             final int printOffsetY) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mZpc = Utils.connectPrinter(address);
                try {
                    Looper.prepare();
                    mZpc.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(mZpc);
                    printer.getGraphicsUtil().printImage(bitmap, printOffsetX,
                            printOffsetY,
                            -1, -1, false);
                    mZpc.close();
                    mHandler.sendEmptyMessage(PRINTER_SUCCESS);
                } catch (ZebraPrinterConnectionException e) {
                    mHandler.sendEmptyMessage(PRINTER_FAIL);
                    e.printStackTrace();
                } catch (ZebraPrinterLanguageUnknownException e) {
                    mHandler.sendEmptyMessage(PRINTER_FAIL);
                    e.printStackTrace();
                } finally {
                    Looper.myLooper().quit();
                }
            }
        }).start();
    }

    /**
     * 提交二维码到服务器
     */
    private void upLoadQRCode() {
        Log.i(TAG,"deviceStatusCode is " + mDeviceStatusCode);
        String upLoadTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        BodyScaleRestClient.newInstance(this).executeUpload(
                Config.ANDROID_DEVICE_TYPE,
                Utils.getIMEI(this),
                Utils.getVersionName(this),
                upLoadTime,

                Config.RESERVED_CODE,
                mDeviceCodeIndex + "",
                Config.DEVICE_VERSION_CODE,
                mDeviceColorIndex + "",
                Config.DEVICE_PROVIDE_CODE,
                mDeviceStatusCode + "",
                deviceMAC,

                Utils.getIMEI(this),
                Utils.encryption(Config.RESERVED_CODE +
                        mDeviceCodeIndex +
                        Config.DEVICE_VERSION_CODE +
                        mDeviceColorIndex +
                        Config.DEVICE_PROVIDE_CODE +
                        mDeviceStatusCode +
                        deviceMAC +
                        Utils.getIMEI(this) +
                        Config.CONVENTIOS_STR),
                new Callback<UploadData>() {
                    @Override
                    public void success(UploadData uploadData, Response response) {
                        if (null != uploadData.result &&
                                uploadData.result.equals(Config.RESULT_SUCCESS)) {
                            getQRCodeStr = uploadData.dimensionalCode;
                            mHandler.sendEmptyMessage(UPLOAD_DATA_SUCCESS);
                            return;
                        }
                        makeToast(getResources().getString(R.string.upload_fail));

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        getQrCodeBar.setVisibility(View.GONE);
                        if (retrofitError.isNetworkError()) {
                            makeToast(getResources().getString(R.string.network_error));
                            return;
                        }
                        makeToast(getResources().getString(R.string.upload_fail));
                    }
                }
        );

    }
}
