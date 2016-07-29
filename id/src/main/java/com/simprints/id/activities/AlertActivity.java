package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.simprints.id.R;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;

public class AlertActivity extends AppCompatActivity {

    AppState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        appState = AppState.getInstance();

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        final ALERT_TYPE alertType = (ALERT_TYPE) extras.get("alertType");
        assert alertType != null;

        if (alertType.mustBeLogged()) {
            Answers.getInstance().logCustom(new CustomEvent("Alert Triggered")
                    .putCustomAttribute("Alert Type", alertType.name())
                    .putCustomAttribute("API Key", appState.getApiKey())
                    .putCustomAttribute("MAC Address", appState.getMacAddress()));
        }

        ((TextView) findViewById(R.id.title)).setText(alertType.getAlertTitleId());

        ((ImageView) findViewById(R.id.graphic)).setImageResource(alertType.getAlertDrawableId());

        ((TextView) findViewById(R.id.message)).setText(alertType.getAlertMessageId());

        TextView alertLeftButtonTextView = (TextView) findViewById(R.id.left_button);
        if (alertType.isLeftButtonActive()) {
            alertLeftButtonTextView.setText(alertType.getAlertLeftButtonTextId());
            alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (alertType) {
                        case BLUETOOTH_NOT_ENABLED:
                        case NOT_PAIRED:
                        case MULTIPLE_PAIRED_SCANNERS:
                        case NETWORK_FAILURE:
                        case SCANNER_UNREACHABLE:
                        case DISCONNECTED:
                            appState.setResultCode(InternalConstants.RESULT_TRY_AGAIN);
                            finish();
                            break;

                        default:
                            appState.setResultCode(RESULT_CANCELED);
                            finish();
                            break;
                    }
                }
            });
        }

        TextView alertRightButtonTextView = (TextView) findViewById(R.id.right_button);
        if (alertType.isRightButtonActive()) {
            alertRightButtonTextView.setText(alertType.getAlertRightButtonTextId());
            alertRightButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (alertType) {
                        case BLUETOOTH_NOT_ENABLED:
                        case NOT_PAIRED:
                        case MULTIPLE_PAIRED_SCANNERS:
                            Intent intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            appState.setResultCode(RESULT_CANCELED);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
