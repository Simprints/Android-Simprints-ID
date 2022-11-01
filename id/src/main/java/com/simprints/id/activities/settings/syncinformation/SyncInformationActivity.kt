package com.simprints.id.activities.settings.syncinformation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCountAdapter
import com.simprints.id.databinding.ActivitySyncInformationBinding
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class SyncInformationActivity : BaseSplitActivity() {

    private val viewModel: SyncInformationViewModel by viewModels()

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    private val binding by viewBinding(ActivitySyncInformationBinding::inflate)

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(IDR.string.title_activity_sync_information)
        setContentView(binding.root)

        setTextInLayout()
        setupAdapters()
        setupToolbar()
        setupClickListeners()
        observeUi()
    }

    override fun onResume() {
        super.onResume()

        refreshSyncInformation()
    }

    private fun setTextInLayout() {
        binding.moduleSelectionButton.text =
            getString(IDR.string.select_modules_button_title)
        binding.recordsToUploadText.text =
            getString(IDR.string.sync_info_records_to_upload)
        binding.recordsToDownloadText.text =
            getString(IDR.string.sync_info_records_to_download)
        binding.recordsToDeleteText.text =
            getString(IDR.string.sync_info_records_to_delete)
        binding.totalRecordsOnDeviceText.text =
            getString(IDR.string.sync_info_total_records_on_device)
        binding.imagesToUploadText.text = getString(IDR.string.sync_info_images_to_upload)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sync_info_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.sync_redo -> {
                refreshSyncInformation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshSyncInformation() {
        viewModel.resetFetchingSyncInformation()
        viewModel.fetchSyncInformation()
    }

    private fun enableModuleSelectionButtonAndTabsIfNecessary(synchronizationConfiguration: SynchronizationConfiguration) {
        if (isModuleSyncAndModuleIdOptionsNotEmpty(synchronizationConfiguration)) {
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

    private fun isModuleSyncAndModuleIdOptionsNotEmpty(synchronizationConfiguration: SynchronizationConfiguration) =
        with(synchronizationConfiguration.down) {
            moduleOptions?.isNotEmpty() == true &&
                partitionType == DownSynchronizationConfiguration.PartitionType.MODULE
        }

    private fun setupToolbar() {
        setSupportActionBar(binding.syncInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupClickListeners() {
        binding.moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }

    private fun observeUi() {
        viewModel.synchronizationConfiguration.observe(this) {
            enableModuleSelectionButtonAndTabsIfNecessary(it)
            setupRecordsCountCards(it)
        }

        viewModel.recordsInLocal.observe(this) {
            binding.totalRecordsCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.totalRecordsCount, binding.totalRecordsProgress)
        }

        viewModel.recordsToUpSync.observe(this) {
            binding.recordsToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToUploadCount, binding.recordsToUploadProgress)
        }

        viewModel.imagesToUpload.observe(this) {
            binding.imagesToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.imagesToUploadCount, binding.imagesToUploadProgress)
        }

        viewModel.recordsToDownSync.observe(this) {
            binding.recordsToDownloadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDownloadCount, binding.recordsToDownloadProgress)
        }

        viewModel.recordsToDelete.observe(this) {
            binding.recordsToDeleteCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDeleteCount, binding.recordsToDeleteProgress)
        }

        viewModel.moduleCounts.observe(this) {
            it?.let {
                addTotalRowAndSubmitList(it, moduleCountAdapterForSelected)
            }
        }

        eventSyncManager.getLastSyncState().observe(this) {
            viewModel.fetchSyncInformationIfNeeded(it)
        }
    }

    private fun setProgressBar(value: Int?, tv: TextView, pb: ProgressBar) {
        if (value == null) {
            pb.visibility = View.VISIBLE
            tv.visibility = View.GONE
        } else {
            pb.visibility = View.GONE
            tv.visibility = View.VISIBLE
        }
    }

    private fun addTotalRowAndSubmitList(
        moduleCounts: List<ModuleCount>,
        moduleCountAdapter: ModuleCountAdapter
    ) {
        val moduleCountsArray = ArrayList<ModuleCount>().apply {
            addAll(moduleCounts)
        }

        val totalRecordsEntry =
            ModuleCount(getString(IDR.string.sync_info_total_records),
                moduleCounts.sumOf { it.count })
        moduleCountsArray.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        moduleCountAdapter.submitList(moduleCountsArray)
    }

    private fun setupRecordsCountCards(synchronizationConfiguration: SynchronizationConfiguration) {
        if (!synchronizationConfiguration.canDownSyncEvents()) {
            binding.recordsToDownloadCardView.visibility = View.GONE
            binding.recordsToDeleteCardView.visibility = View.GONE
        }

        if (!synchronizationConfiguration.canSyncDataToSimprints()) {
            binding.recordsToUploadCardView.visibility = View.GONE
            binding.imagesToUploadCardView.visibility = View.GONE
        }
    }

    private fun SynchronizationConfiguration.canDownSyncEvents(): Boolean =
        frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

    private fun SynchronizationConfiguration.canSyncDataToSimprints(): Boolean =
        up.simprints.kind != UpSynchronizationConfiguration.UpSynchronizationKind.NONE

    companion object {
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
