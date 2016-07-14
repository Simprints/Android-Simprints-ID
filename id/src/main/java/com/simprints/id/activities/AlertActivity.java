package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.libsimprints.Constants;

public class AlertActivity extends AppCompatActivity {

    private int alertType;

    private TextView alertTitleTextView;
    private ImageView alertGraphicImageView;
    private TextView alertMessageTextView;
    private TextView alertRightButtonTextView;
    private TextView alertLeftButtonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alertType = extras.getInt("alertType");
        }

        alertTitleTextView = (TextView) findViewById(R.id.title);

        alertGraphicImageView = (ImageView) findViewById(R.id.graphic);

        alertMessageTextView = (TextView) findViewById(R.id.message);

        alertLeftButtonTextView = (TextView) findViewById(R.id.left_button);
        alertLeftButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (alertType) {
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
                    default:
                        break;
                }
            }
        });

        if (alertType == BaseApplication.MISSING_API_KEY) {
            alertTitleTextView.setText(R.string.configuration_error_title);
            alertMessageTextView.setText(R.string.missing_apikey_message);
            alertGraphicImageView.setImageResource(R.drawable.configuration_error);
            alertLeftButtonTextView.setText(R.string.close);
            alertRightButtonTextView.setVisibility(View.GONE);
        }
    }
}
