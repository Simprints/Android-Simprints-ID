package com.simprints.feature.dashboard.settings.syncinfo

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncInfoBinding
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCountAdapter
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration.PartitionType
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.canSyncDataToSimprints
import com.simprints.infra.config.domain.models.isEventDownSyncAllowed
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SyncInfoFragment : Fragment(R.layout.fragment_sync_info) {

    companion object {
        private const val TOTAL_RECORDS_INDEX = 0
    }

    private val viewModel: SyncInfoViewModel by viewModels()
    private val binding by viewBinding(FragmentSyncInfoBinding::bind)
    private val moduleCountAdapter by lazy { ModuleCountAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectedModulesView.adapter = moduleCountAdapter
        setupClickListeners()
        observeUI()
        viewModel.refreshInformation()
    }

    private fun setupClickListeners() {
        binding.moduleSelectionButton.setOnClickListener {
            findNavController().navigate(R.id.action_syncInfoFragment_to_moduleSelectionFragment)
        }
        binding.syncInfoToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.syncInfoToolbar.setOnMenuItemClickListener {
            viewModel.refreshInformation()
            true
        }
    }

    private fun observeUI() {
        viewModel.configuration.observe(viewLifecycleOwner) {
            enableModuleSelectionButtonAndTabsIfNecessary(it.synchronization)
            setupRecordsCountCards(it)
        }
        viewModel.recordsInLocal.observe(viewLifecycleOwner) {
            binding.totalRecordsCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.totalRecordsCount, binding.totalRecordsProgress)
        }

        viewModel.recordsToUpSync.observe(viewLifecycleOwner) {
            binding.recordsToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToUploadCount, binding.recordsToUploadProgress)
        }

        viewModel.imagesToUpload.observe(viewLifecycleOwner) {
            binding.imagesToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.imagesToUploadCount, binding.imagesToUploadProgress)
        }

        viewModel.recordsToDownSync.observe(viewLifecycleOwner) {
            binding.recordsToDownloadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDownloadCount, binding.recordsToDownloadProgress)
        }

        viewModel.recordsToDelete.observe(viewLifecycleOwner) {
            binding.recordsToDeleteCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDeleteCount, binding.recordsToDeleteProgress)
        }

        viewModel.moduleCounts.observe(viewLifecycleOwner) {
            updateModuleCounts(it)
        }

        viewModel.lastSyncState.observe(viewLifecycleOwner) {
            viewModel.fetchSyncInformationIfNeeded(it)
        }
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

    private fun isModuleSyncAndModuleIdOptionsNotEmpty(synchronizationConfiguration: SynchronizationConfiguration) =
        with(synchronizationConfiguration.down) {
            moduleOptions.isNotEmpty() && partitionType == PartitionType.MODULE
        }

    private fun setupRecordsCountCards(configuration: ProjectConfiguration) {
        if (!configuration.isEventDownSyncAllowed()) {
            binding.recordsToDownloadCardView.visibility = View.GONE
            binding.recordsToDeleteCardView.visibility = View.GONE
        }

        if (!configuration.canSyncDataToSimprints()) {
            binding.recordsToUploadCardView.visibility = View.GONE
            binding.imagesToUploadCardView.visibility = View.GONE
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

    private fun updateModuleCounts(moduleCounts: List<ModuleCount>) {
        val moduleCountsArray = ArrayList<ModuleCount>().apply {
            addAll(moduleCounts)
        }

        val totalRecordsEntry = ModuleCount(
            getString(IDR.string.sync_info_total_records),
            moduleCounts.sumOf { it.count }
        )
        moduleCountsArray.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        moduleCountAdapter.submitList(moduleCountsArray)
    }
}