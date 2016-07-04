package com.example.bluetooth.le;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterParamActivity extends Activity {

    @Bind(R.id.tv_left)
    TextView tvLeft;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_right)
    TextView tvRight;
    @Bind(R.id.et_uuid)
    EditText etUuid;
    @Bind(R.id.et_major)
    EditText etMajor;
    @Bind(R.id.et_minor)
    EditText etMinor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_param);
        ButterKnife.bind(this);
        SPUtiles.getInstance(this);
        etUuid.setText(SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_UUID, ""));
        etMajor.setText(SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_MAJOR, ""));
        etMinor.setText(SPUtiles.getStringValue(BTConstants.SP_KEY_FILTER_MINOR, ""));
    }

    @OnClick({R.id.tv_left, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_left:
                finish();
                break;
            case R.id.tv_right:
                if (TextUtils.isEmpty(etUuid.getText().toString()) ||
                        TextUtils.isEmpty(etMajor.getText().toString()) ||
                        TextUtils.isEmpty(etMinor.getText().toString())) {
                    ToastUtils.showToast(this, "不能为空！");
                    return;
                }
                SPUtiles.setStringValue(BTConstants.SP_KEY_FILTER_UUID, etUuid.getText().toString());
                SPUtiles.setStringValue(BTConstants.SP_KEY_FILTER_MAJOR, etMajor.getText().toString());
                SPUtiles.setStringValue(BTConstants.SP_KEY_FILTER_MINOR, etMinor.getText().toString());
                ToastUtils.showToast(this, "保存成功！");
                finish();
                break;
        }
    }
}
