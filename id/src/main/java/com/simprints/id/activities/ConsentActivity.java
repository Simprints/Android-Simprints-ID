package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libscanner.Scanner;

public class ConsentActivity extends AppCompatActivity {

    private Context context;
    private boolean isExiting = false;
    private Button consentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        context = this;

        consentButton = (Button) findViewById(R.id.consent_button);
        consentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
