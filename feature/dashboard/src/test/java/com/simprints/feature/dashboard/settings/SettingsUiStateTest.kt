package com.simprints.feature.dashboard.settings

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.settings.SettingsUiState.UpdateConfigSummary
import org.junit.Test

class SettingsUiStateTest {
    @Test
    fun `update config summary keeps the xml default while the last updated value is unknown`() {
        assertThat(SettingsUiState().updateConfigSummary).isEqualTo(UpdateConfigSummary.Empty)
    }

    @Test
    fun `update config summary shows the last updated value once known`() {
        assertThat(SettingsUiState(configLastUpdated = "5 minutes ago").updateConfigSummary)
            .isEqualTo(UpdateConfigSummary.LastUpdated("5 minutes ago"))
    }

    @Test
    fun `update config summary keeps an empty last updated value as it comes from the cache`() {
        // The cache returns "" when no update was ever recorded; the summary then reads "Last updated: " as before.
        assertThat(SettingsUiState(configLastUpdated = "").updateConfigSummary).isEqualTo(UpdateConfigSummary.LastUpdated(""))
    }

    @Test
    fun `update config summary shows scheduled while an update is scheduled, whatever the last updated value`() {
        assertThat(SettingsUiState(isConfigUpdateScheduled = true).updateConfigSummary).isEqualTo(UpdateConfigSummary.Scheduled)
        assertThat(SettingsUiState(configLastUpdated = "5 minutes ago", isConfigUpdateScheduled = true).updateConfigSummary)
            .isEqualTo(UpdateConfigSummary.Scheduled)
    }
}
