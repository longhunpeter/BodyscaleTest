package com.ichroncloud.bodyscaletest.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ichroncloud.bodyscaletest.Config;
import com.ichroncloud.bodyscaletest.R;
import com.ichroncloud.bodyscaletest.data.GetDeviceCodeData;
import com.ichroncloud.bodyscaletest.network.BodyScaleRestClient;
import com.ichroncloud.bodyscaletest.server.BluetoothLeService;
import com.ichroncloud.bodyscaletest.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by lxl on 14-4-16.
 */
public class BluetoothTestActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getCanonicalName();
    private TextView mTestStatus;
    private Button testBtn;
    private Button backBtn;

    private boolean mScanning;
    private boolean isConnect;
    private String getDeviceCode;
    private List mA8;
    private List mA1;
    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private MyCount myCount;
    private Handler mHandler;
    private Intent gattServiceIntent;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;

    private static final long SCAN_PERIOD = 5000;

    private static final int SCAN_BLE_SUCCESS = 0x0000;
    private static final int SCAN_BLE_FAIL = 0x0001;
    private static final int DEVICE_IS_DISCONNECT = 0x0002;
    private static final int DEVICE_IS_CONNECT = 0x0003;
    private static final int TEST_SUCCESS = 0x0004;
    private static final int TEST_FAIL = 0x0005;
    private static final int GET_DEVICE_CODE_SUCCESS = 0x0006;
    private static final int GET_DEVICE_CODE_FAIL = 0x0007;
    private static final int SEND_ORDER = 0x0008;


    private Handler deviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DEVICE_CODE_SUCCESS:
                    mTestStatus.setText(getResources().getString(R.string.get_device_code));
                    connectDevice();
                    break;
                case GET_DEVICE_CODE_FAIL:
                    mTestStatus.setText(getResources().getString(R.string.get_deviceCode_fail));
                    testBtn.setEnabled(true);
                    break;
                case SCAN_BLE_SUCCESS:
                    if (mDeviceList.size() > 0) {
                        mTestStatus.setText(getResources().getString(R.string.scan_ble_success) +
                                mDeviceList.get(0).getAddress());
                        getDeviceCode(Utils.changeMACStr(mDeviceList.get(0).getAddress()));
                        break;
                    }
                    testBtn.setEnabled(true);
                    mTestStatus.setText(getResources().getString(R.string.scan_ble_fail));

                    break;
                case DEVICE_IS_DISCONNECT:
                    isConnect = false;
                    testBtn.setEnabled(true);
                    mTestStatus.setText(getResources().getString(R.string.device_connect_fail));
                    break;
                case DEVICE_IS_CONNECT:
                    isConnect = true;
                    mTestStatus.setText(getResources().getString(R.string.device_connect_success));
                    break;
                case SEND_ORDER:
                    mTestStatus.setText(getResources().getString(R.string.send_order));
                    sendTestOrder();
                    break;
                case TEST_SUCCESS:
                    testBtn.setEnabled(true);
                    mTestStatus.setText(getResources().getString(R.string.test_ble_success));
                    Intent intent = new Intent(BluetoothTestActivity.this, PrintQRCodeActivity.class);
                    if (getDeviceCode != null) {
                        intent.putExtra(Config.EXTRAS_RESULT_DEVICE_CODE, getDeviceCode);
                    }
                    intent.putExtra(Config.EXTRAS_DEVICE_MAC,
                            Utils.changeMACStr(mDeviceList.get(0).getAddress()));
                    startActivity(intent);
                    break;
                case TEST_FAIL:
                    testBtn.setEnabled(true);
                    mTestStatus.setText(getResources().getString(R.string.test_ble_fail));
                    break;
                default:
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume!!!!!!!!!!!!!!");
        if (mA8 != null && mA8.size() > 0) {
            mA8.clear();
        }
        if (mA1 != null && mA1.size() > 0) {
            mA1.clear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_btn:
                mTestStatus.setText(getResources().getString(R.string.scan_ble_now));
                testBtn.setEnabled(false);
                if (mDeviceList.size() > 0) {
                    mDeviceList.clear();
                }
                if (isConnect) {
                    isConnect = false;
                    mBluetoothLeService.disconnect();
                }
//                getDeviceCode();
                scanDevice(true);
                break;
            case R.id.back_btn:
                finish();
                break;
            default:
                break;
        }
    }

    private void initData() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            makeToast(R.string.ble_not_supported);
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            makeToast(R.string.ble_not_supported);
            finish();
            return;
        }

        mHandler = new Handler();
