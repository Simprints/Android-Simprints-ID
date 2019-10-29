package com.simprints.id.activities.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionFragment
import kotlinx.android.synthetic.main.settings_toolbar.*

class ModuleSelectionActivity : AppCompatActivity() {

    private val fragment = ModuleSelectionFragment.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_toolbar)
        configureToolbar()

        supportFragmentManager.beginTransaction()
            .replace(R.id.prefContent, fragment)
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

}
