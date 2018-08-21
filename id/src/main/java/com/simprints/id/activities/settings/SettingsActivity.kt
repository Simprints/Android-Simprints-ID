package com.simprints.id.activities.settings

import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.tools.extensions.isXLargeTablet
import kotlinx.android.synthetic.main.settings_toolbar.*


class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_toolbar)
        setSupportActionBar(settingsToolbar)

        fragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsPreferenceFragment())
            .commit()
    }

    override fun onIsMultiPane() = isXLargeTablet()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
            || SettingsPreferenceFragment::class.java.name == fragmentName
    }
}
