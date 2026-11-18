package com.suntek.sample.sc1sk1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.sdkapi.api.SdkApi;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SdkApi.newInstance(getApplicationContext());
        TextView view = new TextView(this);
        String model = android.os.Build.MODEL;
        String fw = SdkApi.getInstance().DeviceInfo().getFirmwareVersion();
        view.setText("SUNTEK Model: " + model + "\nFirmware: " + fw);
        setContentView(view);
    }
}
