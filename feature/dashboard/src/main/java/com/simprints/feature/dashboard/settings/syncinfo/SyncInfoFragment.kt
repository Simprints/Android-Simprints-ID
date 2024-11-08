package com.simprints.feature.dashboard.settings.syncinfo

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncInfoBinding
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCountAdapter
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
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

        findNavController().handleResult<LoginResult>(
            viewLifecycleOwner,
            R.id.syncInfoFragment,
            LoginContract.DESTINATION,
        ) { result -> viewModel.handleLoginResult(result) }
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
        binding.syncButton.setOnClickListener {
            viewModel.forceSync()
            updateSyncButton(isSyncInProgress = true)
        }
        binding.syncReloginRequiredLoginButton.setOnClickListener {
            viewModel.login()
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
            binding.recordsToDownloadCount.text = it?.let {
                if (it.isLowerBound) "${it.count}+" else "${it.count}"
            } ?: ""
            setProgressBar(it?.count, binding.recordsToDownloadCount, binding.recordsToDownloadProgress)
        }

        viewModel.moduleCounts.observe(viewLifecycleOwner) {
            updateModuleCounts(it)
        }
        viewModel.lastSyncState.observe(viewLifecycleOwner) {
            viewModel.fetchSyncInformationIfNeeded(it)
            val isRunning = it.isSyncRunning()
            updateSyncButton(isRunning)
        }
        viewModel.isSyncAvailable.observe(viewLifecycleOwner) {
            binding.syncButton.isEnabled = it
        }
        viewModel.isReloginRequired.observe(viewLifecycleOwner) { reloginRequired ->
            if (reloginRequired) {
                binding.syncReloginRequiredSection.visibility = View.VISIBLE
                binding.syncButton.visibility = View.GONE
            } else {
                binding.syncReloginRequiredSection.visibility = View.GONE
                binding.syncButton.visibility = View.VISIBLE
            }
        }
        viewModel.loginRequestedEventLiveData.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { loginArgs ->
            findNavController().navigate(
                R.id.action_syncInfoFragment_to_login,
                loginArgs
            )
        })
    }

    private fun updateSyncButton(isSyncInProgress: Boolean) {
        binding.syncButton.text = getString(
            if (isSyncInProgress) IDR.string.dashboard_sync_info_sync_in_progress
            else IDR.string.dashboard_sync_info_sync_now_button
        )
    }

    private fun enableModuleSelectionButtonAndTabsIfNecessary(synchronizationConfiguration: SynchronizationConfiguration) {
        if (viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(synchronizationConfiguration)) {
            binding.moduleSelectionButton.visibility = View.VISIBLE
            binding.modulesTabHost.visibility = View.VISIBLE
        } else {
            binding.moduleSelectionButton.visibility = View.GONE
            binding.modulesTabHost.visibility = View.GONE
        }
    }

    private fun setupRecordsCountCards(configuration: ProjectConfiguration) {
        if (!configuration.isEventDownSyncAllowed()) {
            binding.recordsToDownloadCardView.visibility = View.GONE
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
            getString(IDR.string.dashboard_sync_info_total_records),
            moduleCounts.sumOf { it.count }
        )
        moduleCountsArray.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        moduleCountAdapter.submitList(moduleCountsArray)
    }
}
