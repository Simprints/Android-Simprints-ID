package com.simprints.feature.dashboard.settings.syncinfo

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncInfoBinding
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCountAdapter
import com.simprints.feature.dashboard.view.ConfigurableSyncInfoFragmentContainer
import com.simprints.feature.login.LoginContract
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.toBundle
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SyncInfoFragment : Fragment(R.layout.fragment_sync_info) {
    private val viewModel: SyncInfoViewModel by viewModels()
    private val binding by viewBinding(FragmentSyncInfoBinding::bind)
    private val moduleCountAdapter by lazy { ModuleCountAdapter() }

    private var syncInfoConfig: SyncInfoFragmentConfig = SyncInfoFragmentConfig()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
            AlertDialog.Builder(requireContext())
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
                viewModel.logoutEventLiveData.observe(viewLifecycleOwner) { logoutIfNotNull ->
                    logoutIfNotNull?.let {
                        viewModel.logout()
                    }
                }
            }
        }

        viewModel.loginNavigationEventLiveData.observe(viewLifecycleOwner) { loginParams ->
            findNavController().navigate(com.simprints.feature.login.R.id.graph_login, loginParams.toBundle())
        }
    }

    private fun renderSyncInfo(syncInfo: SyncInfo, config: SyncInfoFragmentConfig) {
        // note: ".isGone = not" is preferred to ".isVisible =" below for non-ambiguity of the no-show state

        // App toolbar
        binding.appBarLayout.isGone = !config.isSyncInfoToolbarVisible

        // Config loading progress bar
        binding.progressConfigRefresh.isInvisible = !syncInfo.isConfigurationLoadingProgressBarVisible

        // Sync info header
        binding.syncStatusHeader.isGone = !config.isSyncInfoStatusHeaderVisible
        binding.syncSettingsButton.isGone = !config.isSyncInfoStatusHeaderSettingsButtonVisible

        // Section separators
        binding.headerRecordSync.isGone = !config.areSyncInfoSectionHeadersVisible
        binding.sectionDivider1.isGone = !config.areSyncInfoSectionHeadersVisible
        binding.headerImageSync.isGone = !config.areSyncInfoSectionHeadersVisible
        binding.sectionDivider2.isGone = !config.areSyncInfoSectionHeadersVisible
        binding.headerModuleSelection.isGone = !config.areSyncInfoSectionHeadersVisible
        binding.sectionFooter.isGone = config.areSyncInfoSectionHeadersVisible

        // Re-login section
        binding.syncReLoginRequiredSection.isGone = !syncInfo.isLoginPromptSectionVisible

        // Records section
        renderRecordsSection(syncInfo.syncInfoSectionRecords, config)

        // Images section
        binding.layoutImagesSync.isGone = !config.isSyncInfoImageSyncVisible
        renderImagesSection(syncInfo.syncInfoSectionImages)

        // Modules section
        renderModulesSection(syncInfo.syncInfoSectionModules, config)
    }

    private fun renderRecordsSection(records: SyncInfoSectionRecords, config: SyncInfoFragmentConfig) {
        // Counter - total records
        binding.totalRecordsCount.isGone = records.counterTotalRecords.isBlank()
        binding.totalRecordsCount.text = records.counterTotalRecords
        binding.totalRecordsProgress.isGone = records.counterTotalRecords.isNotBlank()

        // Counter - records to upload
        binding.layoutRecordsToDownload.isGone = !records.isCounterRecordsToDownloadVisible
        binding.recordsToUploadCount.isGone = records.counterRecordsToUpload.isBlank()
        binding.recordsToUploadCount.text = records.counterRecordsToUpload
        binding.recordsToUploadProgress.isGone = records.counterRecordsToUpload.isNotBlank()

        // Counter - records to download
        binding.recordsToDownloadCount.isGone = records.counterRecordsToDownload.isBlank()
        binding.recordsToDownloadCount.text = records.counterRecordsToDownload
        binding.recordsToDownloadProgress.isGone = records.counterRecordsToDownload.isNotBlank()

        // Counter - images to upload (may be combined with records)
        binding.layoutComboImageCounter.isGone = !config.isSyncInfoRecordsImagesCombined
        binding.comboImagesToUploadCount.isGone = records.counterImagesToUpload.isBlank()
        binding.comboImagesToUploadCount.text = records.counterImagesToUpload
        binding.comboImagesToUploadProgress.isGone = records.counterImagesToUpload.isNotBlank()

        // Instructions
        binding.textEventSyncInstructionsDefault.isGone = !records.isInstructionDefaultVisible
        binding.textEventSyncInstructionsOffline.isGone = !records.isInstructionOfflineVisible
        binding.textEventSyncInstructionsNoModules.isGone = !records.isInstructionNoModulesVisible
        binding.textEventSyncInstructionsError.isGone = !records.isInstructionErrorVisible
        records.instructionPopupErrorInfo.configureErrorPopup()

        // Progress
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
        binding.buttonSyncRecordsNow.isGone = !isSyncButtonVisible
        binding.buttonSyncRecordsNow.isEnabled = records.isSyncButtonEnabled
        binding.buttonSyncRecordsNow.text = getString(
            when {
                records.isSyncButtonForRetry -> IDR.string.sync_info_button_try_again
                records.isProgressVisible -> IDR.string.sync_info_button_records_syncing
                else -> IDR.string.sync_info_button_sync_records
            }
        )

        // Footer
        val isFooterSyncInProgressVisible = config.isSyncInfoLogoutOnComplete && records.isFooterSyncInProgressVisible
        binding.textFooterRecordSyncInProgress.isGone = !isFooterSyncInProgressVisible
        binding.textFooterRecordLoggingOut.isGone = !records.isFooterReadyToLogOutVisible
        binding.textFooterRecordSyncIncomplete.isGone = !records.isFooterSyncIncompleteVisible
        binding.textFooterRecordLastSyncedWhen.isGone = !records.isFooterLastSyncTimeVisible
        binding.textFooterRecordLastSyncedWhen.text = formatLastSyncTime(records.footerLastSyncMinutesAgo)
    }

    private fun SyncInfoError.configureErrorPopup() {
        binding.textEventSyncInstructionsError.showInfoPopupOnClick(
            when {
                isTooManyRequests -> getString(
                    IDR.string.sync_info_details_too_many_modules
                )

                isBackendMaintenance && backendMaintenanceEstimatedOutage > 0 -> getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    TimeUtils.getFormattedEstimatedOutage(backendMaintenanceEstimatedOutage),
                )

                isBackendMaintenance -> getString(
                    IDR.string.error_backend_maintenance_message
                )

                else -> getString(
                    IDR.string.sync_info_details_error
                )
            }
        )
    }

    private fun renderImagesSection(images: SyncInfoSectionImages) {
        // Counter - images to upload
        binding.imagesToUploadCount.isGone = images.counterImagesToUpload.isBlank()
        binding.imagesToUploadCount.text = images.counterImagesToUpload
        binding.imagesToUploadProgress.isGone = images.counterImagesToUpload.isNotBlank()

        // Handle instruction visibility
        binding.textImageSyncInstructionsDefault.isGone = !images.isInstructionDefaultVisible
        binding.textImageSyncInstructionsOffline.isGone = !images.isInstructionOfflineVisible

        // Progress
        binding.layoutImageSyncProgress.isInvisible = !images.isProgressVisible
        renderProgress(images.progress, binding.imageSyncProgressBar, binding.textImageSyncProgress, IDR.string.sync_info_item_image)
        binding.imageSyncProgressBar.setPulseAnimation(isEnabled = images.isProgressVisible)

        // Sync button
        binding.buttonSyncImagesNow.isEnabled = images.isSyncButtonEnabled
        binding.buttonSyncImagesNow.text = getString(
            when {
                images.isProgressVisible -> IDR.string.sync_info_button_images_sync_stop
                else -> IDR.string.sync_info_button_sync_images
            }
        )
        binding.buttonSyncImagesNow.backgroundTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled), // enabled
                intArrayOf(-android.R.attr.state_enabled) // disabled
            ),
            intArrayOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (images.isProgressVisible) {
                        IDR.color.simprints_red_dark
                    } else {
                        IDR.color.simprints_orange
                    }
                ),
                ContextCompat.getColor(requireContext(), IDR.color.simprints_grey_disabled),
            ),
        )

        // Footer
        binding.textFooterImageLastSyncedWhen.isInvisible = !images.isFooterLastSyncTimeVisible
        binding.textFooterImageLastSyncedWhen.text = formatLastSyncTime(images.footerLastSyncMinutesAgo)
    }

    private fun renderModulesSection(modules: SyncInfoSectionModules, config: SyncInfoFragmentConfig) {
        val isModuleSectionVisible =
            modules.isSectionAvailable && (config.isSyncInfoModuleListVisible || modules.moduleCounts.isEmpty())
        binding.layoutModuleSelection.isGone = !isModuleSectionVisible
        binding.selectedModulesView.isGone = !config.isSyncInfoModuleListVisible

        val moduleCountsForAdapter = modules.moduleCounts.map { syncInfoModuleCount ->
            ModuleCount(
                name = if (syncInfoModuleCount.isTotal) {
                    getString(IDR.string.sync_info_total_records)
                } else {
                    syncInfoModuleCount.name
                },
                count = syncInfoModuleCount.count.toIntOrNull() ?: 0
            )
        }

        moduleCountAdapter.submitList(moduleCountsForAdapter)

        // RecyclerView height fix (wrong height may be caused by ConstraintLayout in parent views)
        binding.selectedModulesView.post {
            val itemHeight = resources.getDimensionPixelSize(R.dimen.module_item_height)
            val itemCount = moduleCountsForAdapter.size.coerceAtMost(MAX_MODULE_LIST_HEIGHT_ITEMS)
            binding.selectedModulesView.apply {
                layoutParams = layoutParams.apply {
                    height = itemHeight * itemCount
                }
            }
        }
    }

    private fun renderProgress(
        progress: SyncInfoProgress,
        progressBar: com.google.android.material.progressindicator.LinearProgressIndicator,
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

    private fun formatLastSyncTime(minutesAgo: Int): String =
        when {
            minutesAgo < 0 -> getString(IDR.string.sync_info_footer_time_none)
            minutesAgo == 0 -> getString(IDR.string.sync_info_footer_time_now)
            minutesAgo == 1 -> getString(IDR.string.sync_info_footer_time_1_minute)
            minutesAgo < 60 -> getString(IDR.string.sync_info_footer_time_minutes, minutesAgo)
            minutesAgo < 2 * 60 -> getString(IDR.string.sync_info_footer_time_1_hour)
            minutesAgo < 24 * 60 -> getString(IDR.string.sync_info_footer_time_hours, minutesAgo / 60)
            minutesAgo < 2 * 24 * 60 -> getString(IDR.string.sync_info_footer_time_1_day)
            else -> getString(IDR.string.sync_info_footer_time_days, minutesAgo / 60 / 24)
        }

    private fun View.setPulseAnimation(isEnabled: Boolean) {
        (tag as? ObjectAnimator?)?.run {
            cancel()
            tag = null
        }
        if (!isEnabled) return
        val progressBarPulseAnimator = ObjectAnimator.ofFloat(
            this,
            View.ALPHA,
            PULSE_ANIMATION_ALPHA_FULL, PULSE_ANIMATION_ALPHA_INTERMEDIATE, PULSE_ANIMATION_ALPHA_MIN,
        ).apply {
            duration = PULSE_ANIMATION_DURATION_MILLIS
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        tag = progressBarPulseAnimator
    }

    private fun getCurrentDestinationId() =
        parentFragment?.takeIf { !syncInfoConfig.isSyncInfoToolbarVisible }?.id // parent if this isn't standalone
            ?: id

    private companion object {
        private const val PULSE_ANIMATION_ALPHA_FULL = 1.0f
        private const val PULSE_ANIMATION_ALPHA_INTERMEDIATE = 0.9f
        private const val PULSE_ANIMATION_ALPHA_MIN = 0.6f

        private const val PULSE_ANIMATION_DURATION_MILLIS = 2000L

        private const val MAX_MODULE_LIST_HEIGHT_ITEMS = 5
    }

}
