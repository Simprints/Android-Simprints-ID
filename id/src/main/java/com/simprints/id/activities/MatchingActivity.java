package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

public class MatchingActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }

    }
}
