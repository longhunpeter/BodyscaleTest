package com.ichroncloud.bodyscaletest.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ichroncloud.bodyscaletest.Config;
import com.ichroncloud.bodyscaletest.R;
import com.ichroncloud.bodyscaletest.data.DeviceSetData;
import com.ichroncloud.bodyscaletest.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getCanonicalName();
    private TextView mDeviceCode;
    private TextView mDeviceColor;
    private EditText mIpEditText;
    private Button mNextBtn;
    private RelativeLayout mDeviceCodeLayout;
    private RelativeLayout mDeviceColorLayout;

    private Spinner mSpinner;

    private int currentCodeIndex;
    private int currentColorIndex;
    private int deviceStatusCode;
    private String currentCodeName;
    private String currentColorName;
    private String currentIpAddress;
    private String statusName;
    private List<DeviceSetData> mListDeviceCode;
    private List<DeviceSetData> mListDeviceColor_0;
    private List<DeviceSetData> mListDeviceColor;
    private SharedPreferences mSharedPreferences;

    private int[] deviceStatusCodes;

    private ArrayAdapter mDeviceStatusAdapter;

    private static final int PARAM_DEVICE_CODE = 0x001;
    private static final int PARAM_DEVICE_COLOR = 0x002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mListDeviceCode = new ArrayList<DeviceSetData>();
        mListDeviceColor = new ArrayList<DeviceSetData>();
        mListDeviceColor_0 = new ArrayList<DeviceSetData>();
        deviceStatusCodes = getResources().getIntArray(R.array.device_status_code);
        mDeviceStatusAdapter = ArrayAdapter.createFromResource(this, R.array.device_status_name,
                android.R.layout.simple_spinner_item);
        mDeviceStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final String codeNameArrays[] = getResources().getStringArray(R.array.device_code_name);
        final int codeIndexArrays[] = getResources().getIntArray(R.array.device_code_index);

//        final String colorNameArrays_0[] = getResources().getStringArray(R.array.device_color_name_0);
//        final int colorIndexArrays_0[] = getResources().getIntArray(R.array.device_color_index_0);

        final String colorNameArrays[] = getResources().getStringArray(R.array.device_color_name);
        final int colorIndexArrays[] = getResources().getIntArray(R.array.device_color_index);
        if (codeNameArrays.length > 0 && codeIndexArrays.length > 0) {
            for (int i = 0; i < codeNameArrays.length; i++) {
                DeviceSetData deviceSetData = new DeviceSetData();
                deviceSetData.setName(codeNameArrays[i]);
                deviceSetData.setCode(codeIndexArrays[i]);
                mListDeviceCode.add(deviceSetData);
            }
        }

