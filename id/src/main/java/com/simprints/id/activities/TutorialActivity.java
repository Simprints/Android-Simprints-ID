package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.DataManager;
import com.simprints.id.tools.Language;

public class TutorialActivity extends AppCompatActivity {

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();

        getBaseContext().getResources().updateConfiguration(
                Language.selectLanguage(dataManager.getLanguage()),
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_tutorial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tutorial);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
