package com.simprints.id.activities.settings.syncinformation

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_sync_information.*
import javax.inject.Inject

class SyncInformationActivity : AppCompatActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        title = androidResourcesHelper.getString(R.string.title_activity_sync_information)
        setContentView(R.layout.activity_sync_information)

        setupToolbar()
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

    private fun setupToolbar() {
        setSupportActionBar(syncInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
