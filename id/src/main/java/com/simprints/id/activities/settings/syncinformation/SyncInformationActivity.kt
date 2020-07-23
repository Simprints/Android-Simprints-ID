package com.simprints.id.activities.settings.syncinformation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TabHost
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCountAdapter
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.ON
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState
import kotlinx.android.synthetic.main.activity_sync_information.*
import javax.inject.Inject

class SyncInformationActivity : BaseSplitActivity() {

    @Inject lateinit var viewModelFactory: SyncInformationViewModelFactory
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var subjectsSyncManager: SubjectsSyncManager

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }
    private val moduleCountAdapterForUnselected by lazy { ModuleCountAdapter() }

    private lateinit var viewModel: SyncInformationViewModel
    private lateinit var selectedModulesTabSpec: TabHost.TabSpec
    private lateinit var unselectedModulesTabSpec: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        title = getString(R.string.title_activity_sync_information)
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
        setupProgressOverlay()
        setupRecordsCountCards()
    }

    override fun onResume() {
        super.onResume()
        clearValues()
        setFocusOnDefaultModulesTab()
        fetchRecordsInfo()
    }

    private fun setTextInLayout() {
        moduleSelectionButton.text =
            getString(R.string.select_modules_button_title)
        recordsToUploadText.text =
            getString(R.string.sync_info_records_to_upload)
        recordsToDownloadText.text =
            getString(R.string.sync_info_records_to_download)
        recordsToDeleteText.text =
            getString(R.string.sync_info_records_to_delete)
        totalRecordsOnDeviceText.text =
            getString(R.string.sync_info_total_records_on_device)
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
        recordsToUploadCount.text = getString(R.string.empty)
        recordsToDownloadCount.text = getString(R.string.empty)
        totalRecordsCount.text = getString(R.string.empty)
        recordsToDeleteCount.text = getString(R.string.empty)
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
            .setIndicator(getString(R.string.sync_info_selected_modules))
            .setContent(R.id.selectedModulesView)

        unselectedModulesTabSpec = modulesTabHost.newTabSpec(UNSELECTED_MODULES_TAB_TAG)
            .setIndicator(getString(R.string.sync_info_unselected_modules))
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
        observeForSyncState()
    }

    private fun observeLocalRecordCount() {
        viewModel.localRecordCountLiveData.observe(this, Observer {
            totalRecordsCount.text = it.toString()
        })
    }

    private fun observeUpSyncRecordCount() {
        viewModel.recordsToUpSyncCountLiveData.observe(this, Observer {
            recordsToUploadCount.text = it.toString()
        })
    }

    private fun observeDownSyncRecordCount() {
        viewModel.recordsToDownSyncCountLiveData.observe(this, Observer {
            recordsToDownloadCount.text = it.toString()
        })
    }

    private fun observeDeleteRecordCount() {
        viewModel.recordsToDeleteCountLiveData.observe(this, Observer {
            recordsToDeleteCount.text = it.toString()
        })
    }

    private fun observeSelectedModules() {
        viewModel.selectedModulesCountLiveData.observe(this, Observer {
            addTotalRowAndSubmitList(it, moduleCountAdapterForSelected)
        })
    }

    private fun observeUnselectedModules() {
        viewModel.unselectedModulesCountLiveData.observe(this, Observer {
            if (it.isEmpty()) {
                removeUnselectedModulesTab()
            } else {
                addUnselectedModulesTabIfNecessary()
                addTotalRowAndSubmitList(it, moduleCountAdapterForUnselected)
            }
        })
    }

    private fun setupProgressOverlay() {
        progressOverlayBackground.setOnTouchListener { _, _ -> true }
        progress_sync_overlay.setOnTouchListener { _, _ -> true }
        progress_sync_overlay.text = getString(R.string.progress_sync_overlay)
        progressBar.setOnTouchListener { _, _ -> true }
    }

    private fun isProgressOverlayVisible() = group_progress_overlay.visibility == View.VISIBLE

    private fun showProgressOverlay() {
        group_progress_overlay.visibility = View.VISIBLE
    }

    private fun hideProgressOverlay() {
        group_progress_overlay.visibility = View.GONE
    }

    private fun addTotalRowAndSubmitList(
        moduleCounts: List<ModuleCount>,
        moduleCountAdapter: ModuleCountAdapter
    ) {
        val moduleCountsArray = ArrayList<ModuleCount>().apply {
            addAll(moduleCounts)
        }

        val totalRecordsEntry =
            ModuleCount(getString(R.string.sync_info_total_records),
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

    private fun observeForSyncState() {
        subjectsSyncManager.getLastSyncState().observe(this, Observer { syncState ->
            if (syncState.isRunning() && !isProgressOverlayVisible()) {
                showProgressOverlay()
            } else if (!syncState.isRunning() && isProgressOverlayVisible()) {
                hideProgressOverlay()
                fetchRecordsInfo()
            }
        })
    }

    private fun fetchRecordsInfo() {
        viewModel.fetchRecordsInfo()
    }

    private fun setupRecordsCountCards() {
        if(!isDownSyncAllowed()) {
            recordsToDownloadCardView.visibility = View.GONE
            recordsToDeleteCardView.visibility = View.GONE
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    private fun SubjectsSyncState.isRunning(): Boolean {
        val downSyncStates = downSyncWorkersInfo
        val upSyncStates = upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return allSyncStates.any {
            it.state is SubjectsSyncWorkerState.Running || it.state is SubjectsSyncWorkerState.Enqueued
        }
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val UNSELECTED_MODULES_TAB_TAG = "UnselectedModules"
        private const val UNSELECTED_MODULES_TAB_INDEX = 1
        private const val MAX_MODULES_TAB_COUNT = 2
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
