package com.simprints.feature.troubleshooting.applogs

/**
 * Groups consecutive entries that have the same severity and message, ignoring their timestamps.
 * Input and output are newest first. Only neighbours are grouped, so the timeline is preserved.
 */
internal fun groupRepeats(entries: List<IndexedValue<AppLogEntry>>): List<List<IndexedValue<AppLogEntry>>> {
    val groups = mutableListOf<MutableList<IndexedValue<AppLogEntry>>>()
    for (item in entries) {
        val current = groups.lastOrNull()
        if (current != null && current.first().value.repeatKey() == item.value.repeatKey()) {
            current += item
        } else {
            groups += mutableListOf(item)
        }
    }
    return groups
}

private fun AppLogEntry.repeatKey(): String = (listOf(severity, message) + lines.drop(1)).joinToString("\n")
