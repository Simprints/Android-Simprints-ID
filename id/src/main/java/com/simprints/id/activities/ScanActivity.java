package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;

public class ScanActivity extends BaseNavigationActivity {

    private Context context;
    private int operation;
    private String guid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            operation = extras.getInt("operation");
            guid = extras.getString("guid");
        }

        if (operation == BaseApplication.REGISTER_SUBJECT) {
            actionBar.setTitle(R.string.register_title);
        }
        else {
            actionBar.setTitle(R.string.identify_title);
        }
    }

    @Override
    protected void onActionForward() {
        if (operation == BaseApplication.REGISTER_SUBJECT) {
            finish();
        }
        else {
            Intent intent = new Intent(this, MatchingActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
