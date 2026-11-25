package com.simprints.feature.dashboard.settings.syncinfo

import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoRenderRules.ErrorPopupMessage
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoRenderRules.ProgressLine
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import org.junit.Test
import com.simprints.infra.resources.R as IDR

class SyncInfoRenderRulesTest {
    @Test
    fun `logout host shows the sync button only when the state does`() {
        assertThat(SyncInfoRenderRules.isSyncButtonVisible(LOGOUT, SyncInfoSectionRecords(isSyncButtonVisible = true))).isTrue()
        assertThat(SyncInfoRenderRules.isSyncButtonVisible(LOGOUT, SyncInfoSectionRecords(isSyncButtonVisible = false))).isFalse()
    }

    @Test
    fun `standalone and main hosts always show the sync button`() {
        assertThat(SyncInfoRenderRules.isSyncButtonVisible(STANDALONE, SyncInfoSectionRecords(isSyncButtonVisible = false))).isTrue()
        assertThat(SyncInfoRenderRules.isSyncButtonVisible(MAIN, SyncInfoSectionRecords(isSyncButtonVisible = false))).isTrue()
    }

    @Test
    fun `only the logout host shows the sync in progress footer, and only when the state does`() {
        val shown = SyncInfoSectionRecords(isFooterSyncInProgressVisible = true)
        val hidden = SyncInfoSectionRecords(isFooterSyncInProgressVisible = false)

        assertThat(SyncInfoRenderRules.isFooterSyncInProgressVisible(LOGOUT, shown)).isTrue()
        assertThat(SyncInfoRenderRules.isFooterSyncInProgressVisible(LOGOUT, hidden)).isFalse()
        assertThat(SyncInfoRenderRules.isFooterSyncInProgressVisible(STANDALONE, shown)).isFalse()
        assertThat(SyncInfoRenderRules.isFooterSyncInProgressVisible(MAIN, shown)).isFalse()
    }

    @Test
    fun `module section with the module list is shown whenever it is available`() {
        assertThat(SyncInfoRenderRules.isModuleSectionVisible(STANDALONE, modules(available = true, counts = MODULE_COUNTS))).isTrue()
        assertThat(SyncInfoRenderRules.isModuleSectionVisible(STANDALONE, modules(available = true, counts = emptyList()))).isTrue()
        assertThat(SyncInfoRenderRules.isModuleSectionVisible(STANDALONE, modules(available = false, counts = MODULE_COUNTS))).isFalse()
    }

    @Test
    fun `module section without the module list is shown only while no module is selected`() {
        listOf(MAIN, LOGOUT).forEach { host ->
            assertThat(SyncInfoRenderRules.isModuleSectionVisible(host, modules(available = true, counts = emptyList()))).isTrue()
            assertThat(SyncInfoRenderRules.isModuleSectionVisible(host, modules(available = true, counts = MODULE_COUNTS))).isFalse()
            assertThat(SyncInfoRenderRules.isModuleSectionVisible(host, modules(available = false, counts = emptyList()))).isFalse()
        }
    }

    @Test
    fun `image section needs both the host flag and the state flag`() {
        assertThat(SyncInfoRenderRules.isImageSectionVisible(STANDALONE, SyncInfo(isImageSyncSectionVisible = true))).isTrue()
        assertThat(SyncInfoRenderRules.isImageSectionVisible(STANDALONE, SyncInfo(isImageSyncSectionVisible = false))).isFalse()
        assertThat(SyncInfoRenderRules.isImageSectionVisible(MAIN, SyncInfo(isImageSyncSectionVisible = true))).isFalse()
        assertThat(SyncInfoRenderRules.isImageSectionVisible(LOGOUT, SyncInfo(isImageSyncSectionVisible = true))).isFalse()
    }

    @Test
    fun `sync button text prefers syncing over try again over sync records`() {
        assertThat(SyncInfoRenderRules.syncButtonText(SyncInfoSectionRecords(isProgressVisible = true, isSyncButtonForRetry = true)))
            .isEqualTo(IDR.string.sync_info_button_records_syncing)
        assertThat(SyncInfoRenderRules.syncButtonText(SyncInfoSectionRecords(isSyncButtonForRetry = true)))
            .isEqualTo(IDR.string.sync_info_button_try_again)
        assertThat(SyncInfoRenderRules.syncButtonText(SyncInfoSectionRecords()))
            .isEqualTo(IDR.string.sync_info_button_sync_records)
    }

    @Test
    fun `image sync button is a red stop button while images sync`() {
        val syncing = SyncInfoSectionImages(isProgressVisible = true)

        assertThat(SyncInfoRenderRules.imageSyncButtonText(syncing)).isEqualTo(IDR.string.sync_info_button_images_sync_stop)
        assertThat(SyncInfoRenderRules.imageSyncButtonColor(syncing)).isEqualTo(IDR.color.button_sync_images_background_red)
    }

    @Test
    fun `image sync button is the default sync button while images do not sync`() {
        val idle = SyncInfoSectionImages(isProgressVisible = false)

        assertThat(SyncInfoRenderRules.imageSyncButtonText(idle)).isEqualTo(IDR.string.sync_info_button_sync_images)
        assertThat(SyncInfoRenderRules.imageSyncButtonColor(idle)).isEqualTo(IDR.color.button_sync_images_background_default)
    }

