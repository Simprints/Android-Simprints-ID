package com.simprints.id.activities.settings.syncinformation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCountAdapter
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_sync_information.*
import javax.inject.Inject

class SyncInformationActivity : AppCompatActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var viewModelFactory: SyncInformationViewModelFactory
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var peopleSyncManager: PeopleSyncManager

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }
    private val moduleCountAdapterForUnselected by lazy { ModuleCountAdapter() }

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
        enableModuleSelectionButtonAndTabsIfNecessary()
        setupAdapters()
        setupToolbar()
        setupModulesTabs()
        setupClickListeners()
        observeUi()
        showProgressOverlay()
        peopleSyncManager.getLastSyncState().observe(this, Observer { syncState ->
            if (syncState.isNotRunning()) {
                hideProgressOverlay()
                viewModel.start()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        clearValues()
        setFocusOnDefaultModulesTab()
        showProgressOverlay()
    }

    private fun setTextInLayout() {
        moduleSelectionButton.text =
            androidResourcesHelper.getString(R.string.select_modules_button_title)
        recordsToUploadText.text =
            androidResourcesHelper.getString(R.string.sync_info_records_to_upload)
        recordsToDownloadText.text =
            androidResourcesHelper.getString(R.string.sync_info_records_to_download)
        recordsToDeleteText.text =
            androidResourcesHelper.getString(R.string.sync_info_records_to_delete)
        totalRecordsOnDeviceText.text =
            androidResourcesHelper.getString(R.string.sync_info_total_records_on_device)
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

    private fun clearValues() {
        recordsToUploadCount.text = androidResourcesHelper.getString(R.string.empty)
        recordsToDownloadCount.text = androidResourcesHelper.getString(R.string.empty)
        totalRecordsCount.text = androidResourcesHelper.getString(R.string.empty)
        recordsToDeleteCount.text = androidResourcesHelper.getString(R.string.empty)
    }

    private fun setFocusOnDefaultModulesTab() {
        modulesTabHost.setCurrentTabByTag(SELECTED_MODULES_TAB_TAG)
    }

    private fun enableModuleSelectionButtonAndTabsIfNecessary() {
        if (isModuleSyncAndModuleIdOptionsNotEmpty()) {
            moduleSelectionButton.visibility = View.VISIBLE
            modulesTabHost.visibility = View.VISIBLE
        } else {
            moduleSelectionButton.visibility = View.GONE
            modulesTabHost.visibility = View.GONE
        }
    }

    private fun setupAdapters() {
        with(selectedModulesView) {
            adapter = moduleCountAdapterForSelected
            layoutManager = LinearLayoutManager(applicationContext)
        }

        with(unselectedModulesView) {
            adapter = moduleCountAdapterForUnselected
            layoutManager = LinearLayoutManager(applicationContext)
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
    }

    private fun setupClickListeners() {
        moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }

    private fun observeUi() {
        observeLocalRecordCount()
        observeUpSyncRecordCount()
        observeDownSyncRecordCount()
        observeDeleteRecordCount()
        observeSelectedModules()
        observeUnselectedModules()
    }

    private fun observeLocalRecordCount() {
        viewModel.localRecordCount.observe(this, Observer {
            totalRecordsCount.text = it.toString()
        })
    }

    private fun observeUpSyncRecordCount() {
        viewModel.recordsToUpSyncCount.observe(this, Observer {
            recordsToUploadCount.text = it.toString()
        })
    }

    private fun observeDownSyncRecordCount() {
        viewModel.recordsToDownSyncCount.observe(this, Observer {
            recordsToDownloadCount.text = it.toString()
        })
    }

    private fun observeDeleteRecordCount() {
        viewModel.recordsToDeleteCount.observe(this, Observer {
            recordsToDeleteCount.text = it.toString()
        })
    }

    private fun observeSelectedModules() {
        viewModel.selectedModulesCount.observe(this, Observer {
            addTotalRowAndSubmitList(it, moduleCountAdapterForSelected)
        })
    }

    private fun observeUnselectedModules() {
        viewModel.unselectedModulesCount.observe(this, Observer {
            if (it.isEmpty()) {
                removeUnselectedModulesTab()
            } else {
                addUnselectedModulesTabIfNecessary()
                addTotalRowAndSubmitList(it, moduleCountAdapterForUnselected)
            }
        })
    }

    private fun showProgressOverlay() {
        with(progressOverlayContainer) {
            visibility = View.VISIBLE
            setOnTouchListener { _, _ -> true }
        }
    }

    private fun hideProgressOverlay() {
        progressOverlayContainer.visibility = View.GONE
    }

    private fun addTotalRowAndSubmitList(
        moduleCounts: List<ModuleCount>,
        moduleCountAdapter: ModuleCountAdapter
    ) {
        val moduleCountsArray = ArrayList<ModuleCount>().apply {
            addAll(moduleCounts)
        }

        val totalRecordsEntry =
            ModuleCount(androidResourcesHelper.getString(R.string.sync_info_total_records),
                moduleCounts.sumBy { it.count })
        moduleCountsArray.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        moduleCountAdapter.submitList(moduleCountsArray)
    }

    private fun removeUnselectedModulesTab() {
        with(modulesTabHost.tabWidget) {
            removeView(getChildTabViewAt(UNSELECTED_MODULES_TAB_INDEX))
        }
    }

    private fun addUnselectedModulesTabIfNecessary() {
        if (modulesTabHost.tabWidget.tabCount != MAX_MODULES_TAB_COUNT) {
            modulesTabHost.addTab(unselectedModulesTabSpec)
        }
    }

    private fun PeopleSyncState.isNotRunning(): Boolean {
        val downSyncStates = downSyncWorkersInfo
        val upSyncStates = upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return allSyncStates.none { it.state is PeopleSyncWorkerState.Running }
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val UNSELECTED_MODULES_TAB_TAG = "UnselectedModules"
        private const val UNSELECTED_MODULES_TAB_INDEX = 1
        private const val MAX_MODULES_TAB_COUNT = 2
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
