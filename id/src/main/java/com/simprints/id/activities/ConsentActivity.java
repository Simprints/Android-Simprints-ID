package com.simprints.id.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.simprints.id.R;
import com.simprints.id.tools.AppState;

public class ConsentActivity extends AppCompatActivity {

    AppState appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        appState = AppState.getInstance();

        findViewById(R.id.consent_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsentActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
