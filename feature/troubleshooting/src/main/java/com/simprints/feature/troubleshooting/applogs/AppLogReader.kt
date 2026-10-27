package com.simprints.feature.troubleshooting.applogs

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val ISO_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val LOCAL_TIME = "HH:mm:ss.SSS"
private const val LOCAL_DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSS"

// Entries start with "2026-10-26T14:10:59.226Z Info: (Tag) message".
// Lines that follow (such as stack traces) belong to the entry above them.
private val ENTRY_HEADER = Regex("""^(\S+) (Verbose|Debug|Info|Warn|Error|Assert): (.*)$""")

// Log timestamps drop trailing zeros, so "59.16Z" means 160 ms and "59.1Z" means 100 ms.
private val FRACTION = Regex("""\.(\d+)Z$""")

internal data class AppLogEntry(
    val timestamp: String,
    val severity: String,
    val message: String,
    val lines: List<String>,
) {
    val fullText: String
        get() = lines.joinToString("\n")
}

internal fun AppLogEntry.matches(
    severities: Set<String>,
    query: String,
): Boolean = severity in severities && (query.isBlank() || fullText.contains(query, ignoreCase = true))

/**
 * Returns up to [maxEntries] of the most recent log entries, sorted newest first by timestamp.
 *
 * Log files are rolled by size, so the most recently modified file holds the newest entries.
 * Each file is streamed and only its last entries are kept, so memory stays bounded.
 */
internal fun readRecentLogEntries(
    directory: File,
    maxEntries: Int,
): List<AppLogEntry> {
    if (maxEntries <= 0) return emptyList()

    val files = directory
        .listFiles()
        .orEmpty()
        .filter { it.isFile }
        .sortedByDescending { it.lastModified() }

    val result = mutableListOf<AppLogEntry>()
    for (file in files) {
        val remaining = maxEntries - result.size
        if (remaining <= 0) break
        // A file can be rolled or deleted while being read; skip it rather than failing the whole view
        val tail = runCatching { tailEntries(file, remaining) }.getOrDefault(emptyList())
        result += tail.asReversed()
    }

    return result
        .map { it to parseEpochMillis(it.timestamp) }
        .sortedByDescending { it.second }
        .map { it.first }
}

/** Returns the last [count] entries in the file, oldest first. */
private fun tailEntries(
    file: File,
    count: Int,
): List<AppLogEntry> = file.useLines { lines ->
    val tail = ArrayDeque<AppLogEntry>(count)
    var header: MatchResult? = null
    val body = mutableListOf<String>()

    fun closeEntry() {
        val match = header ?: return
        if (tail.size == count) tail.removeFirst()
        tail.addLast(
            AppLogEntry(
                timestamp = match.groupValues[1],
                severity = match.groupValues[2],
                message = match.groupValues[3],
                lines = body.toList(),
            ),
        )
    }

    for (line in lines) {
        val match = ENTRY_HEADER.matchEntire(line)
        if (match != null) {
            closeEntry()
            header = match
            body.clear()
        }
        // Lines before the first entry header are dropped
        if (header != null) body += line
    }
    closeEntry()
    tail.toList()
}

private fun parseUtc(isoTimestamp: String): Date? = runCatching {
    val normalized = FRACTION.replace(isoTimestamp) { match ->
        "." + match.groupValues[1].padEnd(3, '0').take(3) + "Z"
    }
    SimpleDateFormat(ISO_TIMESTAMP, Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .parse(normalized)
}.getOrNull()

/** Milliseconds since the epoch for an entry timestamp, or 0 if it cannot be parsed. */
internal fun parseEpochMillis(isoTimestamp: String): Long = parseUtc(isoTimestamp)?.time ?: 0L

/** Converts an entry timestamp such as "2026-10-26T14:10:59.729Z" to local time "14:10:59.729". */
internal fun toLocalTime(
    isoTimestamp: String,
    zone: TimeZone = TimeZone.getDefault(),
): String = formatLocal(isoTimestamp, LOCAL_TIME, zone)

/** Converts an entry timestamp to local date and time, e.g. "2026-10-26 14:10:59.729". */
internal fun toLocalDateTime(
    isoTimestamp: String,
    zone: TimeZone = TimeZone.getDefault(),
): String = formatLocal(isoTimestamp, LOCAL_DATE_TIME, zone)

private fun formatLocal(
    isoTimestamp: String,
    pattern: String,
    zone: TimeZone,
): String {
    val parsed = parseUtc(isoTimestamp) ?: return isoTimestamp
    return SimpleDateFormat(pattern, Locale.US).apply { timeZone = zone }.format(parsed)
}
