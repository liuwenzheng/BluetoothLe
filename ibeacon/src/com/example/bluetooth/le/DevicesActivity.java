package com.example.bluetooth.le;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DevicesActivity extends Activity {

    @Bind(R.id.rv_devices)
    RecyclerView rvDevices;
    private List<iBeaconClass.iBeacon> mDatas;
    private DeviceAdapter mAdapter;
    private ProgressDialog mDialog;
    public static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices_layout);
        ButterKnife.bind(this);
        mHandler = new Handler(getApplication().getMainLooper());
        initData();
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setItemAnimator(new DefaultItemAnimator());
        rvDevices.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new DeviceAdapter();
        rvDevices.setAdapter(mAdapter);
        mAdapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: 2016/7/15 连接设备并跳转
            }
        });
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        scanLeDevice(true);
    }

    private BluetoothAdapter mBluetoothAdapter;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mDialog = ProgressDialog.show(this, null,"正在搜索iBeacon设备", false, false);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsStartScan) {
                        LogModule.i(SCAN_PERIOD / 1000 + "s后停止扫描");
                        BTModule.mBluetoothAdapter.stopLeScan(BTService.this);
                        mIsStartScan = false;
                        Intent intent = new Intent(
                                BTConstants.ACTION_BLE_DEVICES_DATA_END);
                        // intent.putExtra("devices", mDevices);
                        sendBroadcast(intent);
                    }
                }
            }, SCAN_PERIOD);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                    iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
                    if (ibeacon == null) {
                        return;
                    }
                    for (iBeaconClass.iBeacon i : mDatas) {
                        if (i.bluetoothAddress.equals(ibeacon.bluetoothAddress)) {
                            return;
                        }
                    }
                    mDatas.add(ibeacon);
                    Collections.sort(mDatas);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private void initData() {
        mDatas = new ArrayList<>();
    }

    @OnClick({R.id.tv_left, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_left:
                break;
            case R.id.tv_right:
                break;
        }
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    class DeviceAdapter extends RecyclerView.Adapter {

        private OnItemClickLitener mOnItemClickLitener;

        public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DeviceViewHolder holder = new DeviceViewHolder(LayoutInflater.from(
                    DevicesActivity.this).inflate(R.layout.devices_item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            ((DeviceViewHolder) holder).tv_device_name.setText(String.format("Device Name : %s", mDatas.get(position).name));
            ((DeviceViewHolder) holder).tv_device_mac.setText(String.format("Device Mac : %s", mDatas.get(position).bluetoothAddress));
            ((DeviceViewHolder) holder).tv_device_rssi.setText(String.format("Device Rssi : %s", mDatas.get(position).rssi));
            // 如果设置了回调，则设置点击事件
            if (mOnItemClickLitener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(holder.itemView, pos);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class DeviceViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.tv_device_name)
            TextView tv_device_name;
            @Bind(R.id.tv_device_rssi)
            TextView tv_device_rssi;
            @Bind(R.id.tv_device_mac)
            TextView tv_device_mac;

            public DeviceViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
