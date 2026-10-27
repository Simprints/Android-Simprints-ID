package com.simprints.feature.troubleshooting.applogs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLogSummaryTest {
    private val now = parseEpochMillis("2026-10-26T15:00:00.000Z")

    @Test
    fun `reports no problems when there are none`() {
        val entries = listOf(entry("2026-10-26T14:50:00.000Z", "Info", "(A) fine"))

        assertThat(problemSummary(entries, now)).isEqualTo("No problems in the last hour")
    }

    @Test
    fun `counts only warnings and errors from the last hour`() {
        val entries = listOf(
            entry("2026-10-26T14:45:00.000Z", "Warn", "(SYNC) retrying"),
            entry("2026-10-26T14:30:00.000Z", "Error", "(CAMERA) failed"),
            entry("2026-10-26T14:20:00.000Z", "Info", "(A) ok"),
            entry("2026-10-26T13:00:00.000Z", "Error", "(OLD) too old"),
        )

        assertThat(problemSummary(entries, now)).startsWith("2 problems in the last hour.")
    }

    @Test
    fun `uses the singular for one problem`() {
        val entries = listOf(entry("2026-10-26T14:30:00.000Z", "Error", "(CAMERA) failed"))

        assertThat(problemSummary(entries, now)).startsWith("1 problem in the last hour.")
    }

    private fun entry(
        timestamp: String,
        severity: String,
        message: String,
    ) = AppLogEntry(
        timestamp = timestamp,
        severity = severity,
        message = message,
        lines = listOf("$timestamp $severity: $message"),
    )
}
