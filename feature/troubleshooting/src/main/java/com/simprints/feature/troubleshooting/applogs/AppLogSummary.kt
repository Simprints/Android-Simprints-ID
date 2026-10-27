package com.simprints.feature.troubleshooting.applogs

private const val ONE_HOUR_MS = 60 * 60 * 1000L
private val PROBLEM_SEVERITIES = setOf("Warn", "Error", "Assert")

/**
 * One-line summary of the warnings and errors in the last hour.
 * [entries] must be newest first, as [readRecentLogEntries] returns them.
 */
internal fun problemSummary(
    entries: List<AppLogEntry>,
    nowMs: Long,
): String {
    val recent = entries.filter {
        it.severity in PROBLEM_SEVERITIES && nowMs - parseEpochMillis(it.timestamp) <= ONE_HOUR_MS
    }
    if (recent.isEmpty()) return "No problems in the last hour"

    val count = if (recent.size == 1) "1 problem" else "${recent.size} problems"
    val latest = toLocalTime(recent.first().timestamp).substringBeforeLast('.')
    return "$count in the last hour. Latest at $latest"
}
