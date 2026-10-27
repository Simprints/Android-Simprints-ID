package com.simprints.feature.troubleshooting.applogs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLogGroupingTest {
    @Test
    fun `groups consecutive entries with the same message regardless of timestamp`() {
        val entries = listOf(
            entry(0, "2026-10-26T14:12:00.000Z", "Warn", "(SYNC) retrying"),
            entry(1, "2026-10-26T14:11:00.000Z", "Warn", "(SYNC) retrying"),
            entry(2, "2026-10-26T14:10:00.000Z", "Info", "(SYNC) started"),
        )

        val groups = groupRepeats(entries)

        assertThat(groups.map { it.size }).containsExactly(2, 1).inOrder()
        assertThat(groups[0].map { it.index }).containsExactly(0, 1).inOrder()
    }

    @Test
    fun `does not group repeats separated by a different entry`() {
        val entries = listOf(
            entry(0, "2026-10-26T14:12:00.000Z", "Warn", "(SYNC) retrying"),
            entry(1, "2026-10-26T14:11:00.000Z", "Info", "(SYNC) started"),
            entry(2, "2026-10-26T14:10:00.000Z", "Warn", "(SYNC) retrying"),
        )

        assertThat(groupRepeats(entries).map { it.size }).containsExactly(1, 1, 1).inOrder()
    }

    @Test
    fun `does not group the same message at different severities`() {
        val entries = listOf(
            entry(0, "2026-10-26T14:12:00.000Z", "Error", "(CAMERA) failed"),
            entry(1, "2026-10-26T14:11:00.000Z", "Warn", "(CAMERA) failed"),
        )

        assertThat(groupRepeats(entries).map { it.size }).containsExactly(1, 1).inOrder()
    }

    private fun entry(
        index: Int,
        timestamp: String,
        severity: String,
        message: String,
    ) = IndexedValue(
        index,
        AppLogEntry(
            timestamp = timestamp,
            severity = severity,
            message = message,
            lines = listOf("$timestamp $severity: $message"),
        ),
    )
}
