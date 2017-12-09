package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.tools.AppState;

public class AlertActivity extends AppCompatActivity {

    DataManager dataManager;
    ALERT_TYPE alertType;

    // Singletons
    AppState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        appState = app.getAppState();

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        final ALERT_TYPE alertType = (ALERT_TYPE) extras.get(IntentKeys.alertActivityAlertTypeKey);
        assert alertType != null;
        this.alertType = alertType;

        dataManager.logAlert(alertType);

        int color = ResourcesCompat.getColor(getResources(), alertType.getBackgroundColor(), null);
        findViewById(R.id.alertLayout).setBackgroundColor(color);
        findViewById(R.id.left_button).setBackgroundColor(color);
        findViewById(R.id.right_button).setBackgroundColor(color);

        ((TextView) findViewById(R.id.title)).setText(alertType.getAlertTitleId());

        ((ImageView) findViewById(R.id.graphic)).setImageResource(alertType.getAlertMainDrawableId());

        ImageView alertHint = findViewById(R.id.hintGraphic);
        if (alertType.getAlertHintDrawableId() != -1) {
            alertHint.setImageResource(alertType.getAlertHintDrawableId());
        } else {
            alertHint.setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.message)).setText(alertType.getAlertMessageId());

        TextView alertLeftButtonTextView = findViewById(R.id.left_button);
        if (alertType.isLeftButtonActive()) {
            alertLeftButtonTextView.setText(alertType.getAlertLeftButtonTextId());
            alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setResult(alertType.getResultCode());
                    finish();
                }
            });
        }

        TextView alertRightButtonTextView = findViewById(R.id.right_button);
        if (alertType.isRightButtonActive()) {
            alertRightButtonTextView.setText(alertType.getAlertRightButtonTextId());
            alertRightButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (alertType) {
                        case BLUETOOTH_NOT_ENABLED:
                        case NOT_PAIRED:
                        case MULTIPLE_PAIRED_SCANNERS:
                        case DISCONNECTED:
                            Intent intent1 = new Intent();
                            intent1.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent1);
                            break;

                        case GUID_NOT_FOUND_OFFLINE:
                        case UNVERIFIED_API_KEY:
                            Intent intent2 = new Intent();
                            intent2.setAction(android.provider.Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent2);
                            break;

                        default:
                            setResult(RESULT_CANCELED);
                            finish();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
