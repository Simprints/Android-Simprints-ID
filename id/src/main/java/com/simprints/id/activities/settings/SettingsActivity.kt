package com.simprints.id.activities.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import androidx.preference.PreferenceFragment
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.isXLargeTablet
import kotlinx.android.synthetic.main.settings_toolbar.*
import javax.inject.Inject


class SettingsActivity : AppCompatPreferenceActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_toolbar)
        (application as Application).component.inject(this)
        title = androidResourcesHelper.getString(R.string.title_activity_settings)

        setupActionBar()

        fragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsPreferenceFragment())
            .commit()
    }

    private fun setupActionBar() {
        settingsToolbar.title = androidResourcesHelper.getString(R.string.settings_title)
        setSupportActionBar(settingsToolbar)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            setResult(LOGOUT_RESULT_CODE)
            finish()
        }
    }

    fun openSettingAboutActivity() {
        startActivityForResult(Intent(this, SettingsAboutActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
    }

    fun openModuleSelectionActivity() {
        val intent = Intent(this, ModuleSelectionActivity::class.java)
        startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE)
    }
}