//        /**
//         * 经典时尚版对应颜色
//         */
//        if (colorNameArrays_0.length > 0 && colorIndexArrays_0.length > 0) {
//            for (int j = 0; j < colorNameArrays_0.length; j++) {
//                DeviceSetData deviceSetData = new DeviceSetData();
//                deviceSetData.setName(colorNameArrays_0[j]);
//                deviceSetData.setCode(colorIndexArrays_0[j]);
//                mListDeviceColor_0.add(deviceSetData);
//            }
//
//        }

        if (colorNameArrays.length > 0 && colorIndexArrays.length > 0) {
            for (int j = 0; j < colorNameArrays.length; j++) {
                DeviceSetData deviceSetData = new DeviceSetData();
                deviceSetData.setName(colorNameArrays[j]);
                deviceSetData.setCode(colorIndexArrays[j]);
                mListDeviceColor.add(deviceSetData);
            }

        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (mListDeviceCode.size() <= 0 || mListDeviceColor.size() <= 0) {
            Log.e(TAG, "mListDeviceCode or mListDeviceColor of size can't <=0;");
            return;
        }
        currentCodeIndex = mSharedPreferences.getInt(Config.EXTRAS_DEVICE_CODE_INDEX,
                mListDeviceCode.get(0).getCode());
        currentColorIndex = mSharedPreferences.getInt(Config.EXTRAS_DEVICE_COLOR_INDEX,
                mListDeviceColor.get(0).getCode());
        currentCodeName = mSharedPreferences.getString(Config.EXTRAS_DEVICE_CODE_NAME,
                mListDeviceCode.get(0).getName());

        currentColorName = mSharedPreferences.getString(Config.EXTRAS_DEVICE_COLOR_NAME,
                mListDeviceColor.get(0).getName());
        currentIpAddress = mSharedPreferences.getString(Config.EXTRAS_PRINT_IP, "");

    }

    private void initView() {
        mDeviceCode = (TextView) findViewById(R.id.display_device_code);
        mDeviceColor = (TextView) findViewById(R.id.display_device_color);
        mIpEditText = (EditText) findViewById(R.id.ip_address);
        mDeviceCode.setText(currentCodeName);
        mDeviceColor.setText(currentColorName);
        mIpEditText.setText(currentIpAddress);
        mDeviceCodeLayout = (RelativeLayout) findViewById(R.id.deviceCode_layout);
        mDeviceCodeLayout.setOnClickListener(this);
        mDeviceColorLayout = (RelativeLayout) findViewById(R.id.deviceColor_layout);
        mDeviceColorLayout.setOnClickListener(this);
        mNextBtn = (Button) findViewById(R.id.next_btn);
        mNextBtn.setOnClickListener(this);
        mSpinner = (Spinner) findViewById(R.id.device_status);
        mSpinner.setAdapter(mDeviceStatusAdapter);
        mSpinner.setOnItemSelectedListener(itemSelectedListener);

    }


    /**
     * 选择设备code
     */
    private void settingDeviceParams(final List<DeviceSetData> list, final int paramIndex) {
        LinearLayout linearLayoutMain = new LinearLayout(this);
        linearLayoutMain.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ListView listView = new ListView(this);
        listView.setFadingEdgeLength(0);
        MyAdapter myAdapter = new MyAdapter(list);
        listView.setAdapter(myAdapter);
        linearLayoutMain.addView(listView);

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(linearLayoutMain).create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (paramIndex == PARAM_DEVICE_CODE) {

                    currentCodeIndex = list.get(position).getCode();
                    currentCodeName = list.get(position).getName();
                    mDeviceCode.setText(currentCodeName);


                } else if (paramIndex == PARAM_DEVICE_COLOR) {
                    currentColorIndex = list.get(position).getCode();
                    currentColorName = list.get(position).getName();
                    mDeviceColor.setText(currentColorName);

                }
                dialog.cancel();
            }
        });

    }

    /**
     * 根据设备code得到设备code name.
     *
     * @param deviceCodeIndex
     * @param list
     * @return
     */
    private String getDeviceCodeName(int deviceCodeIndex, List<DeviceSetData> list) {
        if (list.size() <= 0) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            if (deviceCodeIndex == list.get(i).getCode()) {
                return list.get(i).getName();
            }
        }
        return null;
    }

    /**
     * 根据颜色code得到设备颜色 name.
     *
     * @param deviceColorIndex
     * @param list
     * @return
     */
    private String getDeviceColorName(int deviceColorIndex, List<DeviceSetData> list) {
        if (list.size() <= 0) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            if (deviceColorIndex == list.get(i).getCode()) {
                return list.get(i).getName();
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceCode_layout:
                settingDeviceParams(mListDeviceCode, PARAM_DEVICE_CODE);
                break;
            case R.id.deviceColor_layout:
                settingDeviceParams(mListDeviceColor, PARAM_DEVICE_COLOR);
                break;
            case R.id.next_btn:
                currentIpAddress = mIpEditText.getText().toString();
                if (TextUtils.isEmpty(currentIpAddress)) {
                    makeToast(getResources().getString(R.string.ip_toast));
                    break;
                } else if (!Utils.isIp(currentIpAddress)) {
                    makeToast("输入ip地址有误");
                    break;
                }
                Log.i(TAG, "");
                mSharedPreferences.edit().putString(Config.EXTRAS_PRINT_IP,
                        Utils.convertIp(currentIpAddress)).commit();

                mSharedPreferences.edit().putInt(Config.EXTRAS_DEVICE_CODE_INDEX,
                        currentCodeIndex).commit();
                mSharedPreferences.edit().putString(Config.EXTRAS_DEVICE_CODE_NAME,
                        currentCodeName).commit();

                mSharedPreferences.edit().putInt(Config.EXTRAS_DEVICE_COLOR_INDEX,
                        currentColorIndex).commit();
                mSharedPreferences.edit().putString(Config.EXTRAS_DEVICE_COLOR_NAME,
                        currentColorName).commit();

                mSharedPreferences.edit().putInt(Config.EXTRAS_DEVICE_STATUS_CODE,
                        deviceStatusCode).commit();
                mSharedPreferences.edit().putString(Config.EXTRAS_DEVICE_STATUS_NAME,
                        statusName).commit();
                startActivity(new Intent(MainActivity.this, BluetoothTestActivity.class));
                break;
            default:
                break;
        }
    }


    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ((TextView) parent.getChildAt(0)).setTextSize(20);
            ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            statusName = mDeviceStatusAdapter.getItem(position).toString();
            if (null == deviceStatusCodes) return;
            deviceStatusCode = deviceStatusCodes[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    public class MyAdapter extends BaseAdapter {
        private List<DeviceSetData> mList;
        private LayoutInflater mInflator;


        public MyAdapter(List<DeviceSetData> list) {
            this.mList = list;
            mInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.device_set_item, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_param_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.deviceName.setText(mList.get(position).getName());
            return view;
        }

        class ViewHolder {
            TextView deviceName;
        }
    }


}
