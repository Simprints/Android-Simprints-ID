package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.simprints.id.R;
import com.simprints.id.tools.AppState;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (AppState.getInstance().getScanner() != null) {
            /*
                    busy = false;
        bt = new BluetoothCom(macAddr, new DefaultNotifier());
        hwConfig = Message.HARDWARE_CONFIG.HW_MODE_UNKNOWN;
        ucVersion = NO_INFO;
        un20Version = NO_INFO;
        batteryLevel1 = NO_INFO;
        batteryLevel2 = NO_INFO;
        crashLogValid = false;
        hwVersion = NO_INFO;
        un20State = Message.UN20_STATE.UN20_STATE_UNKNOWN;
        continuousCapture = false;
        imageQuality = NO_INFO;
        imageFragmentsStream = new ByteArrayOutputStream();
        imageBytes = NO_BYTES;
        templateFragmentsStream = new ByteArrayOutputStream();
        template = null;
        crashLog = Message.CRASH_LOG_STATUS.UNKNOWN;
        scannerListener = null;
             */
        }
    }
}
