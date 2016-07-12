package com.simprints.id.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.simprints.id.R;

public class ConsentActivity extends BaseNavigationActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

    }

    @Override
    protected void onActionForward() {
        Intent intent = new Intent(context, ScanActivity.class);
        startActivity(intent);
        finish();
    }
}
