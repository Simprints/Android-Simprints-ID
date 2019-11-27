package com.simprints.id.activities.settings.syncinformation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_sync_information.*
import javax.inject.Inject

class SyncInformationActivity : AppCompatActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var viewModelFactory: SyncInformationViewModelFactory
    @Inject lateinit var preferencesManager: PreferencesManager

    private lateinit var viewModel: SyncInformationViewModel
    private lateinit var selectedModulesTabSpec: TabHost.TabSpec
    private lateinit var unselectedModulesTabSpec: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        title = androidResourcesHelper.getString(R.string.title_activity_sync_information)
        setContentView(R.layout.activity_sync_information)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SyncInformationViewModel::class.java)

        setTextInLayout()
        disableModuleSelectionButtonIfNecessary()
        setupToolbar()
        setupModulesTabs()
        setupClickListeners()
        observeUi()
    }

    private fun setTextInLayout() {
        moduleSelectionButton.text = androidResourcesHelper.getString(R.string.select_modules_button_title)
        recordsToUploadText.text = androidResourcesHelper.getString(R.string.sync_info_records_to_upload)
        recordsToDownloadText.text = androidResourcesHelper.getString(R.string.sync_info_records_to_download)
        recordsToDeleteText.text = androidResourcesHelper.getString(R.string.sync_info_records_to_delete)
        totalRecordsOnDeviceText.text = androidResourcesHelper.getString(R.string.sync_info_total_records_on_device)
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

    private fun disableModuleSelectionButtonIfNecessary() {
        if (isModuleSyncAndModuleIdOptionsNotEmpty()) {
            moduleSelectionButton.visibility = View.VISIBLE
        }
    }

    private fun isModuleSyncAndModuleIdOptionsNotEmpty() =
        preferencesManager.moduleIdOptions.isNotEmpty() && preferencesManager.syncGroup == GROUP.MODULE

    private fun setupToolbar() {
        setSupportActionBar(syncInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupModulesTabs() {
        modulesTabHost.setup()

        selectedModulesTabSpec = modulesTabHost.newTabSpec(SELECTED_MODULES_TAB_TAG)
            .setIndicator(androidResourcesHelper.getString(R.string.sync_info_selected_modules))
            .setContent(R.id.selectedModulesView)

        unselectedModulesTabSpec = modulesTabHost.newTabSpec(UNSELECTED_MODULES_TAB_TAG)
            .setIndicator(androidResourcesHelper.getString(R.string.sync_info_unselected_modules))
            .setContent(R.id.unselectedModulesView)

        modulesTabHost.addTab(selectedModulesTabSpec)
        modulesTabHost.addTab(unselectedModulesTabSpec)
    }

    private fun setupClickListeners() {
        moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }
    private fun observeUi() {
        viewModel.localRecordCount.observe(this, Observer {
            totalRecordsCount.text = it.toString()
        })

        viewModel.recordsToUpSyncCount.observe(this, Observer {
            recordsToUploadCount.text = it.toString()
        })

        viewModel.recordsToDownSyncCount.observe(this, Observer {
            recordsToDownloadCount.text = it.toString()
        })
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val UNSELECTED_MODULES_TAB_TAG = "UnselectedModules"
    }
}
