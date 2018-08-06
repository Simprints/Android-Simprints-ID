package com.simprints.id.activities.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.view.MenuItem
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
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

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
            || SettingsPreferenceFragment::class.java.name == fragmentName
    }

    companion object {

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }
}
