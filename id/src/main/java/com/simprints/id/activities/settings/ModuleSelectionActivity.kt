package com.simprints.id.activities.settings

import android.os.Bundle
import android.view.MenuItem
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import com.simprints.id.databinding.SettingsToolbarBinding


class ModuleSelectionActivity : BaseSplitActivity() {

    private lateinit var moduleSelectionFragment: ModuleSelectionFragment
    private val binding by viewBinding(SettingsToolbarBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        setContentView(binding.root)
        configureToolbar()
        moduleSelectionFragment = ModuleSelectionFragment()
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
        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        moduleSelectionFragment.showModuleSelectionDialogIfNecessary()
    }

}
