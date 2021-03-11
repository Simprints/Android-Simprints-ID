package com.simprints.id.activities.settings.syncinformation

import android.annotation.SuppressLint
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
import com.simprints.id.databinding.ActivitySyncInformationBinding
import com.simprints.id.domain.GROUP
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import javax.inject.Inject

class SyncInformationActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: SyncInformationViewModelFactory

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var eventSyncManager: EventSyncManager
    private lateinit var binding: ActivitySyncInformationBinding

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }

    private lateinit var viewModel: SyncInformationViewModel
    private lateinit var selectedModulesTabSpec: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        title = getString(R.string.title_activity_sync_information)
        binding = ActivitySyncInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        viewModel.fetchSyncInformation()
    }

    override fun onResume() {
        super.onResume()
        setFocusOnDefaultModulesTab()
    }

    private fun setTextInLayout() {
        binding.moduleSelectionButton.text =
            getString(R.string.select_modules_button_title)
        binding.recordsToUploadText.text =
            getString(R.string.sync_info_records_to_upload)
        binding.recordsToDownloadText.text =
            getString(R.string.sync_info_records_to_download)
        binding.recordsToDeleteText.text =
            getString(R.string.sync_info_records_to_delete)
        binding.totalRecordsOnDeviceText.text =
            getString(R.string.sync_info_total_records_on_device)
        binding.imagesToUploadText.text = getString(R.string.sync_info_images_to_upload)
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
        binding.recordsToUploadCount.text = getString(R.string.empty)
        binding.recordsToDownloadCount.text = getString(R.string.empty)
        binding.totalRecordsCount.text = getString(R.string.empty)
        binding.recordsToDeleteCount.text = getString(R.string.empty)
        binding.imagesToUploadCount.text = getString(R.string.empty)
    }

    private fun setFocusOnDefaultModulesTab() {
        binding.modulesTabHost.setCurrentTabByTag(SELECTED_MODULES_TAB_TAG)
    }

    private fun enableModuleSelectionButtonAndTabsIfNecessary() {
        if (isModuleSyncAndModuleIdOptionsNotEmpty()) {
            binding.moduleSelectionButton.visibility = View.VISIBLE
            binding.modulesTabHost.visibility = View.VISIBLE
        } else {
            binding.moduleSelectionButton.visibility = View.GONE
            binding.modulesTabHost.visibility = View.GONE
        }
    }

    private fun setupAdapters() {
        with(binding.selectedModulesView) {
            adapter = moduleCountAdapterForSelected
            layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    private fun isModuleSyncAndModuleIdOptionsNotEmpty() =
        preferencesManager.moduleIdOptions.isNotEmpty() && preferencesManager.syncGroup == GROUP.MODULE

    private fun setupToolbar() {
        setSupportActionBar(binding.syncInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupModulesTabs() {
        binding.modulesTabHost.setup()

        selectedModulesTabSpec = binding.modulesTabHost.newTabSpec(SELECTED_MODULES_TAB_TAG)
            .setIndicator(getString(R.string.sync_info_selected_modules))
            .setContent(R.id.selectedModulesView)

        binding.modulesTabHost.addTab(selectedModulesTabSpec)
    }

    private fun setupClickListeners() {
        binding.moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }

    private fun observeUi() {
        viewModel.getViewStateLiveData().observe(this, Observer {
            when (it) {
                Syncing, Calculating -> showProgressOverlayAndClearValuesIfNecessary(it as ViewState.LoadingState)
                is ViewState.SyncDataFetched -> hideProgressAndShowSyncData(it)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupProgressOverlay() {
        binding.progressOverlayBackground.setOnTouchListener { _, _ -> true }
        binding.progressSyncOverlay.setOnTouchListener { _, _ -> true }
        binding.progressBar.setOnTouchListener { _, _ -> true }
    }

    private fun showProgressOverlayAndClearValuesIfNecessary(loadingState: ViewState.LoadingState) {
        binding.progressSyncOverlay.text = when (loadingState) {
            Syncing -> getString(R.string.progress_sync_overlay)
            Calculating -> getString(R.string.calculating_overlay)
        }

        if (!isProgressOverlayVisible()) {
            clearValues()
            binding.groupProgressOverlay.visibility = View.VISIBLE
        }
    }

    private fun isProgressOverlayVisible() = binding.groupProgressOverlay.visibility == View.VISIBLE

    private fun hideProgressAndShowSyncData(it: ViewState.SyncDataFetched) {
        hideProgressOverlay()
        binding.totalRecordsCount.text = it.recordsInLocal.toString()
        binding.recordsToUploadCount.text = it.recordsToUpSync.toString()
        binding.recordsToDownloadCount.text = it.recordsToDownSync?.toString() ?: ""
        binding.recordsToDeleteCount.text = it.recordsToDelete?.toString() ?: ""
        binding.imagesToUploadCount.text = it.imagesToUpload.toString()
        addTotalRowAndSubmitList(it.moduleCounts, moduleCountAdapterForSelected)
    }

    private fun hideProgressOverlay() {
        binding.groupProgressOverlay.visibility = View.GONE
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
            binding.recordsToDownloadCardView.visibility = View.GONE
            binding.recordsToDeleteCardView.visibility = View.GONE
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
    }

    sealed class ViewState {
        sealed class LoadingState : ViewState() {
            object Syncing : LoadingState()
            object Calculating : LoadingState()
        }

        data class SyncDataFetched(
            val recordsInLocal: Int,
            val recordsToDownSync: Int?,
            val recordsToUpSync: Int,
            val recordsToDelete: Int?,
            val imagesToUpload: Int,
            val moduleCounts: List<ModuleCount>
        ) : ViewState()
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
