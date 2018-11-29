package com.simprints.id.activities;

import android.os.Bundle;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.data.prefs.PreferencesManager;
import com.simprints.id.tools.LanguageHelper;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TutorialActivity extends AppCompatActivity {

    @Inject PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getComponent().inject(this);

        LanguageHelper.setLanguage(this, preferencesManager.getLanguage());
        setContentView(R.layout.activity_tutorial);

        Toolbar toolbar = findViewById(R.id.toolbar_tutorial);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
