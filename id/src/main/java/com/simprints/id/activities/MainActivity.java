package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

public class MainActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

    }
}
