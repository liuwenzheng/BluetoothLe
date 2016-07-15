/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.le.iBeaconClass.iBeacon;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends Activity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();
    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final int REQUEST_ENABLE_BT = 1001;
    @Bind(R.id.tv_left)
    TextView tvLeft;
    @Bind(R.id.iv_rssi_near)
    ImageView ivRssiNear;
    @Bind(R.id.iv_rssi_nearer)
    ImageView ivRssiNearer;
    @Bind(R.id.iv_rssi_nearest)
    ImageView ivRssiNearest;
    @Bind(R.id.tv_rssi_status)
    TextView tvRssiStatus;
    @Bind(R.id.iv_rssi_status)
    ImageView ivRssiStatus;
    @Bind(R.id.tv_uuid)
    TextView tvUuid;
    @Bind(R.id.tv_major)
    TextView tvMajor;
    @Bind(R.id.tv_minor)
    TextView tvMinor;
    @Bind(R.id.tv_rssi)
    TextView tvRssi;
    @Bind(R.id.tv_mac)
    TextView tvMac;
    /**
     * 搜索BLE终端
     */
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isReset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent,
                    REQUEST_ENABLE_BT);
        }
        SPUtiles.getInstance(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
                    if (ibeacon == null) {
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String filter_uuid = SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_UUID, "");
                            String filter_minor = SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_MINOR, "");
                            String filter_major = SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_MAJOR, "");
                            if (!TextUtils.isEmpty(filter_uuid) &&
                                    !TextUtils.isEmpty(filter_minor) &&
                                    !TextUtils.isEmpty(filter_major)) {
                                if (!filter_uuid.equals(ibeacon.proximityUuid) ||
                                        !filter_minor.equals(ibeacon.minor + "") ||
                                        !filter_major.equals(ibeacon.major + "")) {
                                    return;
                                }
                            }
                            tvUuid.setText(ibeacon.proximityUuid);
                            tvMajor.setText(ibeacon.major + "");
                            tvMinor.setText(ibeacon.minor + "");
                            tvMac.setText(ibeacon.bluetoothAddress);
                            tvRssi.setText(ibeacon.rssi + "");
                            int nearest = SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEAREST, -40);
                            int nearer = SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEARER, -70);
                            int near = SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEAR, -100);
                            int duration = SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_LEAVE_DURATION, 50);
                            if (ibeacon.rssi > nearest && ibeacon.rssi < 0) {
                                isReset = true;
                                ivRssiStatus.setImageResource(R.drawable.nearest);
                                ivRssiNearest.setImageResource(R.drawable.checked);
                                ivRssiNearer.setImageResource(R.drawable.checked);
                                ivRssiNear.setImageResource(R.drawable.checked);
                            }
                            if (ibeacon.rssi > nearer && ibeacon.rssi < nearest) {
                                isReset = true;
                                ivRssiStatus.setImageResource(R.drawable.nearer);
                                ivRssiNearest.setImageResource(R.drawable.checked_no);
                                ivRssiNearer.setImageResource(R.drawable.checked);
                                ivRssiNear.setImageResource(R.drawable.checked);
                            }
                            if (ibeacon.rssi > near && ibeacon.rssi < nearer) {
                                isReset = true;
                                ivRssiStatus.setImageResource(R.drawable.near);
                                ivRssiNearest.setImageResource(R.drawable.checked_no);
                                ivRssiNearer.setImageResource(R.drawable.checked_no);
                                ivRssiNear.setImageResource(R.drawable.checked);
                            }
                            if (ibeacon.rssi == 0 || ibeacon.rssi < near) {
                                isReset = false;
                                mHandler.sendEmptyMessageDelayed(0, duration * 1000);
                            }
                        }
                    });
                }
            };
    private Handler mHandler = new Handler() {

        public void dispatchMessage(Message msg) {
            if (0 == msg.what && !isReset) {
                ivRssiStatus.setImageResource(R.drawable.away);
                ivRssiNearest.setImageResource(R.drawable.checked_no);
                ivRssiNearer.setImageResource(R.drawable.checked_no);
                ivRssiNear.setImageResource(R.drawable.checked_no);
            }
        }
    };

    @OnClick({R.id.tv_left, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_left:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.tv_right:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
        }
    }


}