//        mDeviceList = new ArrayList<BluetoothDevice>();
        mA8 = new ArrayList();
        mA1 = new ArrayList();
        isConnect = false;
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());

    }

    private void initView() {
        mTestStatus = (TextView) findViewById(R.id.test_bluetooth_status);
        testBtn = (Button) findViewById(R.id.test_btn);
        backBtn = (Button) findViewById(R.id.back_btn);
        testBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

    }

    /**
     * 发送测试命令
     */
    private void sendTestOrder() {
        mBluetoothLeService.WriteValue(Config.ORDER_SEND_DEVICE_CODE + "ffffff");
        myCount = new MyCount(10000, 1000);
        myCount.start();
    }

    /**
     * 连接蓝牙
     */
    private void connectDevice() {
        if (mDeviceList.size() > 0) {
            if (mBluetoothLeService == null) return;
            mBluetoothLeService.connect(mDeviceList.get(0).getAddress());
        }
    }

    /**
     * 是否扫描
     *
     * @param isEnable
     */
    private void scanDevice(final boolean isEnable) {
        if (isEnable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        deviceHandler.sendEmptyMessage(SCAN_BLE_SUCCESS);
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 从服务端获取设备编号
     */
    private void getDeviceCode(String macAddress) {
        Log.i(TAG, "macAddress is :" + macAddress);
        String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        BodyScaleRestClient.newInstance(this).executeGetDeviceCode(
                Config.ANDROID_DEVICE_TYPE,
                Utils.getIMEI(this),
                Utils.getVersionName(this),
                reqTime,
                macAddress,
                Utils.encryption(macAddress + Config.CONVENTIOS_STR),
                new Callback<GetDeviceCodeData>() {
                    @Override
                    public void success(GetDeviceCodeData getDeviceCodeData, Response response) {
                        if (getDeviceCodeData.result != null && getDeviceCodeData.result.equals(Config.RESULT_SUCCESS)) {
                            getDeviceCode = getDeviceCodeData.devCode;
                            deviceHandler.sendEmptyMessage(GET_DEVICE_CODE_SUCCESS);
                            return;
                        }
                        deviceHandler.sendEmptyMessage(GET_DEVICE_CODE_FAIL);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        makeToast(getResources().getString(R.string.network_error));
                        deviceHandler.sendEmptyMessage(GET_DEVICE_CODE_FAIL);
                    }
                }
        );

    }

    /**
     * device scan callback.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mDeviceList == null || device == null || mDeviceList.size() > 0) {
                                return;
                            }
                            String deviceName = device.getName();


                            if (deviceName != null && (deviceName.equals(Config.DEVICE_NAME_CHRONOCLOUD) ||
                                    deviceName.equals(Config.DEVICE_NAME_RYFIT))) {
                                mDeviceList.add(device);
                            }
                        }
                    });
                }
            };


    /**
     * Code to manage Service lifecycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.i(TAG, "mBluetoothLeService is okay");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    /**
     * 接收广播
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //关联成功

                Log.i(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接

                deviceHandler.sendEmptyMessage(DEVICE_IS_DISCONNECT);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //建立蓝牙服务
            {
                deviceHandler.sendEmptyMessage(DEVICE_IS_CONNECT);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.i(TAG, "data is : " + data);
                String dataArrays[] = data.split(" ");
                if (dataArrays.length > 0 && dataArrays.length <= 1) {
                    if (mA8.size() > 0) {
                        Log.i(TAG, "mA8 is have!!!!!!!!!!!!!!!!");
                        return;
                    }
                    if (dataArrays[0].equals("F8")) {
                        mA8.add(dataArrays[0]);
                        deviceHandler.sendEmptyMessage(SEND_ORDER);
                    }
                } else if (dataArrays.length > 2) {
                    if (dataArrays[2].equals("04") && mA1.size() <= 0) {
                        if (myCount != null) {
                            myCount.cancel();
                        }
                        mA1.add(dataArrays[2]);
                        deviceHandler.sendEmptyMessage(TEST_SUCCESS);
                    }
                }
            }
        }
    };

    /**
     * 倒计时
     */
    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            deviceHandler.sendEmptyMessage(TEST_FAIL);
        }
    }
}
