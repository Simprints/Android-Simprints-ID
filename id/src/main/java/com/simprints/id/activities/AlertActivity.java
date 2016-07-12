package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.R;

public class AlertActivity extends BaseNavigationActivity {

    private int alertType;

    private TextView alertTitleTextView;
    private ImageView alertGraphicImageView;
    private TextView alertMessageTextView;
    private TextView alertAction1TextView;
    private TextView alertAction2TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alertType = extras.getInt("alertType");
        }

    }
}
