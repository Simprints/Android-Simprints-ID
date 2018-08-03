package com.simprints.id.activities.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.support.v7.preference.PreferenceManager
import android.view.MenuItem
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.PreferencesManager
import kotlinx.android.synthetic.main.settings_toolbar.*
import javax.inject.Inject


class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_toolbar)
        setSupportActionBar(settingsToolbar)

        fragmentManager.beginTransaction()
            .replace(R.id.prefContent, GeneralPreferenceFragment())
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
            || GeneralPreferenceFragment::class.java.name == fragmentName
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {

        @Inject lateinit var preferencesManager: PreferencesManager

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            (activity?.application as Application).component.inject(this)
            addPreferencesFromResource(R.xml.pref_general)
            bindPreferenceSummaryToValue(findPreference("select_language"))
            bindPreferenceSummaryToValue(findPreference("select_fingers"))
            bindPreferenceSummaryToValue(findPreference("sync_upon_launch"))
            bindPreferenceSummaryToValue(findPreference("background_sync"))
            setHasOptionsMenu(true)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }


        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            when (preference) {
                is ListPreference -> {
                    val listPreference = preference
                    val index = listPreference.findIndexOfValue(stringValue)
                    preferencesManager.language = preference.value
                    preferencesManager.fingerStatus
                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

                }
                is MultiSelectListPreference -> {

                }
                is SwitchPreference -> {

                }
            }
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            if(preference is ListPreference) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getString(preference.key, ""))
            }
            else if(preference is MultiSelectListPreference) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getStringSet(preference.key, null))
            }
            else if(preference is SwitchPreference) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getBoolean(preference.key, true))
            }
        }
    }

    companion object {

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }
}

