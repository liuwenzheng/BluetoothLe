package com.example.bluetooth.le;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ParamSettingActivity extends Activity {

    @Bind(R.id.et_reserve)
    EditText etReserve;
    @Bind(R.id.et_uuid)
    EditText etUuid;
    @Bind(R.id.et_major)
    EditText etMajor;
    @Bind(R.id.et_minor)
    EditText etMinor;
    @Bind(R.id.et_tx_power)
    EditText etTxPower;
    @Bind(R.id.et_company_id)
    EditText etCompanyId;
    private ProgressDialog mDialog;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattCallback mGattCallback;
    private BluetoothGatt mBluetoothGatt;
    private static final int GATT_ERROR_TIMEOUT = 133;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private iBeaconClass.iBeacon mIBeacon;
    /**
     * Write, APP send command to wristbands using this characteristic
     */
    public static final UUID CHARACTERISTIC_UUID_WRITE = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
    /**
     * Notify, wristbands send data to APP using this characteristic
     */
    public static final UUID CHARACTERISTIC_UUID_NOTIFY = UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVIE_UUID = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.param_setting_layout);
        ButterKnife.bind(this);
        mIBeacon = getIntent().getParcelableExtra("ibeacon");
        mDialog = ProgressDialog.show(ParamSettingActivity.this, null,
                "正在与iBeacon设备配对", false, false);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mIBeacon.bluetoothAddress);
        if (device == null) {
            Toast.makeText(this, "配对失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mGattCallback = new DeviceGattCallback();
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }

    @Override
    protected void onDestroy() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mNotifyCharacteristic = null;
            mBluetoothGatt = null;
        }
        super.onDestroy();
    }

    class DeviceGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (mBluetoothGatt == null || status == GATT_ERROR_TIMEOUT) {
                        disConnectBle();
                    } else {
                        mBluetoothGatt.discoverServices();
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    disConnectBle();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setCharacteristicNotify();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ParamSettingActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        // TODO: 2016/7/16 赋值
                    }
                });

            } else {
                disConnectBle();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @OnClick({R.id.tv_left, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_left:
                finish();
                break;
            case R.id.tv_right:
                break;
        }
    }

    /**
     * 断开手环
     */
    public void disConnectBle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    mBluetoothGatt.close();
                    mNotifyCharacteristic = null;
                    mBluetoothGatt = null;
                    Toast.makeText(ParamSettingActivity.this, "配对失败", Toast.LENGTH_SHORT).show();
                    ParamSettingActivity.this.finish();
                }
            }
        });

    }

    /**
     * 将所有手环特征设置为notify方式
     */
    public void setCharacteristicNotify() {
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        if (gattServices == null)
            return;
        String uuid = null;
        // 遍历所有服务，找到手环的服务
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.startsWith("0000ffc0")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                        .getCharacteristics();
                // 遍历所有特征，找到发出的特征
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.startsWith("0000ffc2")) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                            if (mNotifyCharacteristic != null) {
                                setCharacteristicNotification(mBluetoothGatt,
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothGatt
                                    .readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = gattCharacteristic;
                            setCharacteristicNotification(mBluetoothGatt,
                                    gattCharacteristic, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGatt mBluetoothGatt,
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        /**
         * 打开数据FFF4
         */
        // This is specific to Heart Rate Measurement.
        if (CHARACTERISTIC_UUID_NOTIFY.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public static void writeCharacteristicData(BluetoothGatt mBluetoothGatt,
                                               byte[] byteArray) {
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);

        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = null;
        characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) {
            return;
        }
        characteristic.setValue(byteArray);
        characteristic
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
