package com.simprints.feature.dashboard.settings.syncinfo

import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.simprints.feature.dashboard.R
import com.simprints.infra.resources.R as IDR

/**
 * Pure decisions behind the sync info render: everything that combines the host configuration with the state, or chooses between
 * alternatives. The Fragment only applies the results and formats strings.
 */
internal object SyncInfoRenderRules {
    // The logout host hides the button when the state does; every other host always shows it.
    fun isSyncButtonVisible(
        config: SyncInfoFragmentConfig,
        records: SyncInfoSectionRecords,
    ): Boolean = !config.isSyncInfoLogoutOnComplete || records.isSyncButtonVisible

    fun isFooterSyncInProgressVisible(
        config: SyncInfoFragmentConfig,
        records: SyncInfoSectionRecords,
    ): Boolean = config.isSyncInfoLogoutOnComplete && records.isFooterSyncInProgressVisible

    // Hosts without the module list still show the section while no module is selected, so the user can select some.
    fun isModuleSectionVisible(
        config: SyncInfoFragmentConfig,
        modules: SyncInfoSectionModules,
    ): Boolean = modules.isSectionAvailable && (config.isSyncInfoModuleListVisible || modules.moduleCounts.isEmpty())

    fun isImageSectionVisible(
        config: SyncInfoFragmentConfig,
        syncInfo: SyncInfo,
    ): Boolean = config.isSyncInfoImageSyncVisible && syncInfo.isImageSyncSectionVisible

    @StringRes
    fun syncButtonText(records: SyncInfoSectionRecords): Int = when {
        records.isProgressVisible -> IDR.string.sync_info_button_records_syncing
        records.isSyncButtonForRetry -> IDR.string.sync_info_button_try_again
        else -> IDR.string.sync_info_button_sync_records
    }

    @StringRes
    fun imageSyncButtonText(images: SyncInfoSectionImages): Int = if (images.isProgressVisible) {
        IDR.string.sync_info_button_images_sync_stop
    } else {
        IDR.string.sync_info_button_sync_images
    }

    @ColorRes
    fun imageSyncButtonColor(images: SyncInfoSectionImages): Int = if (images.isProgressVisible) {
        IDR.color.button_sync_images_background_red
    } else {
        IDR.color.button_sync_images_background_default
    }

    // Priority: too many requests > maintenance with a known outage > maintenance > generic.
    fun errorPopupMessage(error: SyncInfoError): ErrorPopupMessage = when {
        error.isTooManyRequests -> ErrorPopupMessage.TooManyRequests
        error.isBackendMaintenance && error.backendMaintenanceEstimatedOutage > 0 -> ErrorPopupMessage.MaintenanceWithOutage(
            error.backendMaintenanceEstimatedOutage,
        )

        error.isBackendMaintenance -> ErrorPopupMessage.Maintenance
        else -> ErrorPopupMessage.Generic
    }

    fun progressLine(part: SyncProgressInfoPart): ProgressLine = when {
        part.isPending -> ProgressLine.Pending
        part.isDone -> ProgressLine.Done
        !part.areNumbersVisible -> ProgressLine.OngoingWithoutCounters
        else -> ProgressLine.Ongoing(part.currentNumber, part.totalNumber)
    }

    // Being run within a nested NavFragment sometimes messes up the "parent" resolution and breaks the navigation result handling,
    // so the destination that receives results is found from the host configuration.
    @IdRes
    fun currentDestinationId(config: SyncInfoFragmentConfig): Int = when {
        config.isSyncInfoLogoutOnComplete -> R.id.logOutSyncFragment // if logging out on complete, we are at logout screen
        config.isSyncInfoToolbarVisible -> R.id.syncInfoFragment // if shown toolbar, we are in standalone sync info fragment
        else -> R.id.mainFragment // otherwise we are in main fragment
    }

    sealed interface ErrorPopupMessage {
        data object TooManyRequests : ErrorPopupMessage

        data class MaintenanceWithOutage(
            val estimatedOutage: Long,
        ) : ErrorPopupMessage

        data object Maintenance : ErrorPopupMessage

        data object Generic : ErrorPopupMessage
    }

    sealed interface ProgressLine {
        data object Pending : ProgressLine

        data object Done : ProgressLine

        data object OngoingWithoutCounters : ProgressLine

        data class Ongoing(
            val current: Int,
            val total: Int,
        ) : ProgressLine
    }
}
