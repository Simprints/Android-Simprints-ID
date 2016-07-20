package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;

public class AlertActivity extends AppCompatActivity {

    private Context context;
    private int alertType;
    private boolean isExiting = false;

    private String userId = null;
    private String deviceId = null;
    private String apiKey = null;
    private String callingPackage = null;
    private String guid = null;

    private TextView alertTitleTextView;
    private ImageView alertGraphicImageView;
    private TextView alertMessageTextView;
    private TextView alertRightButtonTextView;
    private TextView alertLeftButtonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        context = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alertType = extras.getInt("alertType");
            userId = extras.getString(Constants.SIMPRINTS_USER_ID);
            deviceId = extras.getString(Constants.SIMPRINTS_DEVICE_ID);
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
            guid = extras.getString(Constants.SIMPRINTS_GUID);
        }
        Log.w("Simprints", "ID: alert number " + alertType);

        alertTitleTextView = (TextView) findViewById(R.id.title);

        alertGraphicImageView = (ImageView) findViewById(R.id.graphic);

        alertMessageTextView = (TextView) findViewById(R.id.message);

        alertLeftButtonTextView = (TextView) findViewById(R.id.left_button);
        alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (alertType) {
                    case BaseApplication.BLUETOOTH_NOT_SUPPORTED:
                        finish();
                        break;

                    case BaseApplication.BLUETOOTH_NOT_ENABLED:
                    case BaseApplication.NO_SCANNER_FOUND:
                    case BaseApplication.MULTIPLE_SCANNERS_FOUND:
                        Intent intent = new Intent(context, LaunchActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("deviceId", deviceId);
                        intent.putExtra("apiKey", apiKey);
                        intent.putExtra("callingPackage", callingPackage);
                        intent.putExtra("guid", guid);
                        startActivity(intent);
                        finish();
                        break;

                    default:
                        finish();
                        break;
                }
            }
        });

        alertRightButtonTextView = (TextView) findViewById(R.id.right_button);
        alertRightButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (alertType) {
                    case BaseApplication.BLUETOOTH_NOT_ENABLED:
                    case BaseApplication.NO_SCANNER_FOUND:
                    case BaseApplication.MULTIPLE_SCANNERS_FOUND:
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intentOpenBluetoothSettings);
                        break;

                    default:
                        break;
                }
            }
        });

        if (alertType == BaseApplication.BLUETOOTH_NOT_SUPPORTED) {
            alertTitleTextView.setText(R.string.bluetooth_not_supported_title);
            alertMessageTextView.setText(R.string.bluetooth_not_supported_message);
            alertGraphicImageView.setImageResource(R.drawable.bluetooth_not_supported);
            alertLeftButtonTextView.setText(R.string.close);
            alertRightButtonTextView.setVisibility(View.GONE);
        }

        if (alertType == BaseApplication.BLUETOOTH_NOT_ENABLED) {
            alertTitleTextView.setText(R.string.bluetooth_not_enabled_title);
            alertMessageTextView.setText(R.string.bluetooth_not_enabled_message);
            alertGraphicImageView.setImageResource(R.drawable.bluetooth_not_enabled);
            alertLeftButtonTextView.setText(R.string.try_again_label);
            alertRightButtonTextView.setText(R.string.settings_label);
        }

        if (alertType == BaseApplication.MISSING_API_KEY) {
            alertTitleTextView.setText(R.string.configuration_error_title);
            alertMessageTextView.setText(R.string.missing_apikey_message);
            alertGraphicImageView.setImageResource(R.drawable.configuration_error);
            alertLeftButtonTextView.setText(R.string.close);
            alertRightButtonTextView.setVisibility(View.GONE);
        }

        if (alertType == BaseApplication.INVALID_API_KEY) {
            alertTitleTextView.setText(R.string.configuration_error_title);
            alertMessageTextView.setText(R.string.invalid_apikey_message);
            alertGraphicImageView.setImageResource(R.drawable.configuration_error);
            alertLeftButtonTextView.setText(R.string.close);
            alertRightButtonTextView.setVisibility(View.GONE);
        }

        if (alertType == BaseApplication.NO_SCANNER_FOUND) {
            alertTitleTextView.setText(R.string.no_scanner_found_title);
            alertMessageTextView.setText(R.string.no_scanner_found_message);
            alertGraphicImageView.setImageResource(R.drawable.no_scanner_found);
            alertLeftButtonTextView.setText(R.string.try_again_label);
            alertRightButtonTextView.setText(R.string.settings_label);
        }

        if (alertType == BaseApplication.MULTIPLE_SCANNERS_FOUND) {
            alertTitleTextView.setText(R.string.multiple_scanners_found_title);
            alertMessageTextView.setText(R.string.multiple_scanners_found_message);
            alertGraphicImageView.setImageResource(R.drawable.multiple_scanners_found);
            alertLeftButtonTextView.setText(R.string.try_again_label);
            alertRightButtonTextView.setText(R.string.settings_label);
        }

        if (alertType == BaseApplication.CANNOT_CONNECT_TO_SCANNER) {
            alertTitleTextView.setText(R.string.configuration_error_title);
            alertMessageTextView.setText(R.string.cannot_connect_to_scanner_message);
            alertGraphicImageView.setImageResource(R.drawable.configuration_error);
            alertLeftButtonTextView.setText(R.string.try_again_label);
            alertRightButtonTextView.setVisibility(View.GONE);
        }

        if (alertType == BaseApplication.TIMEOUT_OCCURED) {
            alertTitleTextView.setText(R.string.timeout_occured_title);
            alertMessageTextView.setText(R.string.timeout_occured_message);
            alertGraphicImageView.setImageResource(R.drawable.configuration_error);
            alertLeftButtonTextView.setText(R.string.try_again_label);
            alertRightButtonTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                isExiting = true;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isExiting == true) {
            Scanner scanner = BaseApplication.getScanner();
            if (scanner != null) {
                scanner.disconnect();
            }
        }
    }
}