    @Test
    fun `error popup prefers too many requests over maintenance`() {
        val error = SyncInfoError(isBackendMaintenance = true, backendMaintenanceEstimatedOutage = 600, isTooManyRequests = true)

        assertThat(SyncInfoRenderRules.errorPopupMessage(error)).isEqualTo(ErrorPopupMessage.TooManyRequests)
    }

    @Test
    fun `error popup shows maintenance with the outage only when the outage is positive`() {
        assertThat(
            SyncInfoRenderRules.errorPopupMessage(
                SyncInfoError(
                    isBackendMaintenance = true,
                    backendMaintenanceEstimatedOutage = 600,
                ),
            ),
        ).isEqualTo(ErrorPopupMessage.MaintenanceWithOutage(600))
        assertThat(SyncInfoRenderRules.errorPopupMessage(SyncInfoError(isBackendMaintenance = true, backendMaintenanceEstimatedOutage = 0)))
            .isEqualTo(ErrorPopupMessage.Maintenance)
        assertThat(
            SyncInfoRenderRules.errorPopupMessage(
                SyncInfoError(
                    isBackendMaintenance = true,
                    backendMaintenanceEstimatedOutage = -1,
                ),
            ),
        ).isEqualTo(ErrorPopupMessage.Maintenance)
    }

    @Test
    fun `error popup without maintenance or too many requests is generic, whatever the outage`() {
        assertThat(SyncInfoRenderRules.errorPopupMessage(SyncInfoError())).isEqualTo(ErrorPopupMessage.Generic)
        assertThat(SyncInfoRenderRules.errorPopupMessage(SyncInfoError(backendMaintenanceEstimatedOutage = 600)))
            .isEqualTo(ErrorPopupMessage.Generic)
    }

    @Test
    fun `progress line prefers pending over done over ongoing`() {
        assertThat(SyncInfoRenderRules.progressLine(SyncProgressInfoPart(isPending = true, isDone = true, areNumbersVisible = true)))
            .isEqualTo(ProgressLine.Pending)
        assertThat(SyncInfoRenderRules.progressLine(SyncProgressInfoPart(isPending = false, isDone = true, areNumbersVisible = true)))
            .isEqualTo(ProgressLine.Done)
    }

    @Test
    fun `ongoing progress line shows counters only when they are visible`() {
        assertThat(
            SyncInfoRenderRules.progressLine(
                SyncProgressInfoPart(isPending = false, areNumbersVisible = false, currentNumber = 3, totalNumber = 10),
            ),
        ).isEqualTo(ProgressLine.OngoingWithoutCounters)
        assertThat(
            SyncInfoRenderRules.progressLine(
                SyncProgressInfoPart(isPending = false, areNumbersVisible = true, currentNumber = 3, totalNumber = 10),
            ),
        ).isEqualTo(ProgressLine.Ongoing(current = 3, total = 10))
    }

    @Test
    fun `results are read from the destination of each host`() {
        assertThat(SyncInfoRenderRules.currentDestinationId(LOGOUT)).isEqualTo(R.id.logOutSyncFragment)
        assertThat(SyncInfoRenderRules.currentDestinationId(STANDALONE)).isEqualTo(R.id.syncInfoFragment)
        assertThat(SyncInfoRenderRules.currentDestinationId(MAIN)).isEqualTo(R.id.mainFragment)
    }

    @Test
    fun `logout on complete wins over a visible toolbar for the result destination`() {
        assertThat(
            SyncInfoRenderRules.currentDestinationId(LOGOUT.copy(isSyncInfoToolbarVisible = true)),
        ).isEqualTo(R.id.logOutSyncFragment)
    }

    private fun modules(
        available: Boolean,
        counts: List<ModuleCount>,
    ) = SyncInfoSectionModules(isSectionAvailable = available, moduleCounts = counts)

    private companion object {
        // Standalone syncInfoFragment destination: no container, defaults
        val STANDALONE = SyncInfoFragmentConfig()

        // Container attributes in fragment_main.xml
        val MAIN = SyncInfoFragmentConfig(
            isSyncInfoToolbarVisible = false,
            isSyncInfoStatusHeaderVisible = true,
            isSyncInfoStatusHeaderSettingsButtonVisible = true,
            areSyncInfoSectionHeadersVisible = false,
            isSyncInfoImageSyncVisible = false,
            isSyncInfoRecordsImagesCombined = false,
            isSyncInfoLogoutOnComplete = false,
            isSyncInfoModuleListVisible = false,
        )

        // Container attributes in fragment_logout_sync.xml
        val LOGOUT = SyncInfoFragmentConfig(
            isSyncInfoToolbarVisible = false,
            isSyncInfoStatusHeaderVisible = true,
            isSyncInfoStatusHeaderSettingsButtonVisible = false,
            areSyncInfoSectionHeadersVisible = false,
            isSyncInfoImageSyncVisible = false,
            isSyncInfoRecordsImagesCombined = true,
            isSyncInfoLogoutOnComplete = true,
            isSyncInfoModuleListVisible = false,
        )

        val MODULE_COUNTS = listOf(ModuleCount(name = "", count = 3), ModuleCount(name = "module", count = 3))
    }
}
