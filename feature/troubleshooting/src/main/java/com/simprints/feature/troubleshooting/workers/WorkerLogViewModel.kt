package com.simprints.feature.troubleshooting.workers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.simprints.feature.troubleshooting.IsoDateTimeFormatter
import com.simprints.feature.troubleshooting.adapter.TroubleshootingItemViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
internal class WorkerLogViewModel @Inject constructor(
    private val workManager: WorkManager,
    @IsoDateTimeFormatter private val dateFormatter: SimpleDateFormat,
) : ViewModel() {
    private val _workers = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val workers: LiveData<List<TroubleshootingItemViewData>>
        get() = _workers

    fun collectWorkerData() {
        viewModelScope.launch {
            workManager
                .getWorkInfosFlow(WorkQuery.fromStates(WorkInfo.State.entries))
                .collect { infos ->
                    infos
                        .map { formatWorkInfo(it) }
                        .take(50)
                        .ifEmpty { listOf(TroubleshootingItemViewData("No data")) }
                        .let { _workers.postValue(it) }
                }
        }
    }

    private fun formatWorkInfo(info: WorkInfo) = TroubleshootingItemViewData(
        // One of the work info tags is worker's full class name
        title = info.tags
            .find { it.startsWith("com.simprints") }
            ?.substringAfterLast(".")
            ?: info.id.toString(),
        subtitle = info.state.toString(),
        body = if (info.state == WorkInfo.State.ENQUEUED) {
            "ID: ${info.id}\nNext run: ${dateFormatter.format(info.nextScheduleTimeMillis)}"
        } else {
            "ID: ${info.id}\nOutput: ${info.outputData}"
        },
    )
}
