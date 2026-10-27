package com.simprints.feature.troubleshooting.applogs

import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.adapter.TroubleshootingItemViewData
import com.simprints.feature.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.infra.logging.LogDirectoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class AppLogViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logDirectoryProvider: LogDirectoryProvider,
    private val collectIds: CollectIdsUseCase,
) : ViewModel() {
    private val _logs = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val logs: LiveData<List<TroubleshootingItemViewData>>
        get() = _logs

    val deviceInfo: LiveData<String>
        get() = _deviceInfo
    private val _deviceInfo = MutableLiveData("Collecting device details")

    val summary: LiveData<String>
        get() = _summary
    private val _summary = MutableLiveData("Checking logs...")

    private var entries: List<AppLogEntry> = emptyList()
    private var severities: Set<String> = severitiesFor(DEFAULT_GROUPS)
    private var query: String = ""

    fun collectData() {
        _deviceInfo.value = buildDeviceInfo()
        viewModelScope.launch {
            entries = withContext(Dispatchers.IO) {
                readRecentLogEntries(logDirectoryProvider(context), MAX_ENTRIES)
            }
            _summary.value = problemSummary(entries, System.currentTimeMillis())
            render()
        }
    }

    /** [groups] are the filter group names from the chips, e.g. [GROUP_PROBLEMS]. */
    fun setGroups(groups: Set<String>) {
        severities = severitiesFor(groups)
        render()
    }

    fun setQuery(text: String) {
        query = text.trim()
        render()
    }

    /** Full text of an entry for the details dialog. [navigationId] is the entry's index in the loaded entries. */
    fun fullTextFor(navigationId: String): String = navigationId.toIntOrNull()?.let { entries.getOrNull(it)?.fullText }.orEmpty()

    private fun buildDeviceInfo(): String {
        val appVersion = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "unknown"
        return listOf("App $appVersion · Android ${Build.VERSION.RELEASE} · ${Build.MODEL}", collectIds()).joinToString("\n")
    }

    private fun render() {
        val visible = entries.withIndex().filter { (_, entry) -> entry.matches(severities, query) }
        _logs.value = when {
            entries.isEmpty() -> listOf(TroubleshootingItemViewData(title = "No app logs yet"))
            visible.isEmpty() -> listOf(TroubleshootingItemViewData(title = "Nothing matches these filters"))
            else -> groupRepeats(visible).map { toRow(it) }
        }
    }

    /** One row per group of repeated entries. The row shows the newest entry, with a count when it repeats. */
    private fun toRow(group: List<IndexedValue<AppLogEntry>>): TroubleshootingItemViewData {
        val newest = group.first().value
        val time = toLocalDateTime(newest.timestamp)
        val messageLines = listOf(newest.message) + newest.lines.drop(1)
        val background = backgroundFor(newest.severity)
        val navigationId = group.first().index.toString()

        return if (group.size == 1) {
            TroubleshootingItemViewData(
                title = time,
                body = messageLines.joinToString("\n"),
                navigationId = navigationId,
                backgroundColor = background,
            )
        } else {
            val range = "from ${toLocalDateTime(group.last().value.timestamp)} to $time"
            TroubleshootingItemViewData(
                title = "$time (x${group.size})",
                body = (listOf("Repeated ${group.size} times $range") + messageLines).joinToString("\n"),
                navigationId = navigationId,
                backgroundColor = background,
            )
        }
    }

    /** Problems get a tint so they stand out from normal steps. */
    private fun backgroundFor(severity: String): Int? = when (severity) {
        "Error", "Assert" -> ContextCompat.getColor(context, R.color.app_log_error_background)
        "Warn" -> ContextCompat.getColor(context, R.color.app_log_warn_background)
        else -> null
    }

    companion object {
        const val MAX_ENTRIES = 1000
        const val GROUP_PROBLEMS = "Problems"
        const val GROUP_STEPS = "Steps"
        val DEFAULT_GROUPS = setOf(GROUP_PROBLEMS)

        private val SEVERITIES_BY_GROUP = mapOf(
            GROUP_PROBLEMS to setOf("Warn", "Error", "Assert"),
            GROUP_STEPS to setOf("Info"),
        )

        fun severitiesFor(groups: Set<String>): Set<String> = groups.flatMapTo(mutableSetOf()) { SEVERITIES_BY_GROUP[it].orEmpty() }
    }
}
