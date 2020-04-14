package com.simprints.id.activities.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.settings_toolbar.*
import javax.inject.Inject

class ModuleSelectionActivity : AppCompatActivity() {

    private lateinit var moduleSelectionFragment: ModuleSelectionFragment
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        setContentView(R.layout.settings_toolbar)
        configureToolbar()
        moduleSelectionFragment = ModuleSelectionFragment(application as Application)
        title = androidResourcesHelper.getString(R.string.preference_select_modules_title)

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
