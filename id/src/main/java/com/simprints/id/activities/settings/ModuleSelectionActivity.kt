package com.simprints.id.activities.settings

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import com.simprints.core.tools.utils.LanguageHelper
import kotlinx.android.synthetic.main.settings_toolbar.*

class ModuleSelectionActivity : BaseSplitActivity() {

    private lateinit var moduleSelectionFragment: ModuleSelectionFragment

    override fun attachBaseContext(newBase: Context) {
        val languageCtx = LanguageHelper.getLanguageConfigurationContext(newBase)
        super.attachBaseContext(languageCtx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        setContentView(R.layout.settings_toolbar)
        configureToolbar()
        moduleSelectionFragment = ModuleSelectionFragment(application as Application)
        title = getString(R.string.preference_select_modules_title)

        supportFragmentManager.beginTransaction()
            .replace(R.id.prefContent, moduleSelectionFragment)
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

    private fun configureToolbar() {
        setSupportActionBar(settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        moduleSelectionFragment.showModuleSelectionDialogIfNecessary()
    }

}
