package com.simprints.feature.dashboard.settings.syncinfo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncInfoBinding
import com.simprints.feature.dashboard.requestlogin.LogoutReason
import com.simprints.feature.dashboard.requestlogin.RequestLoginFragmentArgs
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCountAdapter
import com.simprints.feature.dashboard.view.ConfigurableSyncInfoFragmentContainer
import com.simprints.feature.login.LoginContract
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.navigation.toBundle
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.setPulseAnimation
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SyncInfoFragment : Fragment(R.layout.fragment_sync_info) {
    private val viewModel: SyncInfoViewModel by viewModels()
    private val binding by viewBinding(FragmentSyncInfoBinding::bind)
    private val moduleCountAdapter by lazy { ModuleCountAdapter() }

    private var syncInfoConfig: SyncInfoFragmentConfig = SyncInfoFragmentConfig()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        syncInfoConfig =
            (container?.parent as? ConfigurableSyncInfoFragmentContainer)?.syncInfoFragmentConfig
                ?: SyncInfoFragmentConfig()
        viewModel.isPreLogoutUpSync = syncInfoConfig.isSyncInfoLogoutOnComplete
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)

        binding.selectedModulesView.adapter = moduleCountAdapter

        setupClickListeners()
        observeUI()

        findNavController().handleResult(
            viewLifecycleOwner,
            getCurrentDestinationId(),
            LoginContract.DESTINATION,
            viewModel::handleLoginResult,
        )
    }

    private fun setupClickListeners() {
        binding.buttonSelectModules.setOnClickListener {
            findNavController().navigate(R.id.moduleSelectionFragment)
        }
        binding.textEventSyncInstructionsNoModules.setOnClickListener {
            findNavController().navigate(R.id.moduleSelectionFragment)
        }
        binding.syncSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.syncInfoFragment)
        }
        binding.syncInfoToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.syncReloginRequiredLoginButton.setOnClickListener {
            viewModel.requestNavigationToLogin()
        }
        binding.buttonSyncRecordsNow.setOnClickListener {
            viewModel.forceEventSync()
        }
        binding.buttonSyncImagesNow.setOnClickListener {
            viewModel.toggleImageSync()
        }
        binding.textEventSyncInstructionsCommCarePermission.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                },
            )
        }
        binding.textEventSyncInstructionsOffline.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        binding.textImageSyncInstructionsOffline.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        binding.textEventSyncInstructionsDefault.showInfoPopupOnClick(getString(IDR.string.sync_info_details_event_sync_default))
        binding.textImageSyncInstructionsDefault.showInfoPopupOnClick(getString(IDR.string.sync_info_details_image_sync_default))
        binding.textModuleSyncInstructions.showInfoPopupOnClick(getString(IDR.string.sync_info_details_module_selection))
    }

    private fun View.showInfoPopupOnClick(message: String) {
        setOnClickListener {
            AlertDialog
                .Builder(requireContext())
                .setMessage(message)
                .setPositiveButton(IDR.string.sync_info_details_ok) { di, _ -> di.dismiss() }
                .create()
                .show()
        }
    }

    private fun observeUI() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                renderSyncInfo(SyncInfo(), syncInfoConfig)
                viewModel.syncInfoLiveData.observe(viewLifecycleOwner) { syncInfo ->
                    renderSyncInfo(syncInfo, syncInfoConfig)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.logoutEventFlow.collect { reason ->
                    viewModel.performLogout()

                    val logoutReason = reason?.takeIf { it == LogoutActionReason.PROJECT_ENDING_OR_DEVICE_COMPROMISED }?.let {
                        LogoutReason(
                            title = getString(IDR.string.dashboard_sync_project_ending_alert_title),
                            body = getString(IDR.string.dashboard_sync_project_ending_message),
                        )
                    }
                    findNavController().navigateSafely(
                        parentFragment,
                        R.id.action_to_requestLoginFragment,
                        RequestLoginFragmentArgs(logoutReason = logoutReason).toBundle(),
                    )
                }
            }
        }

        viewModel.loginNavigationEventLiveData.observe(viewLifecycleOwner) { loginParams ->
            findNavController().navigate(com.simprints.feature.login.R.id.graph_login, loginParams.toBundle())
        }
    }

    private fun renderSyncInfo(
        syncInfo: SyncInfo,
        config: SyncInfoFragmentConfig,
    ) {
        // App toolbar
        binding.appBarLayout.isVisible = config.isSyncInfoToolbarVisible

        // Config loading progress bar: using `isInvisible` because the empty space should still be there if no progress bar
        binding.progressConfigRefresh.isInvisible = !syncInfo.isConfigurationLoadingProgressBarVisible

        // Sync info header
        binding.syncStatusHeader.isVisible = config.isSyncInfoStatusHeaderVisible
        binding.syncSettingsButton.isVisible = config.isSyncInfoStatusHeaderSettingsButtonVisible

        // Section separators
        binding.headerRecordSync.isVisible = config.areSyncInfoSectionHeadersVisible
        binding.sectionDivider1.isVisible = config.areSyncInfoSectionHeadersVisible
        binding.headerImageSync.isVisible = config.areSyncInfoSectionHeadersVisible
        binding.sectionDivider2.isVisible = config.areSyncInfoSectionHeadersVisible
        binding.headerModuleSelection.isVisible = config.areSyncInfoSectionHeadersVisible
        binding.sectionFooter.isVisible = !config.areSyncInfoSectionHeadersVisible

        // Re-login section
        binding.syncReLoginRequiredSection.isVisible = syncInfo.isLoginPromptSectionVisible

        // Records section
        renderRecordsSection(syncInfo.syncInfoSectionRecords, config)

        // Images section
        binding.layoutImagesSync.isVisible = config.isSyncInfoImageSyncVisible && syncInfo.isImageSyncSectionVisible
        renderImagesSection(syncInfo.syncInfoSectionImages)

        // Modules section
        renderModulesSection(syncInfo.syncInfoSectionModules, config)
    }

    private fun renderRecordsSection(
        records: SyncInfoSectionRecords,
        config: SyncInfoFragmentConfig,
    ) {
        // Counter - total records
        binding.totalRecordsCount.isVisible = records.counterTotalRecords.isNotBlank()
        binding.totalRecordsCount.text = records.counterTotalRecords
        binding.totalRecordsProgress.isVisible = records.counterTotalRecords.isBlank()

        // Counter - records to upload
        binding.layoutRecordsToDownload.isVisible = records.isCounterRecordsToDownloadVisible
        binding.recordsToUploadCount.isVisible = records.counterRecordsToUpload.isNotBlank()
        binding.recordsToUploadCount.text = records.counterRecordsToUpload
        binding.recordsToUploadProgress.isVisible = records.counterRecordsToUpload.isBlank()

        // Counter - records to download
        binding.recordsToDownloadCount.isVisible = records.counterRecordsToDownload.isNotBlank()
        binding.recordsToDownloadCount.text = records.counterRecordsToDownload
        binding.recordsToDownloadProgress.isVisible = records.counterRecordsToDownload.isBlank()

        // Counter - images to upload (may be combined with records)
        binding.layoutComboImageCounter.isVisible = config.isSyncInfoRecordsImagesCombined
        binding.comboImagesToUploadCount.isVisible = records.counterImagesToUpload.isNotBlank()
        binding.comboImagesToUploadCount.text = records.counterImagesToUpload
        binding.comboImagesToUploadProgress.isVisible = records.counterImagesToUpload.isBlank()

        // Instructions
        binding.textEventSyncInstructionsDefault.isVisible = records.recordSyncVisibleState == RecordSyncVisibleState.ON_STANDBY
        binding.textEventSyncInstructionsCommCarePermission.isVisible =
            records.recordSyncVisibleState == RecordSyncVisibleState.COMM_CARE_ERROR
        binding.textEventSyncInstructionsOffline.isVisible = records.recordSyncVisibleState == RecordSyncVisibleState.OFFLINE_ERROR
        binding.textEventSyncInstructionsNoModules.isVisible = records.recordSyncVisibleState == RecordSyncVisibleState.NO_MODULES_ERROR
        binding.textEventSyncInstructionsError.isVisible = records.recordSyncVisibleState == RecordSyncVisibleState.ERROR
        records.instructionPopupErrorInfo.configureErrorPopup()

        // Progress: using `isInvisible` because the empty space should still be there if no progress bar
        binding.layoutEventSyncProgress.isInvisible = !records.isProgressVisible
        renderProgress(
            records.progress,
            binding.eventSyncProgressBar,
            binding.textEventSyncProgress,
            IDR.string.sync_info_item_record_or_event,
            IDR.string.sync_info_item_image,
        )
        binding.eventSyncProgressBar.setPulseAnimation(isEnabled = records.isProgressVisible)

        // Sync button
        val isSyncButtonVisible = !config.isSyncInfoLogoutOnComplete || records.isSyncButtonVisible
        binding.buttonSyncRecordsNow.isVisible = isSyncButtonVisible
        binding.buttonSyncRecordsNow.isEnabled = records.isSyncButtonEnabled
        binding.buttonSyncRecordsNow.text = getString(
            when {
                records.isSyncButtonForRetry -> IDR.string.sync_info_button_try_again
                records.isProgressVisible -> IDR.string.sync_info_button_records_syncing
                else -> IDR.string.sync_info_button_sync_records
            },
        )

        // Footer
        val isFooterSyncInProgressVisible = config.isSyncInfoLogoutOnComplete && records.isFooterSyncInProgressVisible
        binding.textFooterRecordSyncInProgress.isVisible = isFooterSyncInProgressVisible
        binding.layoutFooterRecordLoggingOut.isVisible = records.isFooterReadyToLogOutVisible
        binding.textFooterRecordSyncIncomplete.isVisible = records.isFooterSyncIncompleteVisible
        binding.textFooterRecordLastSyncedWhen.isVisible = records.isFooterLastSyncTimeVisible
        binding.textFooterRecordLastSyncedWhen.text =
            String.format(getString(IDR.string.sync_info_last_sync), records.footerLastSyncMinutesAgo)
    }

    private fun SyncInfoError.configureErrorPopup() {
        binding.textEventSyncInstructionsError.showInfoPopupOnClick(
            when {
                isTooManyRequests -> getString(
                    IDR.string.sync_info_details_too_many_modules,
                )

                isBackendMaintenance && backendMaintenanceEstimatedOutage > 0 -> getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    TimeUtils.getFormattedEstimatedOutage(backendMaintenanceEstimatedOutage),
                )

                isBackendMaintenance -> getString(
                    IDR.string.error_backend_maintenance_message,
                )

                else -> getString(
                    IDR.string.sync_info_details_error,
                )
            },
        )
    }

    private fun renderImagesSection(images: SyncInfoSectionImages) {
        // Counter - images to upload
        binding.imagesToUploadCount.isVisible = images.counterImagesToUpload.isNotBlank()
        binding.imagesToUploadCount.text = images.counterImagesToUpload
        binding.imagesToUploadProgress.isVisible = images.counterImagesToUpload.isBlank()

        // Handle instruction visibility
        binding.textImageSyncInstructionsDefault.isVisible = images.isInstructionDefaultVisible
        binding.textImageSyncInstructionsOffline.isVisible = images.isInstructionOfflineVisible

        // Progress: using `isInvisible` because the empty space should still be there if no progress bar
        binding.layoutImageSyncProgress.isInvisible = !images.isProgressVisible
        renderProgress(images.progress, binding.imageSyncProgressBar, binding.textImageSyncProgress, IDR.string.sync_info_item_image)
        binding.imageSyncProgressBar.setPulseAnimation(isEnabled = images.isProgressVisible)

        // Sync button
        binding.buttonSyncImagesNow.isEnabled = images.isSyncButtonEnabled
        binding.buttonSyncImagesNow.text = getString(
            when {
                images.isProgressVisible -> IDR.string.sync_info_button_images_sync_stop
                else -> IDR.string.sync_info_button_sync_images
            },
        )
        binding.buttonSyncImagesNow.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (images.isProgressVisible) {
                IDR.color.button_sync_images_background_red
            } else {
                IDR.color.button_sync_images_background_default
            },
        )

        // Footer: using `isInvisible` because the empty space should still be there if no visible footer
        binding.textFooterImageLastSyncedWhen.isInvisible = !images.isFooterLastSyncTimeVisible
        binding.textFooterImageLastSyncedWhen.text =
            String.format(getString(IDR.string.sync_info_last_sync), images.footerLastSyncMinutesAgo)
    }

    private fun renderModulesSection(
        modules: SyncInfoSectionModules,
        config: SyncInfoFragmentConfig,
    ) {
        val isModuleSectionVisible =
            modules.isSectionAvailable && (config.isSyncInfoModuleListVisible || modules.moduleCounts.isEmpty())
        binding.layoutModuleSelection.isVisible = isModuleSectionVisible
        binding.selectedModulesView.isVisible = config.isSyncInfoModuleListVisible

        moduleCountAdapter.submitList(modules.moduleCounts)

        // RecyclerView height fix (wrong height may be caused by ConstraintLayout in parent views)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val itemHeight = resources.getDimensionPixelSize(R.dimen.module_item_height)
                val itemCount = modules.moduleCounts.size.coerceAtMost(MAX_MODULE_LIST_HEIGHT_ITEMS)
                binding.selectedModulesView.apply {
                    layoutParams = layoutParams.apply {
                        height = itemHeight * itemCount
                    }
                }
            }
        }
    }

    private fun renderProgress(
        progress: SyncInfoProgress,
        progressBar: LinearProgressIndicator,
        textView: TextView,
        vararg itemNameResIDs: Int,
    ) {
        progressBar.progress = progress.progressBarPercentage
        val progressText = progress.progressParts
            .mapIndexed { index, (isPending, isDone, areNumbersVisible, currentNumber, totalNumber) ->
                val itemName = getString(itemNameResIDs.getOrNull(index) ?: IDR.string.sync_info_item_default)
                when {
                    isPending -> getString(IDR.string.sync_info_progress_pending, itemName)
                    isDone -> getString(IDR.string.sync_info_progress_complete, itemName)
                    !areNumbersVisible -> getString(IDR.string.sync_info_progress_ongoing_no_counters, itemName)
                    else -> getString(IDR.string.sync_info_progress_ongoing, itemName, currentNumber, totalNumber)
                }
            }.joinToString(separator = "\n")

        textView.text = progressText
    }

    private fun getCurrentDestinationId() =
        parentFragment?.takeIf { !syncInfoConfig.isSyncInfoToolbarVisible }?.id // parent if this isn't standalone
            ?: id

    private companion object {
        private const val MAX_MODULE_LIST_HEIGHT_ITEMS = 5
    }
}
