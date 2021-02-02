package com.simprints.id.activities.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.settings.fingerselection.FingerSelectionActivity
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragment
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity
import kotlinx.android.synthetic.main.settings_toolbar.settingsToolbar

class SettingsActivity : AppCompatPreferenceActivity() {

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_toolbar)
        title = getString(R.string.title_activity_settings)

        setupActionBar()

        supportFragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsPreferenceFragment())
            .commit()
    }

    private fun setupActionBar() {
        settingsToolbar.title = getString(R.string.settings_title)
        setSupportActionBar(settingsToolbar)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            setResult(LOGOUT_RESULT_CODE)
            finish()
        }
    }

    fun openFingerSelectionActivity() {
        startActivity(Intent(this, FingerSelectionActivity::class.java))
    }

    fun openSettingAboutActivity() {
        startActivityForResult(Intent(this, SettingsAboutActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
    }

    fun openSyncInformationActivity() {
        startActivity(Intent(this, SyncInformationActivity::class.java))
    }

    fun openCheckLoginFromMainLauncherActivity() {
        startActivity(Intent(this, CheckLoginFromMainLauncherActivity::class.java))
        removeAnimationsToNextActivity()
    }
}
