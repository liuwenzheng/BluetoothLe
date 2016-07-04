package com.example.bluetooth.le;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RssiActivity extends Activity {

    @Bind(R.id.tv_left)
    TextView tvLeft;
    @Bind(R.id.et_nearest)
    EditText etNearest;
    @Bind(R.id.et_nearer)
    EditText etNearer;
    @Bind(R.id.et_near)
    EditText etNear;
    @Bind(R.id.et_leavetime)
    EditText etLeavetime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);
        ButterKnife.bind(this);
        SPUtiles.getInstance(this);
        etNearest.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEAREST, -40));
        etNearer.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEARER, -70));
        etNear.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_NEAR, -100));
        etLeavetime.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_RSSI_LEAVE_DURATION, 50));
    }

    @OnClick(R.id.tv_left)
    public void onClick() {
        if (TextUtils.isEmpty(etNear.getText().toString()) ||
                TextUtils.isEmpty(etNearer.getText().toString()) ||
                TextUtils.isEmpty(etNearest.getText().toString()) ||
                TextUtils.isEmpty(etLeavetime.getText().toString())) {
            ToastUtils.showToast(this, "不能为空！");
            return;
        }
        SPUtiles.setIntValue(BTConstants.SP_KEY_RSSI_NEAR, Integer.valueOf(etNear.getText().toString()));
        SPUtiles.setIntValue(BTConstants.SP_KEY_RSSI_NEARER, Integer.valueOf(etNearer.getText().toString()));
        SPUtiles.setIntValue(BTConstants.SP_KEY_RSSI_NEAREST, Integer.valueOf(etNearest.getText().toString()));
        SPUtiles.setIntValue(BTConstants.SP_KEY_RSSI_LEAVE_DURATION, Integer.valueOf(etLeavetime.getText().toString()));
        ToastUtils.showToast(this, "保存成功！");
        finish();
    }
}
