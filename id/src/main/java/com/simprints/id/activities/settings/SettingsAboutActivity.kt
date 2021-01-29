package com.simprints.id.activities.settings

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.databinding.SettingsToolbarBinding


class SettingsAboutActivity : AppCompatPreferenceActivity() {

    private lateinit var binding: SettingsToolbarBinding

    companion object {
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_activity_settings_about)

        binding = SettingsToolbarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.settingsToolbar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsAboutFragment())
            .commit()
    }

    // override fun onIsMultiPane() = isXLargeTablet()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun isValidFragment(fragmentName: String): Boolean {
//        return PreferenceFragmentCompat::class.java.name == fragmentName
//            || SettingsAboutFragment::class.java.name == fragmentName
//    }

    fun finishActivityBecauseLogout() {
        setResult(LOGOUT_RESULT_CODE)
        finish()
    }
}
