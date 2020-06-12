package com.simprints.id.activities.settings

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import androidx.preference.PreferenceFragment
import com.simprints.core.tools.LanguageHelper
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.isXLargeTablet
import kotlinx.android.synthetic.main.settings_toolbar.*
import javax.inject.Inject


class SettingsAboutActivity : AppCompatPreferenceActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    companion object {
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        title = androidResourcesHelper.getString(R.string.title_activity_settings_about)

        setContentView(R.layout.settings_toolbar)
        setSupportActionBar(settingsToolbar)

        fragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsAboutFragment())
            .commit()
    }

    override fun attachBaseContext(newBase: Context) {
        val ctx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(ctx)
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
            || SettingsAboutFragment::class.java.name == fragmentName
    }

    fun finishActivityBecauseLogout() {
        setResult(SettingsAboutActivity.LOGOUT_RESULT_CODE)
        finish()
    }
}
