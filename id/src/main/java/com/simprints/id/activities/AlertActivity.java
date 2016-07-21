package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.AppState;
import com.simprints.id.R;

public class AlertActivity extends AppCompatActivity {

    private ALERT_TYPE alertType;

    private TextView alertTitleTextView;
    private ImageView alertGraphicImageView;
    private TextView alertMessageTextView;
    private TextView alertLeftButtonTextView;
    private TextView alertRightButtonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alertType = (ALERT_TYPE) extras.get("alertType");
        }
        Log.w("Simprints", "ID: alert number " + alertType);

        alertTitleTextView = (TextView) findViewById(R.id.title);
        alertGraphicImageView = (ImageView) findViewById(R.id.graphic);
        alertMessageTextView = (TextView) findViewById(R.id.message);
        alertLeftButtonTextView = (TextView) findViewById(R.id.left_button);
        alertRightButtonTextView = (TextView) findViewById(R.id.right_button);


        alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (alertType) {
                    case BLUETOOTH_NOT_ENABLED:
                    case BLUETOOTH_UNBONDED_SCANNER:
                    case NO_SCANNER_FOUND:
                    case MULTIPLE_SCANNERS_FOUND:
                        Intent intent = new Intent(AlertActivity.this, LaunchActivity.class);
                        intent.putExtra(AppState.randomCodeKey, AppState.getInstance().setRandomCode());
                        startActivity(intent);
                        finish();
                        break;

                    default:
                        finish();
                        break;
                }
            }
        });

        alertRightButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (alertType) {
                    case BLUETOOTH_NOT_ENABLED:
                    case NO_SCANNER_FOUND:
                    case MULTIPLE_SCANNERS_FOUND:
                    case BLUETOOTH_UNBONDED_SCANNER:
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intentOpenBluetoothSettings);
                        break;
                }
            }
        });

        switch (alertType) {
            case MISSING_API_KEY:
                setDisplay(R.string.configuration_error_title, R.string.missing_apikey_message,
                        R.drawable.configuration_error,
                        View.VISIBLE, R.string.close, View.GONE, R.string.empty);
                break;

            case INVALID_API_KEY:
                setDisplay(R.string.configuration_error_title, R.string.invalid_apikey_message,
                        R.drawable.configuration_error,
                        View.VISIBLE, R.string.close, View.GONE, R.string.empty);
                break;

            case BLUETOOTH_NOT_SUPPORTED:
                setDisplay(R.string.bluetooth_not_supported_title, R.string.bluetooth_not_supported_message,
                        R.drawable.bluetooth_not_supported,
                        View.VISIBLE, R.string.close, View.GONE, R.string.empty);
                break;

            case BLUETOOTH_NOT_ENABLED:
                setDisplay(R.string.bluetooth_not_enabled_title, R.string.bluetooth_not_enabled_message,
                        R.drawable.bluetooth_not_enabled,
                        View.VISIBLE, R.string.try_again_label, View.VISIBLE, R.string.settings_label);
                break;

            case BLUETOOTH_UNBONDED_SCANNER:
                setDisplay(R.string.unbonded_scanner_title, R.string.unbonded_scanner_message,
                        R.drawable.bluetooth_not_enabled,
                        View.VISIBLE, R.string.try_again_label, View.VISIBLE, R.string.settings_label);
                break;


            case NO_SCANNER_FOUND:
                setDisplay(R.string.no_scanner_found_title, R.string.no_scanner_found_message,
                        R.drawable.no_scanner_found,
                        View.VISIBLE, R.string.try_again_label, View.VISIBLE, R.string.settings_label);
                break;

            case MULTIPLE_SCANNERS_FOUND:
                setDisplay(R.string.multiple_scanners_found_title, R.string.multiple_scanners_found_message,
                        R.drawable.multiple_scanners_found,
                        View.VISIBLE, R.string.try_again_label, View.VISIBLE, R.string.settings_label);
                break;

            case UNEXPECTED_ERROR:
                setDisplay(R.string.error_occured_title, R.string.unforeseen_error_message,
                        R.drawable.generic_failure,
                        View.VISIBLE, R.string.close, View.GONE, R.string.empty);
                break;

        }
    }

    private void setDisplay(int alertTitle, int alertMessage, int alertGraphic,
                            int leftButtonVisibility, int leftButtonText,
                            int rightButtonVisibility, int rightButtonText)
    {
        alertTitleTextView.setText(alertTitle);
        alertMessageTextView.setText(alertMessage);
        alertGraphicImageView.setImageResource(alertGraphic);
        alertLeftButtonTextView.setText(leftButtonText);
        alertLeftButtonTextView.setVisibility(leftButtonVisibility);
        alertRightButtonTextView.setText(rightButtonText);
        alertRightButtonTextView.setVisibility(rightButtonVisibility);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
