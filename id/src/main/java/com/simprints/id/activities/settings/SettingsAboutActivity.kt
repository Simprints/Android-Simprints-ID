package com.simprints.id.activities.settings

import android.os.Bundle
import android.view.MenuItem
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragment
import com.simprints.id.databinding.SettingsToolbarBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class SettingsAboutActivity : BaseSplitActivity() {

    private val binding by viewBinding(SettingsToolbarBinding::inflate)

    companion object {
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(IDR.string.title_activity_settings_about)

        setContentView(binding.root)
        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            .replace(R.id.prefContent, SettingsAboutFragment())
            .commit()
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

    fun finishActivityBecauseLogout() {
        setResult(LOGOUT_RESULT_CODE)
        finish()
    }
}
