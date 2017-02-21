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
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.libsimprints.Constants;

public class AlertActivity extends AppCompatActivity {

    AppState appState;
    Analytics analytics;
    ALERT_TYPE alertType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        appState = AppState.getInstance();
        analytics = Analytics.getInstance(getApplicationContext());


        Bundle extras = getIntent().getExtras();
        assert extras != null;
        final ALERT_TYPE alertType = (ALERT_TYPE) extras.get("alertType");
        assert alertType != null;
        this.alertType = alertType;

        if (alertType.mustBeLogged()) {
            Answers.getInstance().logCustom(new CustomEvent("Alert Triggered")
                    .putCustomAttribute("Alert Type", alertType.name())
                    .putCustomAttribute("API Key", appState.getApiKey())
                    .putCustomAttribute("MAC Address", appState.getMacAddress()));
        }

        ((TextView) findViewById(R.id.title)).setText(alertType.getAlertTitleId());

        ((ImageView) findViewById(R.id.graphic)).setImageResource(alertType.getAlertMainDrawableId());

        ImageView alertHint = (ImageView) findViewById(R.id.hintGraphic);
        if (alertType.getAlertHintDrawableId() != -1) {
            alertHint.setImageResource(alertType.getAlertHintDrawableId());
        } else {
            alertHint.setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.message)).setText(alertType.getAlertMessageId());

        TextView alertLeftButtonTextView = (TextView) findViewById(R.id.left_button);
        if (alertType.isLeftButtonActive()) {
            alertLeftButtonTextView.setText(alertType.getAlertLeftButtonTextId());
            alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    switch (alertType) {
                        case MISSING_API_KEY:
                            setResult(Constants.SIMPRINTS_MISSING_API_KEY);
                            break;
                        case MISSING_USER_ID:
                            setResult(Constants.SIMPRINTS_MISSING_USER_ID);
                            break;
                        case MISSING_MODULE_ID:
                            setResult(Constants.SIMPRINTS_MISSING_MODULE_ID);
                            break;
                        case MISSING_UPDATE_GUID:
                            setResult(Constants.SIMPRINTS_MISSING_UPDATE_GUID);
                            break;
                        case INVALID_API_KEY:
                            setResult(Constants.SIMPRINTS_INVALID_API_KEY);
                            break;
                        case UNEXPECTED_ERROR:
                        case BLUETOOTH_NOT_ENABLED:
                        case NOT_PAIRED:
                        case MULTIPLE_PAIRED_SCANNERS:
                        case DISCONNECTED:
                        case UNVERIFIED_API_KEY:
                            setResult(InternalConstants.RESULT_TRY_AGAIN);
                            break;
                        case BLUETOOTH_NOT_SUPPORTED:
                        default:
                            setResult(RESULT_CANCELED);
                            break;
                    }

                    analytics.setAlert(alertType, false);
                    finish();
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
                        case DISCONNECTED:
                            Intent intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent);
                            break;

                        default:
                            setResult(RESULT_CANCELED);
                            analytics.setAlert(alertType, false);
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
