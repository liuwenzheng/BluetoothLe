package com.example.bluetooth.le;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends Activity {

    @Bind(R.id.tv_left)
    TextView tvLeft;
    @Bind(R.id.tv_filter)
    TextView tvFilter;
    @Bind(R.id.tv_rssi)
    TextView tvRssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.tv_left, R.id.tv_filter, R.id.tv_rssi})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_left:
                this.finish();
                break;
            case R.id.tv_filter:
                startActivity(new Intent(this, FilterParamActivity.class));
                break;
            case R.id.tv_rssi:
                startActivity(new Intent(this, RssiActivity.class));
                break;
        }
    }
}
