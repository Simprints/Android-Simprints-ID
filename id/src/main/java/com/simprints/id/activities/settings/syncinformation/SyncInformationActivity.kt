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
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.LoadingState.Calculating
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.LoadingState.Syncing
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCountAdapter
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.ON
import kotlinx.android.synthetic.main.activity_sync_information.*
import javax.inject.Inject

class SyncInformationActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: SyncInformationViewModelFactory
    @Inject
    lateinit var preferencesManager: PreferencesManager
    @Inject
    lateinit var subjectsSyncManager: SubjectsSyncManager

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }

    private lateinit var viewModel: SyncInformationViewModel
    private lateinit var selectedModulesTabSpec: TabHost.TabSpec

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
        viewModel.updateSyncInfo()
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
        imagesToUploadText.text = getString(R.string.sync_info_images_to_upload)
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
        imagesToUploadCount.text = getString(R.string.empty)
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

        modulesTabHost.addTab(selectedModulesTabSpec)
    }

    private fun setupClickListeners() {
        moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }

    private fun observeUi() {
        viewModel.getViewStateLiveData().observe(this, Observer {
            when(it) {
                Syncing, Calculating -> showProgressOverlayIfNecessary(it as ViewState.LoadingState)
                is ViewState.SyncDataFetched -> hideProgressAndShowSyncData(it)
            }
        })
    }

    private fun setupProgressOverlay() {
        progressOverlayBackground.setOnTouchListener { _, _ -> true }
        progress_sync_overlay.setOnTouchListener { _, _ -> true }
        progressBar.setOnTouchListener { _, _ -> true }
    }

    private fun showProgressOverlayIfNecessary(loadingState: ViewState.LoadingState) {
        progress_sync_overlay.text = when (loadingState) {
            Syncing -> getString(R.string.progress_sync_overlay)
            Calculating -> getString(R.string.calculating_overlay)
        }

        if (!isProgressOverlayVisible()) {
            group_progress_overlay.visibility = View.VISIBLE
        }
    }

    private fun isProgressOverlayVisible() = group_progress_overlay.visibility == View.VISIBLE

    private fun hideProgressAndShowSyncData(it: ViewState.SyncDataFetched) {
        hideProgressOverlay()
        totalRecordsCount.text = it.recordsInLocal.toString()
        recordsToUploadCount.text = it.recordsToUpSync.toString()
        recordsToDownloadCount.text = it.recordsToDownSync?.toString() ?: ""
        recordsToDeleteCount.text = it.recordsToDelete?.toString() ?: ""
        imagesToUploadCount.text = it.imagesToUpload.toString()
        addTotalRowAndSubmitList(it.moduleCounts, moduleCountAdapterForSelected)
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

    private fun setupRecordsCountCards() {
        if (!isDownSyncAllowed()) {
            recordsToDownloadCardView.visibility = View.GONE
            recordsToDeleteCardView.visibility = View.GONE
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    sealed class ViewState {
        sealed class LoadingState: ViewState() {
            object Syncing : LoadingState()
            object Calculating : LoadingState()
        }
        data class SyncDataFetched(val recordsInLocal: Int,
                                   val recordsToDownSync: Int?,
                                   val recordsToUpSync: Int,
                                   val recordsToDelete: Int?,
                                   val imagesToUpload: Int,
                                   val moduleCounts: List<ModuleCount>): ViewState()
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
